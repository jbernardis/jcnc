package main.java.jcnc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

public class EditGCode extends Dialog {
	Shell shell;
	
	Spinner spX, spY;
	Button rbX0, rbX1, rbX2, rbX3, rbY0, rbY1, rbY2, rbY3;
	
	String cmdString;
	
	public EditGCode(Shell parent) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}

	public String open() {
		shell = new Shell(getParent(), getStyle());
		cmdString = null;
		shell.setText("Edit G Code File");

		createContents();
		shell.pack();
		shell.layout(true, true);
		final Point newSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		shell.setSize(newSize);
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return cmdString;
	}

	private void createContents() {
		SelectionAdapter bRadioPress = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Button b = (Button) event.getSource();
				if (b.getSelection()) {
					setSpinnerIncrement((String) b.getData());
				}
			}
		};

		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		gl.makeColumnsEqualWidth = false;
		gl.verticalSpacing = 10;
		gl.horizontalSpacing = 20;
		shell.setLayout(gl);
		
		Group g = new Group(shell, SWT.NONE);
		g.setText(" Translate ");;
		gl = new GridLayout();
		gl.numColumns = 3;
		gl.makeColumnsEqualWidth = false;
		gl.verticalSpacing = 10;
		gl.horizontalSpacing = 20;
		g.setLayout(gl);
		
		Composite c = new Composite(g, SWT.BORDER);
		gl = new GridLayout();
		gl.numColumns = 4;
		gl.makeColumnsEqualWidth = false;
		gl.verticalSpacing = 10;
		gl.horizontalSpacing = 20;
		c.setLayout(gl);
		
		Label l = new Label(c, SWT.NONE);
		l.setText("X:");
		l.setAlignment(SWT.RIGHT);
		
		spX = new Spinner(c, SWT.NONE);
		spX.setDigits(2);
		spX.setMinimum(-100000);
		spX.setMaximum(100000);
		spX.setIncrement(10);
		spX.setPageIncrement(100);
		spX.setSelection(0);
		GridData gd = new GridData();
		gd.widthHint = 48;
		gd.horizontalSpan = 3;
		spX.setLayoutData(gd);
		
		Composite rc = new Composite(c, SWT.NONE);
		RowLayout rl = new RowLayout();
		rl.type = SWT.VERTICAL;
		rl.wrap = false;
		rl.marginWidth = 20;
		rl.marginBottom = 10;
		rl.center = false;
		rc.setLayout(rl);
		
		rbX0 = new Button(rc, SWT.RADIO);
		rbX0.setText("10.0");
		rbX0.setData("X10.0");
		rbX0.addSelectionListener(bRadioPress);
		rbX1 = new Button(rc, SWT.RADIO);
		rbX1.setText("1.0");
		rbX1.setData("X1.0");
		rbX1.addSelectionListener(bRadioPress);
		rbX2 = new Button(rc, SWT.RADIO);
		rbX2.setText("0.1");
		rbX2.setData("X0.1");
		rbX2.addSelectionListener(bRadioPress);
		rbX3 = new Button(rc, SWT.RADIO);
		rbX3.setText("0.01");
		rbX3.setData("X0.01");
		rbX3.addSelectionListener(bRadioPress);
		rbX1.setSelection(true);

		gd = new GridData();
		gd.horizontalSpan = 4;
		rc.setLayoutData(gd);
		
		c = new Composite(g, SWT.BORDER);
		gl = new GridLayout();
		gl.numColumns = 4;
		gl.makeColumnsEqualWidth = false;
		gl.verticalSpacing = 10;
		gl.horizontalSpacing = 20;
		c.setLayout(gl);
		
		l = new Label(c, SWT.NONE);
		l.setText("Y:");
		l.setAlignment(SWT.RIGHT);
		
		spY = new Spinner(c, SWT.NONE);
		spY.setDigits(2);
		spY.setMinimum(-100000);
		spY.setMaximum(100000);
		spY.setIncrement(10);
		spY.setPageIncrement(100);
		spY.setSelection(0);
		gd = new GridData();
		gd.widthHint = 48;
		gd.horizontalSpan = 3;
		spY.setLayoutData(gd);
		
		rc = new Composite(c, SWT.NONE);
		rl = new RowLayout();
		rl.type = SWT.VERTICAL;
		rl.wrap = false;
		rl.marginWidth = 20;
		rl.marginBottom = 10;
		rl.center = false;
		rc.setLayout(rl);
		
		rbY0 = new Button(rc, SWT.RADIO);
		rbY0.setText("10.0");
		rbY0.setData("Y10.0");
		rbY0.addSelectionListener(bRadioPress);
		rbY1 = new Button(rc, SWT.RADIO);
		rbY1.setText("1.0");
		rbY1.setData("Y1.0");
		rbY1.addSelectionListener(bRadioPress);
		rbY2 = new Button(rc, SWT.RADIO);
		rbY2.setText("0.1");
		rbY2.setData("Y0.1");
		rbY2.addSelectionListener(bRadioPress);
		rbY3 = new Button(rc, SWT.RADIO);
		rbY3.setText("0.01");
		rbY3.setData("Y0.01");
		rbY3.addSelectionListener(bRadioPress);
		rbY1.setSelection(true);

		gd = new GridData();
		gd.horizontalSpan = 4;
		rc.setLayoutData(gd);
		
		Display disp = shell.getDisplay();
		Button bTranslate = new Button(g, SWT.PUSH);
		Image img = new Image(disp, "resources/translate.png");
		bTranslate.setImage(img);
		bTranslate.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				double dx = Integer.valueOf(spX.getSelection()) / 100.0;
				double dy = Integer.valueOf(spY.getSelection()) / 100.0;
				cmdString = "tl;" + dx + ";" + dy;
				shell.dispose();
			}
		});
		gd = new GridData();
		gd.heightHint = 48;
		gd.widthHint = 48;
		bTranslate.setLayoutData(gd);

		GridData cgd = new GridData();
		cgd.horizontalSpan = 1;
		cgd.horizontalAlignment = SWT.CENTER;
		cgd.grabExcessHorizontalSpace = true;
		g.setLayoutData(cgd);
		// end of translate group

		
		
		g = new Group(shell, SWT.NONE);
		g.setText(" Mirror ");;
		rl = new RowLayout();
		rl.spacing = 20;
		rl.center = true;
		g.setLayout(rl);
		
		
		
		Button bMirrorX = new Button(g, SWT.PUSH);
		img = new Image(disp, "resources/mirrorx.png");
		bMirrorX.setImage(img);
		bMirrorX.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				cmdString = "mx";
				shell.dispose();
			}
		});
		RowData rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bMirrorX.setLayoutData(rd);
		
		Button bMirrorY = new Button(g, SWT.PUSH);
		img = new Image(disp, "resources/mirrory.png");
		bMirrorY.setImage(img);
		bMirrorY.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				cmdString = "my";
				shell.dispose();
			}
		});
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bMirrorY.setLayoutData(rd);
		
		
		cgd = new GridData();
		cgd.horizontalSpan = 1;
		cgd.grabExcessHorizontalSpace = true;
		cgd.horizontalAlignment = SWT.CENTER;
		g.setLayoutData(cgd);
		// end of mirror group

	}
	
	private void setSpinnerIncrement(String tag) {
		if (tag.equals("X10.0")) {
			spX.setIncrement(1000);
			spX.setPageIncrement(10000);
		}
		else if (tag.equals("X1.0")) {
			spX.setIncrement(100);
			spX.setPageIncrement(1000);
		}
		else if (tag.equals("X0.1")) {
			spX.setIncrement(10);
			spX.setPageIncrement(100);
		}
		else if (tag.equals("X0.01")) {
			spX.setIncrement(1);
			spX.setPageIncrement(10);
		}
		if (tag.equals("Y10.0")) {
			spY.setIncrement(1000);
			spY.setPageIncrement(10000);
		}
		else if (tag.equals("Y1.0")) {
			spY.setIncrement(100);
			spY.setPageIncrement(1000);
		}
		else if (tag.equals("Y0.1")) {
			spY.setIncrement(10);
			spY.setPageIncrement(100);
		}
		else if (tag.equals("Y0.01")) {
			spY.setIncrement(1);
			spY.setPageIncrement(10);
		}
		else {
			System.out.println("Unknown tag (" + tag + ")");
		}
		
	}
}
