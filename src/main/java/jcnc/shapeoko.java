package main.java.jcnc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import main.java.jcnc.box.TabbedBox;
import main.java.jcnc.carvings.DiamondCarve;
import main.java.jcnc.carvings.GridCarve;
import main.java.jcnc.carvings.HatchCarve;
import main.java.jcnc.contours.Arc;
import main.java.jcnc.contours.Circle;
import main.java.jcnc.contours.Line;
import main.java.jcnc.contours.PolyLine;
import main.java.jcnc.contours.Polygon;
import main.java.jcnc.contours.Rectangle;
import main.java.jcnc.contours.RoundedSlot;
import main.java.jcnc.drills.CircDrill;
import main.java.jcnc.drills.LinearDrill;
import main.java.jcnc.drills.RectDrill;
import main.java.jcnc.viewer.GViewer;


public class shapeoko {
	private static Shell shell;
	private static CncProperties props;
	
	private static MenuItem itemAddSpeed;
	
	private static Button createButton(String label, Display d, Group contourButtons) {
		GridData gridButton = new GridData();
		gridButton.heightHint = 48;
		gridButton.widthHint = 48;
		
		Image img = new Image(d, "resources/" + label + ".png");

		Button button = new Button(contourButtons, SWT.PUSH);
		button.setData(label);
		button.setImage(img);;
		button.setLayoutData(gridButton);	
		
		return button;
	}
	
