package main.java.jcnc.contours;

import java.util.ArrayList;
import java.util.Collections;
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
import main.java.jcnc.Rotator;

public class Rectangle extends CncDialog{
	Text tHeight;
	Text tWidth;
	Text tStartX;
	Text tStartY;
	Text tStartZ;
	Text tSafeZ;
	Text tAngle;
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
	
	String toolMovement = null;
	HashMap<String, Button> hmToolMovement = new HashMap<String, Button>();
	String startingPoint = null;
	HashMap<String, Button> hmStartingPoint = new HashMap<String, Button>();
	String cutDirection = null;
	HashMap<String, Button> hmCutDirection = new HashMap<String, Button>();
	String pocket = null;
	HashMap<String, Button> hmPocket = new HashMap<String, Button>();

	public Rectangle(Shell parent, CncProperties props) {
		super(parent, props);
		objectName = "Rectangle Contour " + seqNo;
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

		Group tm = addToolMovement();
		gd = (GridData) tm.getLayoutData();
		gd.horizontalSpan = 2;
		tm.setLayoutData(gd);

		Group cd = addCutDirection();
		gd = (GridData) cd.getLayoutData();
		gd.horizontalSpan = 2;
		cd.setLayoutData(gd);

		Group pkt = addPocket();
		gd = (GridData) pkt.getLayoutData();
		gd.horizontalSpan = 2;
		pkt.setLayoutData(gd);

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

	private Group addToolMovement() {
		hmToolMovement.clear();
		SelectionAdapter bToolMovementPress = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Button b = (Button) event.getSource();
				if (b.getSelection()) {
					setModified(true);
					toolMovement = b.getText();
				}
			}
		};
		Group c = new Group(shell, SWT.NONE);
		c.setText(" Tool Movement ");
		c.setLayout(new GridLayout(1, false));
		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		String[] tags = {"On Rectangle", "Outside", "Inside"};

		for (String k : tags) {
			Button b = new Button(c, SWT.RADIO);
			b.setText(k);
			b.addSelectionListener(bToolMovementPress);
			hmToolMovement.put(k, b);			
		}
		selectToolMovement(tags[0]);

