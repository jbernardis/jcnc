package main.java.jcnc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

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

public class AddFile extends Dialog{
	Shell shell;
	CncProperties props;
	String dialogTitle = "Add File";
	
	Text tGCode;
	Button bOpen, bEdit, bView, bMerge, bCancel;
	
	boolean haveFile;
	String fileName;
	String[] gcode;

	public AddFile(Shell parent, CncProperties props) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.props = props;	
		this.haveFile = false;
	}

	public boolean open() {
		shell = new Shell(getParent(), getStyle());
		setTitle();
	    shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				if (haveFile) {
					MessageBox messageBox = new MessageBox (shell, SWT.APPLICATION_MODAL | SWT.YES | SWT.NO);
					messageBox.setText ("Abandon file");
					messageBox.setMessage ("G Code will not be merged.  Continue?");
					if (messageBox.open () == SWT.NO) {
						event.doit = false;
						return;
					}
					haveFile = false;
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
		return haveFile;
	}

	private void setTitle() {
		String title = dialogTitle;
		if (haveFile)
			title = title + " - " + fileName;
		shell.setText(title);
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

		Image img = new Image(disp, "resources/fileopen.png");
		bOpen = new Button(c, SWT.PUSH);
		bOpen.setImage(img);
		bOpen.setToolTipText("Open a new file");
		bOpen.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				fileOpen();
			}
		});
		RowData rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bOpen.setLayoutData(rd);

		img = new Image(disp, "resources/edit.png");
		bEdit = new Button(c, SWT.PUSH);
		bEdit.setImage(img);
		bEdit.setToolTipText("Edit file");
		bEdit.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				EditGCode editDlg = new EditGCode(shell);
				String cmd = editDlg.open();
				if (cmd == null)
					return;
				
				System.out.println("Command from edit dlg: (" + cmd + ")");
				String[] cstr = cmd.split(";");
				if (cstr[0].equals("mx")) {
					System.out.println("Mirror X");					
					GCMirror mir = new GCMirror();
					String[] gl = tGCode.getText().split("\n");
					tGCode.setText("");
					for (String g : gl) {
						tGCode.append(mir.mirrorx(g) + "\n");
					}
				}
				else if (cstr[0].equals("my")) {
					System.out.println("Mirror Y");
					GCMirror mir = new GCMirror();
					String[] gl = tGCode.getText().split("\n");
					tGCode.setText("");
					for (String g : gl) {
						tGCode.append(mir.mirrory(g) + "\n");
					}
				}
				else if (cstr[0].equals("tl")) {
					if (cstr.length != 3) {
						System.out.println("Unexpected size for command array");
						return;
					}
					double dx = Double.valueOf(cstr[1]);
					double dy = Double.valueOf(cstr[2]);
					GCTranslate tl = new GCTranslate(dx, dy);
					String[] gl = tGCode.getText().split("\n");
					tGCode.setText("");
					for (String g : gl) {
						//String g2 = tl.shift(g);
						tGCode.append(tl.shift(g) + "\n");
					}
				}
			}
		});
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bEdit.setLayoutData(rd);

		img = new Image(disp, "resources/view.png");
		bView = new Button(c, SWT.PUSH);
		bView.setImage(img);
		bView.setToolTipText("View the merged files");
		bView.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				viewFile();
			}
		});
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bView.setLayoutData(rd);

		img = new Image(disp, "resources/merge.png");
		bMerge = new Button(c, SWT.PUSH);
		bMerge.setImage(img);
		bMerge.setToolTipText("Return and add this file to the merge");
		bMerge.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				gcode = tGCode.getText().split("\n");
				shell.dispose();
			}
		});
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bMerge.setLayoutData(rd);
		
		img = new Image(disp, "resources/cancel.png");
		bCancel = new Button(c, SWT.PUSH);
		bCancel.setImage(img);
		bCancel.setToolTipText("Cancel");
		bCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (haveFile) {
					MessageBox messageBox = new MessageBox (shell, SWT.APPLICATION_MODAL | SWT.YES | SWT.NO);
					messageBox.setText ("Abandon file");
					messageBox.setMessage ("G Code will not be merged.  Continue?");
					if (messageBox.open () == SWT.NO) {
						event.doit = false;
						return;
					}
					haveFile = false;
				}
				event.doit = true;
				shell.dispose();
			}
		});
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bCancel.setLayoutData(rd);
		
		gd = new GridData();
		gd.horizontalAlignment = SWT.CENTER;
		c.setLayoutData(gd);
		
		enableButtons();
	}
	
	private void enableButtons() {
		bEdit.setEnabled(haveFile);
		bView.setEnabled(haveFile);		
		bMerge.setEnabled(haveFile);		
	}
	
	public String[] retrieveGCode() {
		return gcode;
	}
	
	private void fileOpen() {
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

		tGCode.setText("");
		haveFile = false;
		enableButtons();

		try {
			Scanner sc = new Scanner(new File(selected));
			while (sc.hasNextLine()) {
			  tGCode.append(sc.nextLine() + "\n");
			}
			sc.close();
			haveFile = true;
			fileName = selected;
			setTitle();
			enableButtons();
		}
		catch (FileNotFoundException e) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			messageBox.setText(" File Open Error");
			messageBox.setMessage("Unable to open file\n\"" + selected + "\".");
			messageBox.open();
			return;
		}
	}
	
	private void viewFile() {		
		GViewer gv = new GViewer(shell, fileName);
		gv.addGCode(tGCode.getText().split("\n"));
	}
}
