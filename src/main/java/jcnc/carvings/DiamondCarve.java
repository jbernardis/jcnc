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

public class DiamondCarve extends CncDialog {
	Text tHeight;
	Text tWidth;
	Text tSegments;
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

	public DiamondCarve(Shell parent, CncProperties props) {
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

		addLabel("Segments: ");
		tSegments = addIntText(10);
		GridData gd = (GridData) tSegments.getLayoutData();
		gd.horizontalSpan = 3;
		tSegments.setLayoutData(gd);

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
		int segments = Integer.valueOf(tSegments.getText());
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
		tGCode.append("(segments: " + segments + " depth from " + fmtDbl(sz) + " to -" + fmtDbl(depth) + ")\n");
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
		
		double sizex = width / segments;
		double sizey = height / segments;
			
		double adjx = 0.0F;
		double adjy = 0.0F;
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
			adjx = -width / 2.0;
			adjy = -height / 2.0;
		}
		
		List<Double> xvals = new ArrayList<Double>();
		List<Double> yvals = new ArrayList<Double>();
		xvals.clear();
		yvals.clear();
		
		for (int i = 0; i <= segments; i++) {	
			xvals.add(sx + i*sizex + adjx);
			yvals.add(sy + i*sizey + adjy);
		}

		List<Point2D> points = new ArrayList<Point2D>();
		points.clear();
		
		for (int i=1; i<segments; i++) {
			points.add(new Point2D(xvals.get(0), yvals.get(segments-i)));
			points.add(new Point2D(xvals.get(i), yvals.get(segments)));
		}
			
		points.add(new Point2D(xvals.get(0), yvals.get(0)));
		points.add(new Point2D(xvals.get(segments), yvals.get(segments)));
			
		for (int i=1; i<segments; i++) {
			points.add(new Point2D(xvals.get(i), yvals.get(0)));
			points.add(new Point2D(xvals.get(segments), yvals.get(segments-i)));
		}
		
		for (int i=1; i<segments; i++) {
			points.add(new Point2D(xvals.get(segments-i), yvals.get(segments)));
			points.add(new Point2D(xvals.get(segments), yvals.get(segments-i)));
		}
			
		points.add(new Point2D(xvals.get(0), yvals.get(segments))); 
		points.add(new Point2D(xvals.get(segments), yvals.get(0)));
			
		for (int i = 1; i<segments; i++) {
			points.add(new Point2D(xvals.get(0), yvals.get(segments-i)));
			points.add(new Point2D(xvals.get(segments-i), yvals.get(0)));
		}
			
			
		
		double cz = sz;
		boolean alt = true;
		
		Point2D pa, pb;
		
		for (int i = 0; i < passes; i++) {
			cz = cz - passdepth;
			if (cz < -depth) 
				cz = -depth;

			tGCode.append("(Pass number " + (i+1) + "/" + passes + " at depth " + fmtDbl(cz) + ")\n");
			
			tGCode.append(moveZ(safez, "G0", stZRapid));
			for (int j = 0; j<points.size(); j = j + 2) {
				if (alt) {
					pa = points.get(j);
					pb = points.get(j+1);
				}
				else {
					pa = points.get(j+1);
					pb = points.get(j);
				}
				alt = !alt;

				tGCode.append(moveXY(pa.x, pa.y, "G0", stXYRapid));
				tGCode.append(moveZ(cz, "G1", stZNormal));
				tGCode.append(moveXY(pb.x, pb.y, "G1", stXYNormal));
				tGCode.append(moveZ(safez, "G0", stZRapid));
			}
			
			if (border) {
				tGCode.append(moveXY(xvals.get(0), yvals.get(0), "G0", stXYRapid));
				tGCode.append(moveZ(cz, "G1", stZNormal));
				
				tGCode.append(moveXY(xvals.get(segments), yvals.get(0), "G1", stXYNormal));
				tGCode.append(moveXY(xvals.get(segments), yvals.get(segments), "G1", stXYNormal));
				tGCode.append(moveXY(xvals.get(0), yvals.get(segments), "G1", stXYNormal));
				tGCode.append(moveXY(xvals.get(0), yvals.get(0), "G1", stXYNormal));
				
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
		
	private void JSONInit() {
        JSONText.put("height",  tHeight);
        JSONText.put("width",  tWidth);
        JSONText.put("segments",  tSegments);
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