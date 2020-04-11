package main.java.jcnc;

import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import main.java.jcnc.viewer.GViewer;

public class Merge extends Dialog{
	Shell shell;
	CncProperties props;
	
	Text tGCode;
	Button bAdd, bSave, bView, bExit;
	
	boolean unsaved = false;
	int fileCount;

	public Merge(Shell parent, CncProperties props) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.props = props;	
		fileCount = 0;
	}

	public void open() {
		shell = new Shell(getParent(), getStyle());
		shell.setText("Merge Files");
	    shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				if (unsaved) {
					MessageBox messageBox = new MessageBox (shell, SWT.APPLICATION_MODAL | SWT.YES | SWT.NO);
					messageBox.setText ("Abandon merge contents");
					messageBox.setMessage ("Merged G Code has not been saved.  Continue?");
					if (messageBox.open () == SWT.NO) {
						event.doit = false;
						return;
					}
				}
				event.doit = true;
			}
		});

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
	}

	private void createContents() {
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		gl.makeColumnsEqualWidth = false;
		gl.verticalSpacing = 10;
		gl.horizontalSpacing = 20;
		shell.setLayout(gl);
		
		tGCode = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, 4, true, true);
		gd.heightHint = 200;
		tGCode.setLayoutData(gd);
		
		Group c = new Group(shell, SWT.NONE);
		RowLayout rl = new RowLayout();
		rl.spacing = 20;
		rl.marginWidth = 20;
		rl.marginBottom = 10;
		rl.center = true;
		c.setLayout(rl);

		Display disp = shell.getDisplay();

		Image img = new Image(disp, "resources/add.png");
		bAdd = new Button(c, SWT.PUSH);
		bAdd.setImage(img);
		bAdd.setToolTipText("Add a file to the merge");
		bAdd.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				AddFile addFile = new AddFile(shell, props);
				if (addFile.open()) {
					fileCount++;
					unsaved = true;
					//retrieve g code from dialog
					String[] gc = addFile.retrieveGCode();
					for (String gl: gc) {
						tGCode.append(gl + "\n");
					}
					enableButtons();
				}
			}
		});
		RowData rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bAdd.setLayoutData(rd);

		img = new Image(disp, "resources/filesaveas.png");
		bSave = new Button(c, SWT.PUSH);
		bSave.setImage(img);
		bSave.setToolTipText("Save the merged files to a single file");
		bSave.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				saveGCode();
			}
		});
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bSave.setLayoutData(rd);

		img = new Image(disp, "resources/view.png");
		bView = new Button(c, SWT.PUSH);
		bView.setImage(img);
		bView.setToolTipText("View the merged files");
		bView.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				viewGCode();
			}
		});
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bView.setLayoutData(rd);

		img = new Image(disp, "resources/exit.png");
		bExit = new Button(c, SWT.PUSH);
		bExit.setImage(img);
		bExit.setToolTipText("Exit dialog");
		bExit.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (unsaved) {
					MessageBox messageBox = new MessageBox (shell, SWT.APPLICATION_MODAL | SWT.YES | SWT.NO);
					messageBox.setText ("Abandon merge contents");
					messageBox.setMessage ("Merged G Code has not been saved.  Continue?");
					if (messageBox.open () == SWT.NO) {
						return;
					}
				}
				shell.dispose();
			}
		});
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bExit.setLayoutData(rd);
		
		gd = new GridData();
		gd.horizontalAlignment = SWT.CENTER;
		c.setLayoutData(gd);
		
		enableButtons();
	}
	
	private void enableButtons() {
		boolean flag = fileCount != 0;
		bSave.setEnabled(flag);
		bView.setEnabled(flag);		
	}
	
	private void saveGCode() {
		String gcd = props.getPropLastGCodeDir();
		FileDialog fd = new FileDialog(shell, SWT.SAVE);
		fd.setText("Save");
		fd.setFilterPath(gcd);
		fd.setOverwrite(true);
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
		
		try {
			FileWriter myWriter = new FileWriter(selected);
			myWriter.write(tGCode.getText());
			myWriter.close();
			
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
			messageBox.setText("Success");
			messageBox.setMessage("File " + selected + " successfully written");
			messageBox.open();
		} catch (IOException e) {	
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("I/O Error");
			messageBox.setMessage("AN I/O Error occurred.  Unable to save G Code.");
			messageBox.open();
			return;
		}

		unsaved = false;
	}
	
	private void viewGCode() {
		GViewer gv = new GViewer(shell, "Merged Files");
		gv.addGCode(tGCode.getText().split("\n"));
	}


}

