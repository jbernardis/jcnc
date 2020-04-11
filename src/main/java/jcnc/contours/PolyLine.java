package main.java.jcnc.contours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONObject;

import main.java.jcnc.CncDialog;
import main.java.jcnc.CncProperties;
import main.java.jcnc.Offset;
import main.java.jcnc.PLEditDlg;
import main.java.jcnc.Point2D;

public class PolyLine extends CncDialog {
	Text tPoints;
	Button bEditPoints;
	
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
	
	String toolMovement = null;
	HashMap<String, Button> hmToolMovement = new HashMap<String, Button>();
	
	Button bClosePath;
	boolean closePath;
	
	Spinner sTracks;

	public PolyLine(Shell parent, CncProperties props) {
		super(parent, props);
		objectName = "PolyLine Contour " + seqNo;
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

		addLabel("Points: ");
		tPoints = new Text(shell, SWT.LEFT | SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
		GridData gridText = new GridData();
		gridText.widthHint = 150;
		gridText.horizontalSpan = 2;
		tPoints.setLayoutData(gridText);
		tPoints.setText("[ ]");

		bEditPoints = new Button(shell, SWT.PUSH);
		bEditPoints.setText("...");
		bEditPoints.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				editPoints();
			}
		});
		
		bClosePath = new Button(shell, SWT.CHECK);
		bClosePath.setText("Close Path");
		bClosePath.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				closePath = bClosePath.getSelection();
				enableGenerateButton();
				setModified(true);
			}
		});
		GridData gd = new GridData();
		gd.horizontalSpan = 4;
		gd.horizontalAlignment = SWT.CENTER;
		gd.horizontalIndent = 10;
		bClosePath.setLayoutData(gd);
		closePath = false;
		bClosePath.setSelection(closePath);
	
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

		addLabel("StepOver: ");
		tStepOver = addDoubleText((Double) toolMaterialInfo.get("stepover"));

		Group tm = addToolMovement();
		gd = (GridData) tm.getLayoutData();
		gd.horizontalSpan = 2;
		tm.setLayoutData(gd);
		
		addLabel("Tracks:");
		sTracks = new Spinner (shell, SWT.BORDER);
		sTracks.setMinimum(1);
		sTracks.setMaximum(100);
		sTracks.setSelection(1);
		sTracks.setIncrement(1);
		sTracks.setPageIncrement(10);
		sTracks.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				setModified(true);
			}
		});


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
		bGenerate.setEnabled(false);

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

		tGCode = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData(SWT.FILL, 4, true, true);
		gd.horizontalSpan = 4;
		gd.heightHint = 200;
		tGCode.setLayoutData(gd);
	}
	
	private void editPoints() {
		List<Point2D> plist = parsePoints(tPoints.getText());
		PLEditDlg dlg = new PLEditDlg(shell, plist);
		int minPoints = 2;
		if (closePath)
			minPoints = 3;
		int rc = dlg.open(minPoints);
		if (rc == SWT.CANCEL) {
			return;
		}
		
		plist = dlg.getPoints();
		
		String result = "[ ";

		boolean first = true;
		for (Point2D p : plist) {
			if (!first)
				result = result + ", ";
			first = false;
			
			result = result + "[" + Double.toString(p.x) + "," + Double.toString(p.y)+ "]";  
		}
		
		result = result + " ]";
		tPoints.setText(result);
		bGenerate.setEnabled(plist.size() >= minPoints);
	}
	
	private List<Point2D> parsePoints(String plString) {
		Pattern point = Pattern.compile("\\[[\\d-.]+, *[\\d-.]+\\]");
		Pattern xy = Pattern.compile(   "\\[([\\d\\-\\.]+), *([\\d\\-\\.]+)\\]");
		
		Matcher m = point.matcher(plString);

		List<Point2D> plist = new ArrayList<Point2D>();
		plist.clear();
		
		while (m.find()) {
			String pt = plString.substring(m.start(), m.end());
			Matcher ptm = xy.matcher(pt);
			if (ptm.find()) {
				Double vx = Double.valueOf(ptm.group(1));
				Double vy = Double.valueOf(ptm.group(2));
				
				Point2D p = new Point2D(vx, vy);
				plist.add(p);
			}
			else {
				System.out.println("Unable to interpret \"" + pt + "\"");
			}
		}
		return plist;
	}
	
	private void enableGenerateButton() {
		List<Point2D> pl = parsePoints(tPoints.getText());
		int minPoints = 2;
		if (closePath)
			minPoints = 3;
		bGenerate.setEnabled(pl.size() >= minPoints);
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
		
		String[] tags = {"On Line", "Right of forward motion", "Left of forward motion"};

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
		
		String pointString = tPoints.getText();
		int tracks = sTracks.getSelection();

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
		
		List<Point2D> points = parsePoints(pointString);
		int needed = 2;
		if (closePath)
			needed = 3;
		
		if (points.size() < needed) {
			System.out.println("Need more points");
			return;
		}

		tGCode.append("(" + objectName + ")\n");
		tGCode.append("(Points: " + pointString + ")\n");
				tGCode.append("(depth from " + fmtDbl(sz) + " to -" + fmtDbl(depth) + ")\n");
		if (tooldiam == (Double) toolInfo.get("diameter")) {
			tGCode.append("(Tool " + (String) toolInfo.get("name") + " diameter: " + fmtDbl(tooldiam));
		}
		else {
			tGCode.append("(Tool diameter " + fmtDbl(tooldiam));
		}
		tGCode.append("   depth/pass -" + fmtDbl(passdepth) + ")\n");
		tGCode.append("(Tool movement: " + toolMovement + ")\n");
		tGCode.append("(Tracks: " + tracks + ")");		
		tGCode.append("(Close Path: " + closePath + ")");		
		
		tGCode.append("G90\n");
		if (metric)
			tGCode.append("G21 (Metric)\n");
		else
			tGCode.append("G20 (Imperial)\n");

		double offset, offset2;
		if (toolMovement.equals("Left of forward motion")) {
			offset = tooldiam/2.0;
			offset2 = stepover * tooldiam;
		}
		else if (toolMovement.equals("Right of forward motion")) {
			offset = -tooldiam/2.0;
			offset2 = -stepover * tooldiam;
		}
		else {
			offset = 0.0;
			offset2 = 0.0;
			if (tracks != 1) {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				messageBox.setText("Invalid number of traces");
				messageBox.setMessage("Only 1 trace permitted it tool movement in \"On Line\"\n\nAssuming value of 1.");
				messageBox.open();

				tracks = 1;
			}
		}
		
		Offset off = new Offset();
		if (offset != 0) {
			points = off.offsetPath(points, offset, closePath);
			if (points == null) {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				messageBox.setText("Unable to proceed");
				messageBox.setMessage("Could not calculate path offset.");
				messageBox.open();
				return;
			}
		}
			
		double cz = sz - passdepth;
		if (cz < -depth)
			cz = -depth;
		Point2D p0 = points.get(0);
		tGCode.append(moveZ(safez, "G0", stZRapid));
		tGCode.append(moveXY(p0.x, p0.y, "G0", stXYRapid));
		
		List<Point2D> savePoints = new ArrayList<Point2D>();
		savePoints.addAll(points);
	
		for (int p = 0; p < passes; p++) {

			tGCode.append("(Pass number " + (p + 1) + "/" + passes + " at depth " + fmtDbl(cz) + ")\n");
			
			tGCode.append(moveZ(cz, "G1", stZNormal));
			
			for (int trk=0; trk < tracks; trk++) {
				for (int px=1; px<points.size(); px++) {
					Point2D pt = points.get(px);
					tGCode.append(moveXY(pt.x, pt.y, "G1", stXYNormal));
				}
				
				if (closePath) {
					tGCode.append(moveXY(p0.x, p0.y, "G1", stXYNormal));
				}
				else {
					tGCode.append(moveZ(safez, "G0", stZRapid));
					tGCode.append(moveXY(p0.x, p0.y, "G0", stXYRapid));
					tGCode.append(moveZ(cz, "G1", stZNormal));
				}
					
				if (trk+1 < tracks) { // if not last track
					points = off.offsetPath(points, offset2, closePath);
					if (points == null) {
						MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
						messageBox.setText("Unable to proceed");
						messageBox.setMessage("Could not calculate path offset for track " + (trk+1) + ".");
						messageBox.open();
						return;
					}
					p0 = points.get(0);
					tGCode.append(moveXY(p0.x, p0.y, "G1", stXYNormal));
				}
			}
			
			points.clear();
			points.addAll(savePoints);
			p0 = points.get(0);
			
			tGCode.append(moveXY(p0.x, p0.y, "G1", stXYNormal));
			cz = cz - passdepth;
			if (cz < -depth)
				cz = -depth;
		}
		
		tGCode.append(moveZ(safez, "G0", stZRapid));
			
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

        JSONText.put("points",  tPoints);
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
		JSONAllKeys.add("closepath");
		JSONAllKeys.add("tracks");
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
		objData.put("closepath", closePath);
		objData.put("tracks", sTracks.getSelection());
		
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
        
        closePath = (boolean) objData.get("closepath");
        bClosePath.setSelection(closePath);
        
        int tracks = ((Long) objData.get("tracks")).intValue();
        sTracks.setSelection(tracks);
        
        boolean b = (boolean) objData.get("addspeedterm");
        if (b != addSpeedTerm) {
        	addSpeedTerm = b;
        	props.setPropAddSpeed(addSpeedTerm);
        	bAddSpeed.setSelection(addSpeedTerm);
        	enableSpeedControls();
        }
        
        enableGenerateButton();

		GCStale = haveGCode;
		setModified(false);
	}
}
