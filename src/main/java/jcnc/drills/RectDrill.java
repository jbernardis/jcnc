package main.java.jcnc.drills;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONObject;

import main.java.jcnc.CncDialog;
import main.java.jcnc.CncProperties;
import main.java.jcnc.Point2D;
import main.java.jcnc.Rotator;

public class RectDrill extends CncDialog{
	Text tHeight;
	Text tWidth;
	Text tStartX;
	Text tStartY;
	Text tStartZ;
	Text tSafeZ;
	Text tAngle;
	Text tHoleDiam;
	Text tSpacing;
	Text tDepth;
	Text tPassDepth;
	Text tToolDiam;
	Text tStepOver;

	Text tGCode;

	Button bGenerate;
	Button bView;
	Button bSave;
	Button bSerialize;
	Button bDeserialize;

	Button bInside;
	boolean inside;

	Button bPerimeterOnly;
	boolean perimeterOnly;

	Button bHolePerimeterOnly;
	boolean holePerimeterOnly;

	Button bStagger;
	boolean stagger;

	Button bRetract;
	boolean retract;
	
	String startingPoint = null;
	HashMap<String, Button> hmStartingPoint = new HashMap<String, Button>();
	String cutDirection = null;
	HashMap<String, Button> hmCutDirection = new HashMap<String, Button>();

	public RectDrill(Shell parent, CncProperties props) {
		super(parent, props);
		objectName = "Rectangular Drill Pattern " + seqNo;
	}

	public void open() {
		super.open();
		createContents();
		JSONInit();
		super.dlgLoop();
	}