	private static Shell mainWindow(Display d) {
		shell = new Shell(d);
		shell.setText("G-Code Generators");
		
		addMenu();
		
		GridLayout gl = new GridLayout();
		shell.setLayout(gl);
		
		//JSONParser jp = new JSONParser();
		
		Group contourButtons = new Group(shell, SWT.NULL);
		contourButtons.setText(" Contours ");
		GridLayout cbgl = new GridLayout();
		cbgl.numColumns = 5;
		cbgl.makeColumnsEqualWidth = true;
		contourButtons.setLayout(cbgl);
		
		Button b1 = createButton("contourarc", d, contourButtons);
		b1.setToolTipText("Generate G Code for an Arc contour");
		
		Button b2 = createButton("contourcircle", d, contourButtons);
		b2.setToolTipText("Generate G Code for a Circle contour");
		
		Button b3 = createButton("contourline", d, contourButtons);
		b3.setToolTipText("Generate G Code for an Linear contour");
		
		Button b4 = createButton("contourpolygon", d, contourButtons);
		b4.setToolTipText("Generate G Code for an Regular Polygon contour");
		
		Button b5 = createButton("contourpolyline", d, contourButtons);
		b5.setToolTipText("Generate G Code for an Irregular Polygon or Open Path contour");
		
		Button b6 = createButton("contourrectangle", d, contourButtons);
		b6.setToolTipText("Generate G Code for an Rectangle contour");
		
		Button b7 = createButton("contourroundedslot", d, contourButtons);
		b7.setToolTipText("Generate G Code for an Rounded Slot contour");
		
		
		Group drillButtons = new Group(shell, SWT.NULL);
		drillButtons.setText(" Drill Patterns ");
		GridLayout dbgl = new GridLayout();
		dbgl.numColumns = 5;
		dbgl.makeColumnsEqualWidth = true;
		drillButtons.setLayout(dbgl);
		
		Button b8 = createButton("drillcircle", d, drillButtons);
		b8.setToolTipText("Generate G Code for a Circular drill pattern");
		
		Button b9 = createButton("drilllinear", d, drillButtons);
		b9.setToolTipText("Generate G Code for a Linear drill pattern");
		
		Button b10 = createButton("drillrectangle", d, drillButtons);
		b10.setToolTipText("Generate G Code for a Rectangular drill pattern");
		
	
		Group carveButtons = new Group(shell, SWT.NULL);
		carveButtons.setText(" Carvings ");
		GridLayout vbgl = new GridLayout();
		vbgl.numColumns = 5;
		vbgl.makeColumnsEqualWidth = true;
		carveButtons.setLayout(vbgl);
		
		Button b11 = createButton("carvediamond", d, carveButtons);
		b11.setToolTipText("Generate G Code for a Diamond carve pattern");
		
		Button b12 = createButton("carvegrid", d, carveButtons);
		b12.setToolTipText("Generate G Code for a Grid carve pattern");
		
		Button b13 = createButton("carvehatch", d, carveButtons);
		b13.setToolTipText("Generate G Code for a Crosshatch carve pattern");
		
		Group objectButtons = new Group(shell, SWT.NULL);
		objectButtons.setText(" Objects ");
		GridLayout obgl = new GridLayout();
		obgl.numColumns = 5;
		obgl.makeColumnsEqualWidth = true;
		objectButtons.setLayout(obgl);
		
		Button b14 = createButton("tabbedbox", d, objectButtons);
		b14.setToolTipText("Generate G Code for a tabbed box");
		
		SelectionAdapter bPress = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Button b = (Button) event.getSource();
				launchDialog((String) b.getData());
			}
		};

		b1.addSelectionListener(bPress);
		b2.addSelectionListener(bPress);
		b3.addSelectionListener(bPress);
		b4.addSelectionListener(bPress);
		b5.addSelectionListener(bPress);
		b6.addSelectionListener(bPress);
		b7.addSelectionListener(bPress);
		b8.addSelectionListener(bPress);
		b9.addSelectionListener(bPress);
		b10.addSelectionListener(bPress);
		b11.addSelectionListener(bPress);
		b12.addSelectionListener(bPress);
		b13.addSelectionListener(bPress);
		b14.addSelectionListener(bPress);

		
		shell.layout(true, true);
		final Point newSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);  
		shell.setSize(newSize);
		shell.open();
		
		return shell;
	}
	
	private static void addMenu() {
		Menu bar = new Menu (shell, SWT.BAR);
		shell.setMenuBar (bar);
		
		MenuItem fileItem = new MenuItem (bar, SWT.CASCADE);
		fileItem.setText ("&File");
		Menu submenu = new Menu (shell, SWT.DROP_DOWN);
		fileItem.setMenu (submenu);
		MenuItem item = new MenuItem (submenu, SWT.PUSH);
		item.addListener (SWT.Selection, e -> fileView());
		item.setText ("&View");
		item.setAccelerator (SWT.MOD1 + 'V');
		
		item = new MenuItem (submenu, SWT.PUSH);
		item.addListener  (SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				merge();
			}
		});
		item.setText ("&Merge");
		item.setAccelerator (SWT.MOD1 + 'M');

		MenuItem optionsItem = new MenuItem (bar, SWT.CASCADE);
		optionsItem.setText ("&Options");
		submenu = new Menu (shell, SWT.DROP_DOWN);
		optionsItem.setMenu (submenu);
		itemAddSpeed = new MenuItem (submenu, SWT.CHECK);
		itemAddSpeed.addListener (SWT.Selection, e -> props.setPropAddSpeed(itemAddSpeed.getSelection()));
		itemAddSpeed.setText ("&Add Speed Term");
		itemAddSpeed.setAccelerator (SWT.MOD1 + 'A');
		itemAddSpeed.setSelection(props.getPropAddSpeed());
		
		MenuItem itemMetric = new MenuItem (submenu, SWT.CHECK);
		itemMetric.addListener (SWT.Selection, e -> props.setPropMetric(itemMetric.getSelection()));
		itemMetric.setText ("Me&tric");
		itemMetric.setAccelerator (SWT.MOD1 + 'T');
		itemMetric.setSelection(props.getPropMetric());
		
		MenuItem materialsItem = new MenuItem (bar, SWT.CASCADE);
		materialsItem.setText ("Mate&rials");
		submenu = new Menu (shell, SWT.DROP_DOWN);
		materialsItem.setMenu (submenu);
		List<String> ml = props.getMaterialList();
		for (String m : ml) {
			item = new MenuItem (submenu, SWT.RADIO);
			item.addListener (SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					selectMaterial(event);
				}
			});
			item.setText(m);
			item.setData(m);
			item.setSelection(m.equals(props.getCurrentMaterial()));
		}

		MenuItem toolsItem = new MenuItem (bar, SWT.CASCADE);
		toolsItem.setText ("Tools");
		submenu = new Menu (shell, SWT.DROP_DOWN);
		toolsItem.setMenu (submenu);
		List<String> tl = props.getToolList();
		for (String t : tl) {
			HashMap<String, Object> ti = props.getTool(t);
			String tn = (String) ti.get("name");
			item = new MenuItem (submenu, SWT.RADIO);
			item.addListener (SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					selectTool(event);
				}
			});
			item.setText(tn);
			item.setData(t);
			item.setSelection(t.equals(props.getCurrentTool()));
		}
	}
	
	private static void fileView() {
		String gcd = props.getPropLastGCodeDir();
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		fd.setText("Open G Code File");
		fd.setFilterPath(gcd);
		String[] filterExt = { "*.nc", "*.*" };
		fd.setFilterExtensions(filterExt);
		String selected = fd.open();
		if (selected == null)
			return;

		int n = selected.lastIndexOf("\\");
		String dname;
		if (n < 0) {
			dname = "";
		}
		else {
			dname = selected.substring(0, n+1);
		}

		if (!dname.equals(gcd)) {
			props.setPropLastGCodeDir(dname);
		}

		List<String> lines = new ArrayList<String>();
		try {
			Scanner sc = new Scanner(new File(selected));
			while (sc.hasNextLine()) {
			  lines.add(sc.nextLine());
			}
			sc.close();
		}
		catch (FileNotFoundException e) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			messageBox.setText(" File Open Error");
			messageBox.setMessage("Unable to open file\n\"" + selected + "\".");
			messageBox.open();
			return;
		}

		String[] gcode = lines.toArray(new String[0]);	
		GViewer gv = new GViewer(shell, selected);
		gv.addGCode(gcode);
	}
	
	private static void merge() {
		Merge mergeDlg = new Merge(shell, props);
		mergeDlg.open();
	}
	
	private static void selectMaterial(Event e) {
		MenuItem w = (MenuItem) e.widget;
		String m = (String) w.getData();
		if (w.getSelection()) {
			props.setCurrentMaterial(m);
		}
	}
	
	private static void selectTool(Event e) {
		MenuItem w = (MenuItem) e.widget;
		String t = (String) w.getData();
		if (w.getSelection()) {
			props.setCurrentTool(t);
		}
	}
	
	private static void launchDialog(String dlgName) {
		if (dlgName.contentEquals("contourline")) {
			Line dlg = new Line(shell, props);
			dlg.open();
		}
		else if (dlgName.contentEquals("contourrectangle")) {
			Rectangle dlg = new Rectangle(shell, props);
			dlg.open();
		}
		else if (dlgName.contentEquals("contourcircle")) {
			Circle dlg = new Circle(shell, props);
			dlg.open();
		}
		else if (dlgName.contentEquals("contourpolygon")) {
			Polygon dlg = new Polygon(shell, props);
			dlg.open();
		}
		else if (dlgName.contentEquals("contourpolyline")) {
			PolyLine dlg = new PolyLine(shell, props);
			dlg.open();
		}
		else if (dlgName.contentEquals("contourroundedslot")) {
			RoundedSlot dlg = new RoundedSlot(shell, props);
			dlg.open();
		}
		else if (dlgName.contentEquals("contourarc")) {
			Arc dlg = new Arc(shell, props);
			dlg.open();
		}

		else if (dlgName.contentEquals("drillcircle")) {
			CircDrill dlg = new CircDrill(shell, props);
			dlg.open();
		}
		else if (dlgName.contentEquals("drillrectangle")) {
			RectDrill dlg = new RectDrill(shell, props);
			dlg.open();
		}
		else if (dlgName.contentEquals("drilllinear")) {
			LinearDrill dlg = new LinearDrill(shell, props);
			dlg.open();
		}
		
		else if (dlgName.contentEquals("carvediamond")) {
			DiamondCarve dlg = new DiamondCarve(shell, props);
			dlg.open();
		}
		else if (dlgName.contentEquals("carvegrid")) {
			GridCarve dlg = new GridCarve(shell, props);
			dlg.open();
		}
		else if (dlgName.contentEquals("carvehatch")) {
			HatchCarve dlg = new HatchCarve(shell, props);
			dlg.open();
		}
		
		else if (dlgName.contentEquals("tabbedbox")) {
			TabbedBox dlg = new TabbedBox(shell, props);
			dlg.open();
		}
		// addspeed can be changed inside of the dialogs, so we assert menu value here
		itemAddSpeed.setSelection(props.getPropAddSpeed());
		
	}

	public static void main(String[] args) {
		props = new CncProperties();
		
		Display display = new Display();
		Shell shell = mainWindow(display);
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

}