		return c;
	}
	
	private void selectToolMovement(String k) {
		if (!hmToolMovement.containsKey(k))
			return;

		Button b;
		
		if (toolMovement != null) {
			b = hmToolMovement.get(toolMovement);
			if (b != null)
				b.setSelection(false);
		}
		
		b = hmToolMovement.get(k);
		b.setSelection(true);
		toolMovement = k;
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

	private Group addPocket() {
		hmPocket.clear();
		SelectionAdapter bPocketPress = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Button b = (Button) event.getSource();
				if (b.getSelection()) {
					setModified(true);
					pocket = b.getText();
				}
			}
		};
		Group c = new Group(shell, SWT.NONE);
		c.setText(" Pocket ");
		c.setLayout(new GridLayout(1, false));
		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		String[] tags = {"None", "Horizontal", "Vertical", "Centered"};

		for (String k : tags) {
			Button b = new Button(c, SWT.RADIO);
			b.setText(k);
			b.addSelectionListener(bPocketPress);
			hmPocket.put(k, b);			
		}
		selectPocket(tags[0]);

		return c;
	}
	
	private void selectPocket(String k) {
		if (!hmPocket.containsKey(k))
			return;

		Button b;
		
		if (pocket != null) {
			b = hmPocket.get(pocket);
			if (b != null)
				b.setSelection(false);
		}
		
		b = hmPocket.get(k);
		b.setSelection(true);
		pocket = k;
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
		tGCode.append("(Rectangle " + fmtDbl(width) + ", " + fmtDbl(height) + " ");
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
		tGCode.append("(Tool movement: " + toolMovement + ")\n");
		tGCode.append("(Starting point: " + startingPoint + ")\n");
		tGCode.append("(Cutting Direction: " + cutDirection + ")\n");
		tGCode.append("(Pocket type: " + pocket + ")\n");

		tGCode.append("G90\n");
		if (metric)
			tGCode.append("G21 (Metric)\n");
		else
			tGCode.append("G20 (Imperial)\n");
		
	
		List<Point2D> points = new ArrayList<Point2D>();
		points.clear();
			
		Double adjx = 0.0;
		Double adjy = 0.0;
		
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
		
		Double adjRad = 0.0;
		if (toolMovement.equals("Inside"))
			adjRad = -tooldiam/2.0;
		else if (toolMovement.equals("Outside"))
			adjRad = tooldiam/2.0;
		
		points.add(new Point2D(sx+adjx-adjRad, sy+adjy-adjRad));
		points.add(new Point2D(sx+adjx-adjRad, sy+height+adjy+adjRad));
		points.add(new Point2D(sx+width+adjx+adjRad, sy+height+adjy+adjRad));
		points.add(new Point2D(sx+width+adjx+adjRad, sy+adjy-adjRad));
		points.add(new Point2D(sx+adjx-adjRad, sy+adjy-adjRad));
							

		if (!cutDirection.equals("Clockwise"))
			Collections.reverse(points);
		
		Double xmin = Math.min(points.get(0).x, points.get(2).x) + tooldiam/2.0;
		Double xmax = Math.max(points.get(0).x, points.get(2).x) - tooldiam/2.0;
		Double ymin = Math.min(points.get(0).y, points.get(2).y) + tooldiam/2.0;
		Double ymax = Math.max(points.get(0).y, points.get(2).y) - tooldiam/2.0;
		
		Double cz = sz;
		Double xlast = 0.0;
		Double ylast = 0.0;
		tGCode.append(moveZ(safez,  "G0",  stZRapid));
		Point2D rp = rot.rotate(points.get(0));
		tGCode.append(moveXY(rp.x,  rp.y,  "G0",  stXYRapid));
		for (int i=0; i<passes; i++) {
			cz = cz - passdepth;
			if (cz < -depth)
				cz = -depth;
			tGCode.append("(Pass number " + (i+1) + "/" + passes + " at depth " + fmtDbl(cz) + ")\n");

			tGCode.append(moveZ(cz,  "G1",  stZNormal));
			
			for (Point2D p : points) {
				rp = rot.rotate(p);
				tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));
			}

				
			if (pocket.equals("Horizontal")) {
				boolean first = true;
				boolean alt = true;
				Double y = ymin;
				tGCode.append(moveZ(safez,  "G0",  stZRapid));
				rp = rot.rotate(new Point2D(xmin, ymin));
				tGCode.append(moveXY(rp.x,  rp.y,  "G0",  stXYRapid));
				
				tGCode.append(moveZ(cz,  "G1",  stZNormal));
				while (y <= ymax) {
					if (!first) {
						rp = rot.rotate(new Point2D(xlast, y));
						tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));
					}
						
					if (alt) {
						rp = rot.rotate(new Point2D(xmax, y));
						tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));
						xlast = xmax;
					}
					else {
						rp = rot.rotate(new Point2D(xmin, y));
						tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));
						xlast = xmin;
					}
					y = y + tooldiam * stepover;
					first = false;
					alt = !alt;
				}
				
				tGCode.append(moveZ(safez,  "G0",  stZRapid));
				rp = rot.rotate(points.get(0));
				tGCode.append(moveXY(rp.x,  rp.y,  "G0",  stXYRapid));
			}
			
			else if (pocket.equals("Vertical")) {
				boolean first = true;
				boolean alt = true;
				Double x = xmin;
				tGCode.append(moveZ(safez,  "G0",  stZRapid));
				rp = rot.rotate(new Point2D(xmin, ymin));
				tGCode.append(moveXY(rp.x,  rp.y,  "G0",  stXYRapid));
				
				tGCode.append(moveZ(cz,  "G1",  stZNormal));
				while (x <= xmax) {
					if (!first) {
						rp = rot.rotate(new Point2D(x, ylast));
						tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));
					}
						
					if (alt) {
						rp = rot.rotate(new Point2D(x, ymax));
						tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));
						ylast = ymax;
					}
					else {
						rp = rot.rotate(new Point2D(x, ymin));
						tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));
						ylast = ymin;
					}
					x = x + tooldiam * stepover;
					first = false;
					alt = !alt;
				}
				
				tGCode.append(moveZ(safez,  "G0",  stZRapid));
				rp = rot.rotate(points.get(0));
				tGCode.append(moveXY(rp.x,  rp.y,  "G0",  stXYRapid));
			}
			
			else if (pocket.equals("Centered")) {
				boolean vertical = false;
				
				Double xa = (xmax+xmin)/2.0;
				Double xb = xa;
				Double ya = (ymax+ymin)/2.0;
				Double yb = ya;
				
				if ((xmax-xmin) > (ymax-ymin)) {
					ya = (ymax+ymin)/2.0;
					yb = ya;
					Double d = ymax - ya;
					xa = xmin + d;
					xb = xmax - d;
				}
				else if ((xmax-xmin) < (ymax-ymin)) {
					vertical = true;
					xa = (xmax+xmin)/2.0;
					xb = xa;
					Double d = xmax - xa;
					ya = ymin + d;
					yb = ymax - d;
				}
					
				tGCode.append(moveZ(safez,  "G0",  stZRapid));
				rp = rot.rotate(new Point2D(xb, yb));
				tGCode.append(moveXY(rp.x,  rp.y,  "G0",  stXYRapid));  
				tGCode.append(moveZ(cz,  "G1",  stZNormal));
				rp = rot.rotate(new Point2D(xa, ya));
				tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
				
				Double d = stepover * tooldiam;
				while ((xa-d) >= xmin) {
					if (cutDirection.equals("Clockwise")) {
						rp = rot.rotate(new Point2D(xa-d, ya-d));
						tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
						
						if (vertical) {
							rp = rot.rotate(new Point2D(xa-d, yb+d));
							tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
						}
						else {
							rp = rot.rotate(new Point2D(xa-d, ya+d));
							tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
						}
						
						rp = rot.rotate(new Point2D(xb+d, yb+d));
						tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
						
						if (vertical) {
							rp = rot.rotate(new Point2D(xb+d, ya-d));
							tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
						}
						else {
							rp = rot.rotate(new Point2D(xb+d, yb-d));
							tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
						}
						
						rp = rot.rotate(new Point2D(xa-d, ya-d));
						tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
					}
					else {
						rp = rot.rotate(new Point2D(xa-d, ya-d));
						tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
						
						if (vertical) {
							rp = rot.rotate(new Point2D(xb+d, ya-d));
							tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
						}
						else {
							rp = rot.rotate(new Point2D(xb+d, yb-d));
							tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
						}
						
						rp = rot.rotate(new Point2D(xb+d, yb+d));
						tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
						
						if (vertical) {
							rp = rot.rotate(new Point2D(xa-d, yb+d));
							tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
						}
						else {
							rp = rot.rotate(new Point2D(xa-d, ya+d));
							tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
						}
							
						rp = rot.rotate(new Point2D(xa-d, ya-d));
						tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));  
					}
					d = d + stepover * tooldiam;
				}
				tGCode.append(moveZ(safez,  "G0",  stZRapid));
				rp = rot.rotate(points.get(0));
				tGCode.append(moveXY(rp.x,  rp.y,  "G0",  stXYRapid));  
			}
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
		JSONAllKeys.add("toolmovement");
		JSONAllKeys.add("startingpoint");
		JSONAllKeys.add("cutdirection");
		JSONAllKeys.add("pocket");
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

		objData.put("toolmovement", toolMovement);
		objData.put("startingpoint", startingPoint);
		objData.put("cutdirection", cutDirection);
		objData.put("pocket", pocket);
		
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
        
        selectToolMovement((String) objData.get("toolmovement"));
        selectStartingPoint((String) objData.get("startingpoint"));
        selectCutDirection((String) objData.get("cutdirection"));
        selectPocket((String) objData.get("pocket"));
        
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