	private void createContents() {
		GridLayout gl = new GridLayout();
		gl.numColumns = 4;
		gl.makeColumnsEqualWidth = false;
		gl.verticalSpacing = 10;
		gl.horizontalSpacing = 20;
		shell.setLayout(gl);

		addLabel("Width(X): ");
		tWidth = addDoubleText(100.0F);

		addLabel("Height(Y): ");
		tHeight = addDoubleText(100.0F);

		addLabel("Start X: ");
		tStartX = addDoubleText(0.0F);

		addLabel("Start Y: ");
		tStartY = addDoubleText(0.0F);

		addLabel("Start Z: ");
		tStartZ = addDoubleText(0.0F);

		addLabel("Safe Z: ");
		tSafeZ = addDoubleText(10.0F);

		addLabel("Rotation Angle: ");
		tAngle = addDoubleText(0.0F);
		GridData gd = (GridData) tAngle.getLayoutData();
		gd.horizontalSpan = 3;
		tAngle.setLayoutData(gd);

		addLabel("Hole Diameter: ");
		tHoleDiam = addDoubleText(1.0F);

		addLabel("Minimum Spacing: ");
		tSpacing = addDoubleText(10.0F);

		addLabel("Depth: ");
		tDepth = addDoubleText(1.0F);

		addLabel("Depth/Pass: ");
		Double dpp = (Double) toolMaterialInfo.get("depthperpass");
		tPassDepth = addDoubleText(dpp);

		bAddSpeed = new Button(shell, SWT.CHECK);
		bAddSpeed.setText("Add Speed Term");
		bAddSpeed.setData("addspeed");
		bAddSpeed.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				addSpeedTerm = bAddSpeed.getSelection();
				props.setPropAddSpeed(addSpeedTerm);
				setModified(true);
				enableSpeedControls();
			}
		});
		gd = new GridData();
		gd.horizontalSpan = 4;
		gd.horizontalAlignment = SWT.CENTER;
		bAddSpeed.setLayoutData(gd);
		addSpeedTerm = props.getPropAddSpeed();
		bAddSpeed.setSelection(addSpeedTerm);

		addLabel("XY Normal Speed: ");
		tXYNormalSpeed = addDoubleText((Double) toolMaterialInfo.get("normalxy"));

		addLabel("Z Normal Speed: ");
		tZNormalSpeed = addDoubleText((Double) toolMaterialInfo.get("normalz"));

		addLabel("XY Rapid Speed: ");
		tXYRapidSpeed = addDoubleText((Double) toolMaterialInfo.get("rapidxy"));

		addLabel("Z Rapid Speed: ");
		tZRapidSpeed = addDoubleText((Double) toolMaterialInfo.get("rapidz"));

		enableSpeedControls();

		addLabel("Tool Diameter: ");
		tToolDiam = addDoubleText((Double) toolInfo.get("diameter"));

		addLabel("Step Over: ");
		tStepOver = addDoubleText((Double) toolMaterialInfo.get("stepover"));

		Group sp = addStartingPoint();
		gd = (GridData) sp.getLayoutData();
		gd.horizontalSpan = 2;
		sp.setLayoutData(gd);

		Group cd = addCutDirection();
		gd = (GridData) cd.getLayoutData();
		gd.horizontalSpan = 2;
		cd.setLayoutData(gd);
		
		bInside = new Button(shell, SWT.CHECK);
		bInside.setText("Inside Rectangle");
		bInside.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				inside = bInside.getSelection();
				setModified(true);
			}
		});
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.LEFT;
		gd.horizontalIndent = 10;
		bInside.setLayoutData(gd);
		inside = false;
		bInside.setSelection(inside);

		bHolePerimeterOnly = new Button(shell, SWT.CHECK);
		bHolePerimeterOnly.setText("Hole Perimeter Only");
		bHolePerimeterOnly.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				holePerimeterOnly = bHolePerimeterOnly.getSelection();
				setModified(true);
			}
		});
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.LEFT;
		gd.horizontalIndent = 10;
		bHolePerimeterOnly.setLayoutData(gd);
		holePerimeterOnly = false;
		bHolePerimeterOnly.setSelection(holePerimeterOnly);
		
				
		bStagger = new Button(shell, SWT.CHECK);
		bStagger.setText("Staggered Rows");
		bStagger.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				stagger = bStagger.getSelection();
				setModified(true);
			}
		});
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.LEFT;
		gd.horizontalIndent = 10;
		bStagger.setLayoutData(gd);
		stagger = false;
		bStagger.setSelection(stagger);

		bPerimeterOnly = new Button(shell, SWT.CHECK);
		bPerimeterOnly.setText("Rectangle Perimeter Only");
		bPerimeterOnly.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				perimeterOnly = bPerimeterOnly.getSelection();
				setModified(true);
			}
		});
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.LEFT;
		gd.horizontalIndent = 10;
		bPerimeterOnly.setLayoutData(gd);
		perimeterOnly = false;
		bPerimeterOnly.setSelection(perimeterOnly);
		
		bRetract = new Button(shell, SWT.CHECK);
		bRetract.setText("Retract each pass");
		bRetract.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				retract = bRetract.getSelection();
				setModified(true);
			}
		});
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.LEFT;
		gd.horizontalIndent = 10;
		bRetract.setLayoutData(gd);
		retract = false;
		bRetract.setSelection(retract);
		
		
		
		Group c = new Group(shell, SWT.NONE);
		RowLayout rl = new RowLayout();
		rl.spacing = 20;
		rl.marginWidth = 20;
		rl.marginBottom = 10;
		rl.center = true;
		c.setLayout(rl);

		Display disp = shell.getDisplay();
		Image img = new Image(disp, "resources/gcode.png");

		bGenerate = new Button(c, SWT.PUSH);
		bGenerate.setImage(img);
		bGenerate.setToolTipText("Generate G Code");
		bGenerate.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				generateGCode();
			}
		});
		RowData rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bGenerate.setLayoutData(rd);

		img = new Image(disp, "resources/view.png");
		bView = new Button(c, SWT.PUSH);
		bView.setImage(img);
		bView.setToolTipText("View G Code");
		bView.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				viewGCode(tGCode);
			}
		});
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bView.setLayoutData(rd);
		bView.setEnabled(false);
		;

		img = new Image(disp, "resources/filesaveas.png");
		bSave = new Button(c, SWT.PUSH);
		bSave.setImage(img);
		bSave.setToolTipText("Save G Code to a file");
		bSave.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				saveGCode(tGCode);
			}
		});
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bSave.setLayoutData(rd);
		bSave.setEnabled(false);
		;

		img = new Image(disp, "resources/tojson.png");
		bSerialize = new Button(c, SWT.PUSH);
		bSerialize.setImage(img);
		bSerialize.setToolTipText("Save data to file");
		bSerialize.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				serialize();
			}
		});
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bSerialize.setLayoutData(rd);

		img = new Image(disp, "resources/fromjson.png");
		bDeserialize = new Button(c, SWT.PUSH);
		bDeserialize.setImage(img);
		bDeserialize.setToolTipText("Read data from file");
		bDeserialize.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				deserialize();
			}
		});
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bDeserialize.setLayoutData(rd);

		GridData cgd = new GridData();
		cgd.horizontalSpan = 4;
		cgd.horizontalAlignment = SWT.CENTER;
		cgd.grabExcessHorizontalSpace = true;
		c.setLayoutData(cgd);

		tGCode = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData(SWT.FILL, 4, true, true);
		gd.horizontalSpan = 4;
		gd.heightHint = 200;
		tGCode.setLayoutData(gd);
	}

	private Group addStartingPoint() {
		hmStartingPoint.clear();
		SelectionAdapter bStartingPointPress = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Button b = (Button) event.getSource();
				if (b.getSelection()) {
					setModified(true);
					startingPoint = b.getText();
				}
			}
		};
		Group c = new Group(shell, SWT.NONE);
		c.setText(" Starting Point ");
		c.setLayout(new GridLayout(1, false));
		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		String[] tags = {"Lower Left", "Upper Left", "Lower Right", "Upper Right", "Center"};

		for (String k : tags) {
			Button b = new Button(c, SWT.RADIO);
			b.setText(k);
			b.addSelectionListener(bStartingPointPress);
			hmStartingPoint.put(k, b);			
		}
		selectStartingPoint(tags[0]);

		return c;
	}
	
	private void selectStartingPoint(String k) {
		if (!hmStartingPoint.containsKey(k))
			return;

		Button b;
		
		if (startingPoint != null) {
			b = hmStartingPoint.get(startingPoint);
			if (b != null)
				b.setSelection(false);
		}
		
		b = hmStartingPoint.get(k);
		b.setSelection(true);
		startingPoint = k;
	}

	private Group addCutDirection() {
		hmCutDirection.clear();
		SelectionAdapter bCutDirectionPress = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Button b = (Button) event.getSource();
				if (b.getSelection()) {
					setModified(true);
					cutDirection = b.getText();
				}
			}
		};
		Group c = new Group(shell, SWT.NONE);
		c.setText(" Cut Direction ");
		c.setLayout(new GridLayout(1, false));
		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		String[] tags = {"Clockwise", "Counter Clockwise"};

		for (String k : tags) {
			Button b = new Button(c, SWT.RADIO);
			b.setText(k);
			b.addSelectionListener(bCutDirectionPress);
			hmCutDirection.put(k, b);			
		}
		selectCutDirection(tags[0]);

		return c;
	}
	
	private void selectCutDirection(String k) {
		if (!hmCutDirection.containsKey(k))
			return;

		Button b;
		
		if (cutDirection != null) {
			b = hmCutDirection.get(cutDirection);
			if (b != null)
				b.setSelection(false);
		}
		
		b = hmCutDirection.get(k);
		b.setSelection(true);
		cutDirection = k;
	}

	private void generateGCode() {
		bView.setEnabled(false);
		bSave.setEnabled(false);
		haveGCode = false;
		GCStale = false;
		unsaved = false;
		setTitle();

		double height = Double.valueOf(tHeight.getText());
		double width = Double.valueOf(tWidth.getText());
		double sx = Double.valueOf(tStartX.getText());
		double sy = Double.valueOf(tStartY.getText());
		double sz = Double.valueOf(tStartZ.getText());
		double safez = Double.valueOf(tSafeZ.getText());
		double angle = Double.valueOf(tAngle.getText());
		double holediam = Double.valueOf(tHoleDiam.getText());
		double spacing = Double.valueOf(tSpacing.getText());
		double depth = Double.valueOf(tDepth.getText());
		double passdepth = Double.valueOf(tPassDepth.getText());
		double tooldiam = Double.valueOf(tToolDiam.getText());
		double stepover = Double.valueOf(tStepOver.getText());

		double XYNormal = Double.valueOf(tXYNormalSpeed.getText());
		double XYRapid = Double.valueOf(tXYRapidSpeed.getText());
		double ZNormal = Double.valueOf(tZNormalSpeed.getText());
		double ZRapid = Double.valueOf(tZRapidSpeed.getText());

		
		tGCode.setText("");

		String stXYNormal = "";
		String stXYRapid = "";
		String stZNormal = "";
		String stZRapid = "";

		if (addSpeedTerm) {
			stXYNormal = " F" + fmtDbl(XYNormal);
			stXYRapid = " F" + fmtDbl(XYRapid);
			stZNormal = " F" + fmtDbl(ZNormal);
			stZRapid = " F" + fmtDbl(ZRapid);
		}
		
		boolean metric = props.getPropMetric();

		int passes = (int) Math.ceil(depth / passdepth);

		Rotator rot = new Rotator(angle);
		
		tGCode.append("(" + objectName + ")\n");
		tGCode.append("(Rectangular Drill Pattern " + fmtDbl(width) + ", " + fmtDbl(height) + " ");
		if (angle == 0) {
			tGCode.append("starting at " + fmtDbl(sx) + ", " + fmtDbl(sy) + " ");
		}
		else {
			Point2D start = rot.rotate(new Point2D(sx, sy));
			tGCode.append("starting at " + fmtDbl(start.x) + ", " + fmtDbl(start.y) + " ");
			tGCode.append("rotation angle " + fmtDbl(angle));
		}
		tGCode.append("depth from " + fmtDbl(sz) + " to -" + fmtDbl(depth) + ")\n");
		if (tooldiam == (Double) toolInfo.get("diameter")) {
			tGCode.append("(Tool " + (String) toolInfo.get("name") + " diameter: " + fmtDbl(tooldiam));
		}
		else {
			tGCode.append("(Tool diameter " + fmtDbl(tooldiam));
		}
		tGCode.append("   stepover " + fmtDbl(stepover) + " ");
		tGCode.append("   depth/pass -" + fmtDbl(passdepth) + ")\n");
		tGCode.append("(Starting point: " + startingPoint + ")\n");
		tGCode.append("(Cutting Direction: " + cutDirection + ")\n");
		tGCode.append("(Inside Circle: " + inside + "  Staggered rows: " + stagger + ")\n");
		tGCode.append("(Retract each pass: " + retract + ")\n");
		tGCode.append("(Rectangle Perimeter only: " + perimeterOnly + "  hole perimeter only: " + holePerimeterOnly + ")\n");

		tGCode.append("G90\n");
		if (metric)
			tGCode.append("G21 (Metric)\n");
		else
			tGCode.append("G20 (Imperial)\n");
		
		if (startingPoint.equals("Upper Left")) {
			sy = sy - height;
		}
		else if (startingPoint.equals("Upper Right")) {
			sy = sy - height;
			sx = sx - width;
		}
		else if (startingPoint.equals("Lower Right")) {
			sx = sx - width;
		}
		else if (startingPoint.equals("Center")) {
			sx = sx - width/2.0;
			sy = sy - height/2.0;
		}

		boolean staggerRows = stagger;
		if (perimeterOnly)
			staggerRows = false;
		
		Double minx = sx;
		Double maxx = sx + width;
		Double miny = sy;
		Double maxy = sy + height;
		
		if (inside) {
			minx = sx + holediam/2.0;
			maxx = sx + width - holediam/2.0;
			miny = sy + holediam/2.0;
			maxy = sy + height - holediam/2.0;
		}

		String cmd = "G3";
		if (cutDirection.equals("Clockwise"))
			cmd = "G2";
		
		int nrows = (int) ((maxy - miny)/(holediam+spacing));
		int ncols = (int) ((maxx - minx)/(holediam+spacing));
		
		Double xstep = (maxx - minx) / ncols;
		Double ystep = (maxy - miny) / nrows;
		
		if (staggerRows) {
			ystep = ystep * 0.866;
			nrows = (int) Math.round(nrows/0.866);
		}
		
		Double cx = minx;
		Double cy = miny;

		tGCode.append(moveZ(safez,  "G0",  stZRapid));
		
		Point2D rp;

		Double yoff;
		Double maxyoff = holediam/2.0 - tooldiam/2.0;
		
		for (int iy = 0; iy <= nrows; iy++) {
			for (int ix = 0; ix <= ncols; ix++) {
				boolean includeHole = false;
				if (!perimeterOnly) {
					if (cx <= maxx && cy <= maxy)
						includeHole = true;
				}
				else {
					if (ix == 0 || ix == ncols || iy == 0 || iy == nrows)
						includeHole = true;
				}
						
				if (includeHole) {
					rp = rot.rotate(new Point2D(cx, cy));
					if (holePerimeterOnly) {
						yoff = (holediam-tooldiam)/2.0;
						tGCode.append(moveXY(rp.x,  rp.y-yoff,  "G0", stXYRapid));
					}
					else {
						tGCode.append(moveXY(rp.x,  rp.y,  "G0", stXYRapid));
					}
					Double cz = sz;
					
					for (int i=0; i<passes; i++) {
						cz = cz - passdepth;
						if (cz < -depth)
							cz = -depth;
						
						if (holediam <= tooldiam) {
							tGCode.append(moveZ(cz,  "G1",  stZNormal));
						}
						else if (!holePerimeterOnly) {
							tGCode.append(moveZ(cz,  "G1",  stZNormal));
							maxyoff = (holediam-tooldiam)/2.0;
							yoff = stepover;
							while (true) {
								if (yoff > maxyoff)
									yoff = maxyoff;
								tGCode.append("G1 Y" + fmtDbl(rp.y-yoff) + "G1" + stXYNormal + "\n");
								tGCode.append(cmd + " J" + fmtDbl(yoff) + " X" + fmtDbl(rp.x) +
										" Y" + fmtDbl(rp.y - yoff) + stXYNormal + "\n");
								if (yoff >= maxyoff)
									break;
								yoff += stepover;
							}
								
							tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));
						}
						else { // hole perimeter only
							yoff = (holediam-tooldiam)/2.0;
							tGCode.append(moveZ(cz,  "G1",  stZNormal));
							tGCode.append(cmd + " J" + fmtDbl(yoff) + " X" + fmtDbl(rp.x) +
									" Y" + fmtDbl(rp.y - yoff) + stXYNormal + "\n");			
						}
							
						if (retract)
							tGCode.append(moveZ(safez,  "G0",  stZRapid));
					}
					
					
					if (!retract)
						tGCode.append(moveZ(safez,  "G0",  stZRapid));
				}

				cx = cx + xstep;
			}
			cy = cy + ystep;
			if (staggerRows && iy%2 == 0)
				cx = minx + xstep/2.0;
			else
				cx = minx;
		}
	
		tGCode.append(moveZ(safez,  "G0",  stZRapid));
		tGCode.append("(End object " + objectName + ")\n");
		
		tGCode.setTopIndex(0);
		bView.setEnabled(true);
		bSave.setEnabled(true);
		haveGCode = true;
		GCStale = false;
		unsaved = true;
		setModified(false);
	}
		
	private void JSONInit() {
		
        JSONText.put("height",  tHeight);
        JSONText.put("width",  tWidth);
        JSONText.put("startx",  tStartX);
        JSONText.put("starty",  tStartY);
        JSONText.put("startz",  tStartZ);
        JSONText.put("safez",  tSafeZ);
        JSONText.put("angle", tAngle);
        JSONText.put("holediam", tHoleDiam);
        JSONText.put("spacing", tSpacing);
        JSONText.put("depth",  tDepth);
        JSONText.put("passdepth",  tPassDepth);
        JSONText.put("tooldiam",  tToolDiam);
        JSONText.put("stepover",  tStepOver);
        JSONText.put("xynormalspeed",  tXYNormalSpeed);
        JSONText.put("xyrapidspeed",  tXYRapidSpeed);
        JSONText.put("znormalspeed",  tZNormalSpeed);
        JSONText.put("zrapidspeed",  tZRapidSpeed);	
        
		JSONAllKeys.clear();
		JSONAllKeys.add("addspeedterm");
		JSONAllKeys.add("startingpoint");
		JSONAllKeys.add("cutdirection");
		JSONAllKeys.add("inside");
		JSONAllKeys.add("perimeteronly");
		JSONAllKeys.add("holeperimeteronly");
		JSONAllKeys.add("stagger");
		JSONAllKeys.add("retract");
		JSONAllKeys.addAll(JSONText.keySet());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void serialize() {
		JSONObject objData = new JSONObject();
		
		Iterator hmIterator = JSONText.entrySet().iterator(); 
	       
		while (hmIterator.hasNext()) { 
			Map.Entry mapElement = (Map.Entry)hmIterator.next(); 
			String key = (String) mapElement.getKey();
			Text txt = (Text) mapElement.getValue();
			objData.put(key, txt.getText());
		}

		objData.put("addspeedterm", addSpeedTerm);

		objData.put("startingpoint", startingPoint);
		objData.put("cutdirection", cutDirection);
		objData.put("inside", inside);
		objData.put("stagger", stagger);
		objData.put("perimeteronly", perimeterOnly);
		objData.put("holeperimeteronly", holePerimeterOnly);
		objData.put("retract", retract);
		
		saveJSONFile(objData);
	}
	
	@SuppressWarnings("rawtypes")
	private void deserialize() {
		JSONObject objData = retrieveJSONFile(JSONAllKeys);
		if (objData == null)
			return;
        
		Iterator hmIterator = JSONText.entrySet().iterator(); 
	       
		while (hmIterator.hasNext()) { 
			Map.Entry mapElement = (Map.Entry)hmIterator.next(); 
			String key = (String) mapElement.getKey();
			Text txt = (Text) mapElement.getValue();
			txt.setText((String) objData.get(key));
		}
        
        selectStartingPoint((String) objData.get("startingpoint"));
        selectCutDirection((String) objData.get("cutdirection"));
        
        boolean b = (boolean) objData.get("addspeedterm");
        if (b != addSpeedTerm) {
        	addSpeedTerm = b;
        	props.setPropAddSpeed(addSpeedTerm);
        	bAddSpeed.setSelection(addSpeedTerm);
        	enableSpeedControls();
        }

        inside = (boolean) objData.get("inside");
        bInside.setSelection(inside);
        
        perimeterOnly = (boolean) objData.get("perimeteronly");
        bPerimeterOnly.setSelection(perimeterOnly);
        
        holePerimeterOnly = (boolean) objData.get("holeperimeteronly");
        bHolePerimeterOnly.setSelection(holePerimeterOnly);
        
        stagger = (boolean) objData.get("stagger");
        bStagger.setSelection(stagger);
        
        retract = (boolean) objData.get("retract");
        bRetract.setSelection(retract);

		GCStale = haveGCode;
		setModified(false);
	}
}
