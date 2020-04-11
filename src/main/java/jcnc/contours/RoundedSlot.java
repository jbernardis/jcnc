package main.java.jcnc.contours;

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
import main.java.jcnc.Rotator;

public class RoundedSlot extends CncDialog {
	Text tDiameter;
	Text tLength;
	Text tAngle;
	Text tCenterX;
	Text tCenterY;
	Text tStartZ;
	Text tSafeZ;
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

	Button bPocket;
	boolean pocket;
	
	String toolMovement = null;
	HashMap<String, Button> hmToolMovement = new HashMap<String, Button>();
	String cutDirection = null;
	HashMap<String, Button> hmCutDirection = new HashMap<String, Button>();
	
	String stXYNormal;
	String stXYRapid;
	String stZNormal;
	String stZRapid;

	public RoundedSlot(Shell parent, CncProperties props) {
		super(parent, props);
		objectName = "Rounded Slot Contour " + seqNo;
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

		addLabel("Diameter: ");
		tDiameter = addDoubleText(10.0F);
		
		addLabel("Length: ");
		tLength = addDoubleText(100.0F);
		
		addLabel("Angle: ");
		tAngle = addDoubleText(0.0F);
		GridData gd = (GridData) tAngle.getLayoutData();
		gd.horizontalSpan = 3;
		tAngle.setLayoutData(gd);

		addLabel("Center X: ");
		tCenterX = addDoubleText(0.0F);

		addLabel("Center Y: ");
		tCenterY = addDoubleText(0.0F);

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

		addLabel("Step Over: ");
		tStepOver = addDoubleText((Double) toolMaterialInfo.get("stepover"));

		Group tm = addToolMovement();
		gd = (GridData) tm.getLayoutData();
		gd.horizontalSpan = 2;
		tm.setLayoutData(gd);

		Group cd = addCutDirection();
		gd = (GridData) cd.getLayoutData();
		gd.horizontalSpan = 2;
		cd.setLayoutData(gd);
		
		bPocket = new Button(shell, SWT.CHECK);
		bPocket.setText("Pocket");
		bPocket.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				pocket = bPocket.getSelection();
				setModified(true);
			}
		});
		gd = new GridData();
		gd.horizontalSpan = 4;
		gd.horizontalAlignment = SWT.CENTER;
		bPocket.setLayoutData(gd);
		pocket = false;
		bPocket.setSelection(pocket);

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
		
		String[] tags = {"On Slot", "Outside", "Inside"};

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

		double diameter = Double.valueOf(tDiameter.getText());
		double length = Double.valueOf(tLength.getText());
		double angle = Double.valueOf(tAngle.getText());
		double cx = Double.valueOf(tCenterX.getText());
		double cy = Double.valueOf(tCenterY.getText());
		double sz = Double.valueOf(tStartZ.getText());
		double safez = Double.valueOf(tSafeZ.getText());
		double depth = Double.valueOf(tDepth.getText());
		double passdepth = Double.valueOf(tPassDepth.getText());
		double tooldiam = Double.valueOf(tToolDiam.getText());
		double stepover = Double.valueOf(tStepOver.getText());

		double XYNormal = Double.valueOf(tXYNormalSpeed.getText());
		double XYRapid = Double.valueOf(tXYRapidSpeed.getText());
		double ZNormal = Double.valueOf(tZNormalSpeed.getText());
		double ZRapid = Double.valueOf(tZRapidSpeed.getText());
		
		tGCode.setText("");
		
		Rotator rot = new Rotator(angle);

		stXYNormal = "";
		stXYRapid = "";
		stZNormal = "";
		stZRapid = "";

		if (addSpeedTerm) {
			stXYNormal = " F" + fmtDbl(XYNormal);
			stXYRapid = " F" + fmtDbl(XYRapid);
			stZNormal = " F" + fmtDbl(ZNormal);
			stZRapid = " F" + fmtDbl(ZRapid);
		}
		
		boolean metric = props.getPropMetric();

		int passes = (int) Math.ceil(depth / passdepth);

		tGCode.append("(" + objectName + ")\n");
		tGCode.append("(Rounded Slot   Diameter: " + fmtDbl(diameter) + " ");
		tGCode.append("center at " + fmtDbl(cx) + ", " + fmtDbl(cy) + " ");
		tGCode.append("depth from " + fmtDbl(sz) + " to -" + fmtDbl(depth) + ")\n");
		tGCode.append("(Length: " + fmtDbl(length) + "  Rotation Angle: " + fmtDbl(angle) + ")\n");
		
		if (tooldiam == (Double) toolInfo.get("diameter")) {
			tGCode.append("(Tool " + (String) toolInfo.get("name") + " diameter: " + fmtDbl(tooldiam));
		}
		else {
			tGCode.append("(Tool diameter " + fmtDbl(tooldiam));
		}
		tGCode.append("   stepover " + fmtDbl(stepover) + " ");
		tGCode.append("   depth/pass -" + fmtDbl(passdepth) + ")\n");
		tGCode.append("(Tool movement: " + toolMovement + ")\n");
		tGCode.append("(Cutting Direction: " + cutDirection + ")\n");
		if (pocket)
			tGCode.append("(Pocket: Yes)\n");
		else
			tGCode.append("(Pocket: None)\n");

		tGCode.append("G90\n");
		if (metric)
			tGCode.append("G21 (Metric)\n");
		else
			tGCode.append("G20 (Imperial)\n");
		
		boolean clockwise = false;
		if (cutDirection.equals("Clockwise"))
			clockwise = true;;

		double rad = tooldiam/2.0;
		double radAdj = 0.0;
		if (toolMovement.equals("Inside")) {
			radAdj = rad;
		}
		else if (toolMovement.equals("Inside")) {
			radAdj = -rad;
		}
		
		List<Point2D> points = new ArrayList<Point2D>();
		points.clear();
		double sy = cy - diameter/2.0;
		points.add(new Point2D(cx, sy + radAdj));
		points.add(new Point2D(cx, sy + diameter - radAdj));
		points.add(new Point2D(cx + length, sy + diameter - radAdj));
		points.add(new Point2D(cx + length, sy + radAdj));
		
		List<Point2D> centers = new ArrayList<Point2D>();
		centers.clear();
		centers.add(new Point2D(cx, cy));
		centers.add(new Point2D(cx + length, cy));

		tGCode.append(moveZ(safez,  "G0",  stZRapid));
		
		double cz = sz;
		Point2D rp;
		for (int i=0; i<passes; i++) {
			cz = cz - passdepth;
			if (cz < -depth)
				cz = -depth;
			
			
			tGCode.append("(Pass number " + (i+1) + "/" + passes + " at depth " + fmtDbl(cz) + ")\n");

			
			if (pocket) {
				Point2D c0 = centers.get(0);
				rp = rot.rotate(centers.get(1));
				tGCode.append(moveXY(rp.x, rp.y, "G0", stXYRapid));
				tGCode.append(moveZ(cz,  "G1",  stZNormal));
				rp = rot.rotate(c0);
				tGCode.append(moveXY(rp.x, rp.y, "G1", stXYNormal));
				
				Point2D p = points.get(0);
				double d = c0.y - p.y;
				double cd = stepover * tooldiam;
				while (cd < d) {
					List<Point2D> ppoints = new ArrayList<Point2D>();
					ppoints.clear();
					Point2D pp = new Point2D(cx, cy - cd);
					ppoints.add(pp);
					ppoints.add(new Point2D(cx, cy + cd));
					ppoints.add(new Point2D(cx + length, cy + cd));
					ppoints.add(new Point2D(cx + length, cy -cd));
					
					rp = rot.rotate(pp);
					tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));
					cutLoop(ppoints, centers, clockwise, rot);
					cd = cd + stepover*tooldiam;
				}
				rp = rot.rotate(points.get(0));
				tGCode.append(moveXY(rp.x,  rp.y,  "G1",  stXYNormal));
			}
			else {
				rp = rot.rotate(points.get(0));
				tGCode.append(moveXY(rp.x, rp.y, "G0", stXYRapid));
				tGCode.append(moveZ(cz,  "G1",  stZNormal));
			}
			
			cutLoop(points, centers, clockwise, rot);
			if (pocket)
				tGCode.append(moveZ(safez,  "G0",  stZRapid));
		}

		if (!pocket)
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
	
	private void cutLoop(List<Point2D> points, List<Point2D> centers, boolean clockwise, Rotator rot) {
		String cmd = "G3";
		if (clockwise)
			cmd = "G2";
		
		Point2D p0 = rot.rotate(points.get(0));
		Point2D p1 = rot.rotate(points.get(1));
		Point2D p2 = rot.rotate(points.get(2));
		Point2D p3 = rot.rotate(points.get(3));
		
		Point2D c0 = rot.rotate(centers.get(0));
		Point2D c1 = rot.rotate(centers.get(1));

		if (clockwise) {
			tGCode.append(cmd + " I" + fmtDbl(c0.x-p0.x) + " J" + fmtDbl(c0.y-p0.y) +
					" X" + fmtDbl(p1.x)+ " Y" + fmtDbl(p1.y) + stXYNormal + "\n");
			
			tGCode.append(moveXY(p2.x,  p2.y,  "G1",  stXYNormal));
			
			tGCode.append(cmd + " I" + fmtDbl(c1.x-p2.x) + " J" + fmtDbl(c1.y-p2.y) +
					" X" + fmtDbl(p3.x)+ " Y" + fmtDbl(p3.y) + stXYNormal + "\n");
			
			tGCode.append(moveXY(p0.x,  p0.y,  "G1",  stXYNormal));
		}
		else {
			tGCode.append(moveXY(p3.x,  p3.y,  "G1",  stXYNormal));
			
			tGCode.append(cmd + " I" + fmtDbl(c1.x-p3.x) + " J" + fmtDbl(c1.y-p3.y) +
					" X" + fmtDbl(p2.x)+ " Y" + fmtDbl(p2.y) + stXYNormal + "\n");
			
			tGCode.append(moveXY(p1.x,  p1.y,  "G1",  stXYNormal));
			
			tGCode.append(cmd + " I" + fmtDbl(c0.x-p1.x) + " J" + fmtDbl(c0.y-p1.y) +
					" X" + fmtDbl(p0.x)+ " Y" + fmtDbl(p0.y) + stXYNormal + "\n");
		}
	}
	
	private void JSONInit() {
        JSONText.clear();
        JSONText.put("diameter",  tDiameter);
        JSONText.put("length",  tLength);
        JSONText.put("angle",  tAngle);
        JSONText.put("centerx",  tCenterX);
        JSONText.put("centery",  tCenterY);
        JSONText.put("startz",  tStartZ);
        JSONText.put("safez",  tSafeZ);
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
        selectCutDirection((String) objData.get("cutdirection"));
        
        pocket = (boolean) objData.get("pocket");
        bPocket.setSelection(pocket);
        
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