package main.java.jcnc.contours;

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

/**
 * This class demonstrates how to create your own dialog classes. It allows
 * users to input a String
 */
public class Line extends CncDialog {
	Text tStartX;
	Text tStartY;
	Text tEndX;
	Text tEndY;
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
	
	String toolMovement = null;
	HashMap<String, Button> hmToolMovement = new HashMap<String, Button>();

	public Line(Shell parent, CncProperties props) {
		super(parent, props);
		objectName = "Line Contour " + seqNo;
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

		addLabel("Start X: ");
		tStartX = addDoubleText(0.0F);

		addLabel("Start Y: ");
		tStartY = addDoubleText(0.0F);

		addLabel("End X: ");
		tEndX = addDoubleText(10.0F);

		addLabel("End Y: ");
		tEndY = addDoubleText(10.0F);

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
		GridData gd = new GridData();
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

		Group tm = addToolMovement();
		gd = (GridData) tm.getLayoutData();
		gd.horizontalSpan = 2;
		tm.setLayoutData(gd);

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
		
		String[] tags = {"Center", "Right of forward motion", "Left of forward motion"};

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

	private void generateGCode() {
		bView.setEnabled(false);
		bSave.setEnabled(false);
		haveGCode = false;
		GCStale = false;
		unsaved = false;
		setTitle();

		double sx = Double.valueOf(tStartX.getText());
		double sy = Double.valueOf(tStartY.getText());
		double ex = Double.valueOf(tEndX.getText());
		double ey = Double.valueOf(tEndY.getText());
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

		double rise = ey - sy;
		double run = ex - sx;

		int sign = 1;
		if (run < 0)
			sign = -1;

		double trise;
		double trun;
		if (run == 0) {
			trise = 0;
			if (rise > 0)
				trun = -tooldiam / 2F;
			else
				trun = tooldiam / 2F;
		} else {
			double angler = Math.atan(rise / run);
			trise = sign * Math.cos(angler) * tooldiam / 2F;
			trun = sign * (-(Math.sin(angler) * tooldiam / 2F));
		}

		if (toolMovement.equals("Left of forward motion")) {
			sx += trun;
			ex += trun;
			sy += trise;
			ey += trise;
		} else if (toolMovement.equals("Right of forward motion")) {
			sx -= trun;
			ex -= trun;
			sy -= trise;
			ey -= trise;
		}

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
		tGCode.append(
				"(Line " + fmtDbl(sz) + ", " + fmtDbl(sy) + " to " + fmtDbl(ex));
		tGCode.append(", " + fmtDbl(ey) + " depth from " + fmtDbl(sz) + " to -"
				+ fmtDbl(depth) + ")\n");
		if (tooldiam == (Double) toolInfo.get("diameter")) {
			tGCode.append("(Tool " + (String) toolInfo.get("name") + " diameter: " + fmtDbl(tooldiam));
		}
		else {
			tGCode.append("(Tool diameter " + fmtDbl(tooldiam));
		}
		tGCode.append("   depth/pass -" + fmtDbl(passdepth) + ")\n");
		tGCode.append("(Tool movement: " + toolMovement + ")\n");

		tGCode.append("G90\n");
		if (metric)
			tGCode.append("G21 (Metric)\n");
		else
			tGCode.append("G20 (Imperial)\n");

		tGCode.append(moveZ(safez,  "G0",  stZRapid));

		double cz = sz;
		for (int i = 0; i < passes; i++) {

			cz -= passdepth;
			if (cz < -depth)
				cz = -depth;

			tGCode.append("(Pass number " + (i + 1) + "/" + passes + " at depth " + fmtDbl(cz) + ")\n");

			tGCode.append(moveXY(sx, sy, "G0", stXYRapid));
			tGCode.append(moveZ(cz, "G1", stZNormal));
			tGCode.append(moveXY(ex, ey, "G1", stXYNormal));
			tGCode.append(moveZ(safez, "G0", stZRapid));
		}
		tGCode.append("(End object " + objectName + ")\n");
		
		tGCode.setTopIndex(0);
		bView.setEnabled(true);
		bSave.setEnabled(true);
		haveGCode = true;
		unsaved = true;
		GCStale = false;
		setModified(false);
	}

	private void JSONInit() {
        JSONText.clear();
        JSONText.put("startx",  tStartX);
        JSONText.put("starty",  tStartY);
        JSONText.put("endx",  tEndX);
        JSONText.put("endy",  tEndY);
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
		JSONAllKeys.add("toolmovement");
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
