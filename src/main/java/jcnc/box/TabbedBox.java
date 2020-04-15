package main.java.jcnc.box;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import main.java.jcnc.CncDialog;
import main.java.jcnc.CncProperties;

public class TabbedBox extends CncDialog {
	private Visualizer viz;

	private class tabSlotGroup {
		Group grp;
		SelectionAdapter bSlotTabPress = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Button b = (Button) event.getSource();
				if (b.getSelection()) {
					setModified(true);
					viz.setSlotsTabs((String) b.getData());
				}
			}
		};
		private tabSlotGroup(Composite c, String a, String b, String tag) {
			grp = new Group(c, SWT.NONE);
			grp.setText(" " + a + " to " + b + " Joints");
			RowLayout rl = new RowLayout();
			rl.marginTop = 15;
			rl.type = SWT.VERTICAL;
			rl.spacing = 10;
			grp.setLayout(rl);
			
			Button rbSlots = new Button(grp, SWT.RADIO);
			rbSlots.setText(a+" slots");
			rbSlots.setData(tag+":slots");
			rbSlots.setSelection(true);
			rbSlots.addSelectionListener(bSlotTabPress);
			viz.setSlotsTabs((String) rbSlots.getData());
			
			Button rbTabs = new Button(grp, SWT.RADIO);
			rbTabs.setText(a+" tabs");
			rbTabs.setData(tag+":tabs");
			rbTabs.addSelectionListener(bSlotTabPress);
			
	
			Composite cc = new Composite(grp, SWT.NONE);
			rl = new RowLayout();
			rl.type = SWT.HORIZONTAL;
			rl.spacing = 10;
			cc.setLayout(rl);
	
			Label l = new Label(cc, SWT.NONE);
			l.setText("Number:");
			l.setAlignment(SWT.RIGHT);
			RowData rdLabel = new RowData();
			rdLabel.width = 60;
			l.setLayoutData(rdLabel);
			
			Spinner spNbr = new Spinner(cc, SWT.NONE);
			spNbr.setIncrement(1);
			spNbr.setMinimum(0);
			spNbr.setMaximum(10);
			spNbr.setSelection(0);
			spNbr.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					setModified(true);
					viz.setNum(tag, spNbr.getSelection());
				}
			});
			viz.setNum(tag, spNbr.getSelection());

	
			Composite wc = new Composite(grp, SWT.NONE);
			rl = new RowLayout();
			rl.type = SWT.HORIZONTAL;
			rl.spacing = 10;
			wc.setLayout(rl);
	
			l = new Label(wc, SWT.NONE);
			l.setText("Width:");
			l.setAlignment(SWT.RIGHT);
			rdLabel = new RowData();
			rdLabel.width = 60;
			l.setLayoutData(rdLabel);
			
			Text tWidth = new Text(wc, SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
			tWidth.setText(Double.toString(10.0));
			RowData rdText = new RowData();
			rdText.width = 50;
			tWidth.setLayoutData(rdText);
			tWidth.addVerifyListener(verifyDouble);
			tWidth.addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent event) {}
				public void focusLost(FocusEvent event) {
					viz.setTabWidth(tag, Double.valueOf(tWidth.getText()));
				}
			});
			viz.setTabWidth(tag, Double.valueOf(tWidth.getText()));
		}
		
		public Group getGroup() {
			return grp;
		}
	}
	
	public TabbedBox(Shell parent, CncProperties props) {
		super(parent, props);
		objectName = "Tabbed Box " + seqNo;
		viz = new Visualizer();
	}

	@Override
	protected void setTitle() {
		String title = "G Code Generator - " + objectName;
		if (modified)
			title = title + " *";

		shell.setText(title);
	}
	
	public void open() {
		super.open();
		createContents();
		super.dlgLoop();
	}
	
	SelectionAdapter bFacePress = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			Button b = (Button) event.getSource();
			if (b.getSelection()) {
				viz.selectFace((String) b.getData());
			}
		}
	};


	private void createContents() {	
		RowLayout dlgl = new RowLayout();
		dlgl.type = SWT.VERTICAL;
		shell.setLayout(dlgl);
		
		Display d = shell.getDisplay();
		
		Composite top = new Composite(shell, SWT.NONE);
		RowLayout rl = new RowLayout();
		rl.type = SWT.HORIZONTAL;
		rl.spacing = 10;
		top.setLayout(rl);

		Composite left = new Composite(top, SWT.NONE);
		rl = new RowLayout();
		rl.type = SWT.VERTICAL;
		rl.spacing = 20;
		left.setLayout(rl);
		
		dimensionGroup(left);
		reliefGroup(left, d);
				
		Composite center = new Composite(top, SWT.NONE);
		rl = new RowLayout();
		rl.type = SWT.VERTICAL;
		rl.spacing = 10;
		center.setLayout(rl);
		
		new tabSlotGroup(center, "Front/Back", "Left/Right", "FBLR");
		new tabSlotGroup(center, "Front/Back", "Top/Bottom", "FBTB");
		new tabSlotGroup(center, "Left/Right", "Top/Bottom", "LRTB");


		Composite right = new Composite(top, SWT.NONE);
		rl = new RowLayout();
		rl.type = SWT.HORIZONTAL;
		rl.spacing = 10;
		right.setLayout(rl);
		
		
		Composite faces = new Composite(right, SWT.NONE);
		rl = new RowLayout();
		rl.type = SWT.VERTICAL;
		rl.marginTop = 125;
		rl.spacing = 30;
		faces.setLayout(rl);
		
		String fNames[] = {"Front", "Back", "Top", "Bottom", "Left", "Right"};
		boolean first = true;
		for (String f : fNames) {
			Button rbFace = new Button(faces, SWT.RADIO);
			rbFace.setText(f);
			rbFace.setData(f);
			rbFace.setSelection(first);
			rbFace.addSelectionListener(bFacePress);
			first = false;
		}
		
		Button cbGrid = new Button(faces, SWT.CHECK);
		cbGrid.setText("Draw Grid");
		cbGrid.setSelection(true);
		cbGrid.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				viz.setDrawGrid(cbGrid.getSelection());
			}
		});
		
		viz.render(right, d);
		
		Composite cButtons = new Composite(shell, SWT.NONE);
		rl = new RowLayout();
		rl.type = SWT.HORIZONTAL;
		rl.center = true;
		rl.pack = false;
		rl.spacing = 30;
		rl.marginLeft = 50;
		rl.marginBottom = 20;
		cButtons.setLayout(rl);
		
		Image img = new Image(d, "resources/filenew.png");
		Button bNew = new Button(cButtons, SWT.PUSH);
		bNew.setImage(img);
		bNew.setToolTipText("New boxfile");
		RowData rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bNew.setLayoutData(rd);
		
		img = new Image(d, "resources/fileopen.png");
		Button bOpen = new Button(cButtons, SWT.PUSH);
		bOpen.setImage(img);
		bOpen.setToolTipText("Open box file");
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bOpen.setLayoutData(rd);
		
		img = new Image(d, "resources/filesave.png");
		Button bSave = new Button(cButtons, SWT.PUSH);
		bSave.setImage(img);
		bSave.setToolTipText("Save box file");
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bSave.setLayoutData(rd);
		
		img = new Image(d, "resources/filesaveas.png");
		Button bSaveAs = new Button(cButtons, SWT.PUSH);
		bSaveAs.setImage(img);
		bSaveAs.setToolTipText("Save box with a different file namefile");
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bSaveAs.setLayoutData(rd);
		
		img = new Image(d, "resources/contourcircle.png");
		Button bCircle = new Button(cButtons, SWT.PUSH);
		bCircle.setImage(img);
		bCircle.setToolTipText("Add/Edit circular openings in current face");
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bCircle.setLayoutData(rd);
		
		img = new Image(d, "resources/contourrectangle.png");
		Button bRect = new Button(cButtons, SWT.PUSH);
		bRect.setImage(img);
		bRect.setToolTipText("Add/Edit rectangular openings in current face");
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bRect.setLayoutData(rd);
		
		img = new Image(d, "resources/gcode.png");
		Button bGCode = new Button(cButtons, SWT.PUSH);
		bGCode.setImage(img);
		bGCode.setToolTipText("Generate G Code for the box faces");
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bGCode.setLayoutData(rd);
		
	}
	
	private Group dimensionGroup(Composite c) {
		Group grp = new Group(c, SWT.NONE);
		grp.setText(" Box Dimensions ");
		GridLayout dgl = new GridLayout();
		dgl.numColumns = 2;
		dgl.makeColumnsEqualWidth = false;
		dgl.verticalSpacing = 10;
		dgl.marginRight = 30;
		grp.setLayout(dgl);
		
		Label l = new Label(grp, SWT.NONE);
		l.setText("Height:");
		l.setAlignment(SWT.RIGHT);
		GridData gridLabel = new GridData();
		gridLabel.widthHint = 80;
		l.setLayoutData(gridLabel);
		
		Text tHeight = new Text(grp, SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
		tHeight.setText(Double.toString(100.0));
		GridData gridText = new GridData();
		gridText.widthHint = 50;
		tHeight.setLayoutData(gridText);
		tHeight.addVerifyListener(verifyDouble);
		tHeight.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) {}
			public void focusLost(FocusEvent event) {
				viz.setHeight(Double.valueOf(tHeight.getText()));
			}
		});
		viz.setHeight(Double.valueOf(tHeight.getText()));
		
		l = new Label(grp, SWT.NONE);
		l.setText("Width:");
		l.setAlignment(SWT.RIGHT);
		gridLabel = new GridData();
		gridLabel.widthHint = 80;
		l.setLayoutData(gridLabel);
		
		Text tWidth = new Text(grp, SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
		tWidth.setText(Double.toString(100.0));
		gridText = new GridData();
		gridText.widthHint = 50;
		tWidth.setLayoutData(gridText);
		tWidth.addVerifyListener(verifyDouble);
		tWidth.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) {}
			public void focusLost(FocusEvent event) {
				viz.setWidth(Double.valueOf(tWidth.getText()));
			}
		});
		viz.setWidth(Double.valueOf(tWidth.getText()));
		
		l = new Label(grp, SWT.NONE);
		l.setText("Depth:");
		l.setAlignment(SWT.RIGHT);
		gridLabel = new GridData();
		gridLabel.widthHint = 80;
		l.setLayoutData(gridLabel);
		
		Text tDepth = new Text(grp, SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
		tDepth.setText(Double.toString(100.0));
		gridText = new GridData();
		gridText.widthHint = 50;
		tDepth.setLayoutData(gridText);
		tDepth.addVerifyListener(verifyDouble);
		tDepth.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) {}
			public void focusLost(FocusEvent event) {
				viz.setDepth(Double.valueOf(tDepth.getText()));
			}
		});
		viz.setDepth(Double.valueOf(tDepth.getText()));
	
		l = new Label(grp, SWT.NONE);
		l.setText("Wall Thickness:");
		l.setAlignment(SWT.RIGHT);
		gridLabel = new GridData();
		gridLabel.widthHint = 80;
		l.setLayoutData(gridLabel);
		
		Text tWall = new Text(grp, SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
		tWall.setText(Double.toString(6.0));
		gridText = new GridData();
		gridText.widthHint = 50;
		tWall.setLayoutData(gridText);
		tWall.addVerifyListener(verifyDouble);
		tWall.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) {}
			public void focusLost(FocusEvent event) {
				viz.setWall(Double.valueOf(tWall.getText()));
			}
		});
		viz.setWall(Double.valueOf(tWall.getText()));
		
		return grp;
	}
	
	private Group reliefGroup(Composite c, Display d) {
		SelectionAdapter bReliefPress = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Button b = (Button) event.getSource();
				if (b.getSelection()) {
					setModified(true);
					viz.setRelief((String) b.getData());
				}
			}
		};

		Group grp = new Group(c, SWT.NONE);
		grp.setText(" Tool Radius Relief ");
		GridLayout rgl = new GridLayout();
		rgl.numColumns = 2;
		rgl.makeColumnsEqualWidth = false;
		rgl.verticalSpacing = 10;
		grp.setLayout(rgl);
		
		Button rbRlfNone = new Button(grp, SWT.RADIO);
		rbRlfNone.setText("None");
		rbRlfNone.setData("none");
		rbRlfNone.setSelection(true);
		GridData rgd = new GridData();
		rgd.widthHint = 100;
		rbRlfNone.setLayoutData(rgd);
		rbRlfNone.addSelectionListener(bReliefPress);
		viz.setRelief((String) rbRlfNone.getData());
		
		Image img = new Image(d, "resources/none.png");
		Label l = new Label(grp, SWT.NONE);
		l.setImage(img);
		
		Button rbRlfWidth = new Button(grp, SWT.RADIO);
		rbRlfWidth.setText("Tab/Slot Width");
		rbRlfWidth.setData("width");
		rgd = new GridData();
		rgd.widthHint = 100;
		rbRlfWidth.setLayoutData(rgd);
		rbRlfWidth.addSelectionListener(bReliefPress);
		
		img = new Image(d, "resources/wrelief.png");
		l = new Label(grp, SWT.NONE);
		l.setImage(img);
		
		Button rbRlfHeight = new Button(grp, SWT.RADIO);
		rbRlfHeight.setText("Tab/Slot Height");
		rbRlfHeight.setData("height");
		rgd = new GridData();
		rgd.widthHint = 100;
		rbRlfHeight.setLayoutData(rgd);
		rbRlfHeight.addSelectionListener(bReliefPress);
			
		img = new Image(d, "resources/hrelief.png");
		l = new Label(grp, SWT.NONE);
		l.setImage(img);
		
		l = new Label(grp, SWT.NONE);
		l.setText("Tool Radius:");
		l.setAlignment(SWT.RIGHT);
		GridData gridLabel = new GridData();
		gridLabel.widthHint = 100;
		l.setLayoutData(gridLabel);
		
		Text tToolRad = new Text(grp, SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
		tToolRad.setText(Double.toString(1.5));
		GridData gridText = new GridData();
		gridText.widthHint = 50;
		tToolRad.setLayoutData(gridText);
		tToolRad.addVerifyListener(verifyDouble);
		tToolRad.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) {}
			public void focusLost(FocusEvent event) {
				viz.setToolRad(Double.valueOf(tToolRad.getText()));
			}
		});
		viz.setToolRad(Double.valueOf(tToolRad.getText()));
		
		return grp;
	}
}

