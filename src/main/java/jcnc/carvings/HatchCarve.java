package main.java.jcnc.carvings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

public class HatchCarve extends CncDialog {
	Text tHeight;
	Text tWidth;
	Text tGap;
	Text tAngle;
	Text tYOffset;
	Text tStartX;
	Text tStartY;
	Text tStartZ;
	Text tSafeZ;
	Text tDepth;
	Text tPassDepth;
	Text tToolDiam;

	Text tGCode;

	Button bGenerate;
	Button bView;
	Button bSave;
	Button bSerialize;
	Button bDeserialize;
	
	Button bBorder;
	boolean border;
	
	String startingPoint = null;
	HashMap<String, Button> hmStartingPoint = new HashMap<String, Button>();

	public HatchCarve(Shell parent, CncProperties props) {
		super(parent, props);
		objectName = "Diamond Carve Pattern " + seqNo;
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

		addLabel("Perpendicular Gap: ");
		tGap = addDoubleText(10.0F);

		addLabel("Angle: ");
		tAngle = addDoubleText(0.0F);

		addLabel("Offset(Y): ");
		tYOffset = addDoubleText(0.0F);
		GridData gd = (GridData) tYOffset.getLayoutData();
		gd.horizontalSpan = 3;
		tYOffset.setLayoutData(gd);

		addLabel("Start X: ");
		tStartX = addDoubleText(0.0F);

		addLabel("Start Y: ");
		tStartY = addDoubleText(0.0F);

		addLabel("Start Z: ");
		tStartZ = addDoubleText(0.0F);

		addLabel("Safe Z: ");
		tSafeZ = addDoubleText(10.0F);

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
		gd = (GridData) tToolDiam.getLayoutData();
		gd.horizontalSpan = 3;
		tToolDiam.setLayoutData(gd);

		Group sp = addStartingPoint();
		gd = (GridData) sp.getLayoutData();
		gd.horizontalSpan = 2;
		sp.setLayoutData(gd);
		
		bBorder = new Button(shell, SWT.CHECK);
		bBorder.setText("Border");
		bBorder.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				border = bBorder.getSelection();
				setModified(true);
			}
		});
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.CENTER;
		bBorder.setLayoutData(gd);
		border = false;
		bBorder.setSelection(border);

		
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

		// tGCode = new Text(shell, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER |
		// SWT.H_SCROLL | SWT.V_SCROLL);
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

	private void generateGCode() {
		bView.setEnabled(false);
		bSave.setEnabled(false);
		haveGCode = false;
		GCStale = false;
		unsaved = false;
		setTitle();

		double height = Double.valueOf(tHeight.getText());
		double width = Double.valueOf(tWidth.getText());
		double gap = Double.valueOf(tGap.getText());
		double angle = Double.valueOf(tAngle.getText());
		double yoffset = Double.valueOf(tYOffset.getText());
		double sx = Double.valueOf(tStartX.getText());
		double sy = Double.valueOf(tStartY.getText());
		double sz = Double.valueOf(tStartZ.getText());
		double safez = Double.valueOf(tSafeZ.getText());
		double depth = Double.valueOf(tDepth.getText());
		double passdepth = Double.valueOf(tPassDepth.getText());
		double tooldiam = Double.valueOf(tToolDiam.getText());

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
		
		tGCode.append("(" + objectName + ")\n");
		tGCode.append("(Diamond Carve Pattern " + fmtDbl(width) + ", " + fmtDbl(height) + " ");
		tGCode.append("starting at " + fmtDbl(sx) + ", " + fmtDbl(sy) + ")\n");
		tGCode.append("(gap: " + fmtDbl(gap) + "  angle: " + fmtDbl(angle) + "  offset: " + fmtDbl(yoffset) + ")\n");
		tGCode.append("(depth from " + fmtDbl(sz) + " to -" + fmtDbl(depth) + ")\n");
		if (tooldiam == (Double) toolInfo.get("diameter")) {
			tGCode.append("(Tool " + (String) toolInfo.get("name") + " diameter: " + fmtDbl(tooldiam));
		}
		else {
			tGCode.append("(Tool diameter " + fmtDbl(tooldiam));
		}
		tGCode.append("   depth/pass -" + fmtDbl(passdepth) + ")\n");
		tGCode.append("(Starting point: " + startingPoint + ")\n");
		tGCode.append("(Border: " + border + ")\n");

		tGCode.append("G90\n");
		if (metric)
			tGCode.append("G21 (Metric)\n");
		else
			tGCode.append("G20 (Imperial)\n");
		

		
		List<Point2D> plist = hatch(angle, new Point2D(sx, sy), new Point2D(sx+width, sy+height), gap, yoffset);
				
		double adjx = 0.0;
		double adjy = 0.0;
		
		if (startingPoint.equals("Upper Left")) {
			adjy = -height;
		}
		else if (startingPoint.equals("Upper Right")) {
			adjy = -height;
			adjx = -width;
		}
		else if (startingPoint.equals("Lower Right")) {
			adjx = -width;
		}
		else if (startingPoint.equals("Center")) {
			adjx = -width/2.0;
			adjy = -height/2.0;
		}

		for (int j = 0; j<plist.size(); j = j + 2) {
			Point2D p0 = plist.get(j);
			Point2D p1 = plist.get(j+1);
			
			p0.x = p0.x + adjx;
			p0.y = p0.y + adjy;
			p1.x = p1.x + adjx;
			p1.y = p1.y + adjy;
			
			plist.set(j,  p0);
			plist.set(j+1,  p1);
		}

		Point2D corner0 = new Point2D(sx+adjx, sy+adjy);
		Point2D corner1 = new Point2D(sx+adjx, sy+height+adjy);
		Point2D corner2 = new Point2D(sx+width+adjx, sy+height+adjy);
		Point2D corner3 = new Point2D(sx+width+adjx, sy+adjy);

		double cz = sz;
		boolean alt = true;
		Point2D pa, pb;
		for (int i = 0; i < passes; i++) {
			cz = cz -passdepth;
			if (cz < -depth)
				cz = -depth;
			
			tGCode.append("(Pass number " + (i+1) + "/" + passes + " at depth " + fmtDbl(cz) + ")\n");
			
			tGCode.append(moveZ(safez, "G0", stZRapid));
			for (int j = 0; j<plist.size(); j = j + 2) {
				if (alt) {
					pa = plist.get(j);
					pb = plist.get(j+1);
				}
				else {
					pa = plist.get(j+1);
					pb = plist.get(j);
				}
				alt = !alt;
					
				tGCode.append(moveXY(pa.x, pa.y, "G0", stXYRapid));
				tGCode.append(moveZ(cz, "G1", stZNormal));
				tGCode.append(moveXY(pb.x, pb.y, "G1", stXYNormal));
				tGCode.append(moveZ(safez, "G0", stZRapid));
			}
			
			if (border) {
				tGCode.append(moveXY(corner0.x, corner0.y, "G0", stXYRapid));
				tGCode.append(moveZ(cz, "G1", stZNormal));
				tGCode.append(moveXY(corner1.x, corner1.y, "G1", stXYNormal));
				tGCode.append(moveXY(corner2.x, corner2.y, "G1", stXYNormal));
				tGCode.append(moveXY(corner3.x, corner3.y, "G1", stXYNormal));
				tGCode.append(moveXY(corner0.x, corner0.y, "G1", stXYNormal));
				tGCode.append(moveZ(safez, "G0", stZRapid));
			}
		}

		tGCode.append(moveXY(sx, sy, "G0", stXYRapid));
				
		tGCode.append("(End object " + objectName + ")\n");
		
		tGCode.setTopIndex(0);
		bView.setEnabled(true);
		bSave.setEnabled(true);
		haveGCode = true;
		GCStale = false;
		unsaved = true;
		setModified(false);
	}
		
	private List<Point2D> hatch(double angle, Point2D minp, Point2D maxp, double gap, double yoffset) {
		List<Point2D> plist = new ArrayList<Point2D>();
		
		double rise, run, slope;
		
		if (angle == 0) {
			slope = 0;
			rise = gap;
		}
		else {
			run = gap / Math.sin(Math.toRadians(angle));
			rise = Math.tan(Math.toRadians(angle)) * run;
			slope = rise / run;
		}
		
		// calculate the min and max intercept
		double mini = minp.y - slope*minp.x;
		double maxi = minp.y - slope*minp.x;
		
		double v = minp.y -slope*maxp.x;
		if (v < mini) mini = v;
		if (v > maxi) maxi = v;
		
		v = maxp.y - slope*minp.x;
		if (v < mini) mini = v;
		if (v > maxi) maxi = v;
		
		v = maxp.y - slope*maxp.x;
		if (v < mini) mini = v;
		if (v > maxi) maxi = v;

		if (yoffset < 0) {
			maxi = maxi + Math.abs(rise) + yoffset;
			mini = mini + yoffset;
		}
		else if (yoffset > 0) {
			mini = mini - Math.abs(rise) + yoffset;
			maxi = maxi + yoffset;
		}
		
		double intercept = mini;
		plist.clear();
		List<Point2D> p = new ArrayList<Point2D>();
		
		while (intercept <= maxi) {
			p.clear();
			
			double y = slope * minp.x + intercept;
			if (y >= minp.y & y <= maxp.y)
				p.add(new Point2D(minp.x, y));
				
			double x = (minp.y - intercept) / slope;
			if (x >= minp.x && x <= maxp.x)
				p.add(new Point2D(x, minp.y));
			
			x = (maxp.y - intercept) / slope;
			if (x >= minp.x && x <= maxp.x)
				p.add(new Point2D(x, maxp.y));
			
			y = slope * maxp.x + intercept;
			if (y >= minp.y && y <= maxp.y)
				p.add(new Point2D(maxp.x, y));
				
			if (p.size() == 2) {
				Point2D p0 = p.get(0);
				Point2D p1 = p.get(1);
				if (p0.x != p1.x || p0.y != p1.y) {
					if (p1.x < p0.x) {
						plist.add(p1);
						plist.add(p0);
					}
					else {
						plist.add(p0);
						plist.add(p1);						
					}
				}
			}
			
			intercept  = intercept + Math.abs(rise);
		}			
		return plist;
	}

	private void JSONInit() {
        JSONText.put("height",  tHeight);
        JSONText.put("width",  tWidth);
        JSONText.put("gap",  tGap);
        JSONText.put("angle",  tAngle);
        JSONText.put("yoffset",  tYOffset);
        JSONText.put("startx",  tStartX);
        JSONText.put("starty",  tStartY);
        JSONText.put("startz",  tStartZ);
        JSONText.put("safez",  tSafeZ);
        JSONText.put("depth",  tDepth);
        JSONText.put("passdepth",  tPassDepth);
        JSONText.put("tooldiam",  tToolDiam);
        JSONText.put("xynormalspeed",  tXYNormalSpeed);
        JSONText.put("xyrapidspeed",  tXYRapidSpeed);
        JSONText.put("znormalspeed",  tZNormalSpeed);
        JSONText.put("zrapidspeed",  tZRapidSpeed);	
        
		JSONAllKeys.clear();
		JSONAllKeys.add("addspeedterm");
		JSONAllKeys.add("startingpoint");
		JSONAllKeys.add("border");
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
		objData.put("border", border);

		objData.put("startingpoint", startingPoint);
		
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
        
        border = (boolean) objData.get("border");
        bBorder.setSelection(border);
        
        boolean b = (boolean) objData.get("addspeedterm");
        if (b != addSpeedTerm) {
        	addSpeedTerm = b;
        	props.setPropAddSpeed(addSpeedTerm);
        	bAddSpeed.setSelection(addSpeedTerm);
        	enableSpeedControls();
        }

		GCStale = haveGCode;
		setModified(false);
	}

}
