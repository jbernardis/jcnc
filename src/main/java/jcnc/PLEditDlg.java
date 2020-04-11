package main.java.jcnc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class PLEditDlg extends Dialog {
	Shell shell;
	List<Point2D> plist = new ArrayList<Point2D>();

	Table table;
	Text tX, tY;
	Button bUp, bDown, bMoveUp, bMoveDown;
	Button bOK;
	int maxTx;
	int minPoints;
	
	boolean modified;
	String shellTitle;
	int rc;
	
	private VerifyListener verifyDouble = new VerifyListener() {
		@Override
		public void verifyText(VerifyEvent e) {
			Text text = (Text) e.getSource();

			// get old text and create new text by using the VerifyEvent.text
			final String oldS = text.getText();
			String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);

			boolean isDouble = true;
			try {
				Double.parseDouble(newS);
			} catch (NumberFormatException ex) {
				isDouble = false;
			}
			if (!isDouble)
				e.doit = false;
		}
	};
	
	public PLEditDlg(Shell parent, List<Point2D> pplist) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		plist.addAll(pplist);
	}
	
	public int open(int minPoints) {
		// Create the dialog window
		shell = new Shell(getParent(), getStyle());
		shellTitle = "Point List Editor (" + minPoints + " minimum points)  ";
		modified = true;
		this.minPoints = minPoints;
		setModified(false);
		
	    shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				if (modified)
					event.doit = confirmDataLoss();
				else
					event.doit = true;
				
				if (event.doit)
					rc = SWT.CANCEL;
			}
		});
	    
		createContents();

		shell.pack();
		shell.layout(true, true);
		final Point newSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		shell.setSize(newSize);
		shell.open();
		
		rc = SWT.CANCEL;
		
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		return rc;
	}
	
	private void setModified(boolean flag) {
		if (flag == modified)
			return;
		
		modified = flag;
		String t = shellTitle;
		if (modified)
			t = t + " *";
		
		shell.setText(t);
	}
	
	private void createContents() {
		GridLayout gl = new GridLayout();
		gl.numColumns = 6;
		gl.makeColumnsEqualWidth = false;
		gl.verticalSpacing = 10;
		gl.horizontalSpacing = 20;
		shell.setLayout(gl);
		
		table = new Table (shell, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible (true);
		table.setHeaderVisible (true);
		GridData data = new GridData();
		data.heightHint = 200;
		data.horizontalSpan = 4;
		data.verticalSpan = 2;
		data.horizontalIndent = 20;
		table.setLayoutData(data);
		String[] titles = {"", "X", "Y"};
		int[] cwidths = {30, 85, 85};
		for (int i=0; i<titles.length; i++)  {
			TableColumn column = new TableColumn (table, SWT.NONE);
			column.setText (titles[i]);
			column.setWidth(cwidths[i]);
			column.setAlignment(SWT.RIGHT);
		}
		for (int i=0; i<plist.size(); i++) {
			TableItem item = new TableItem (table, SWT.NONE);
			item.setText (0, String.valueOf(i+1));
			Point2D p = plist.get(i);
			item.setText (1, Double.toString(p.x));
			item.setText (2, Double.toString(p.y));
		}
		table.pack();
		maxTx = plist.size();
		if (maxTx > 0)
			table.setSelection(0);
		
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				setXYFields();
				enableNavigationButtons();
			}
		});
		

		Display disp = shell.getDisplay();

		Image img = new Image(disp, "resources/scrollup.png");
		bUp = new Button(shell, SWT.PUSH);
		bUp.setImage(img);
		data = new GridData();
		data.heightHint = 48;
		data.widthHint = 48;
		data.horizontalAlignment = SWT.CENTER;
		data.verticalAlignment = SWT.CENTER;
		data.verticalIndent = 25;
		bUp.setLayoutData(data);
		bUp.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int tx = table.getSelectionIndex();
				table.setSelection(tx-1);
				setXYFields();
				enableNavigationButtons();
			}
		});
		
		img = new Image(disp, "resources/moveup.png");
		bMoveUp = new Button(shell, SWT.PUSH);
		bMoveUp.setImage(img);
		data = new GridData();
		data.heightHint = 48;
		data.widthHint = 48;
		data.horizontalAlignment = SWT.CENTER;
		data.verticalAlignment = SWT.CENTER;
		data.verticalIndent = 25;
		bMoveUp.setLayoutData(data);
		bMoveUp.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int tx = table.getSelectionIndex();
				TableItem tiTo = table.getItem(tx-1);
				TableItem tiFrom = table.getItem(tx);
				String xval = tiFrom.getText(1);
				String yval = tiFrom.getText(2);
				tiFrom.setText(1, tiTo.getText(1));
				tiFrom.setText(2, tiTo.getText(2));
				tiTo.setText(1, xval);
				tiTo.setText(2, yval);
				table.setSelection(tx-1);
				Point2D p = plist.get(tx);
				plist.set(tx,  plist.get(tx-1));
				plist.set(tx-1,  p);
				setXYFields();
				enableNavigationButtons();
			}
		});
		
		img = new Image(disp, "resources/scrolldown.png");
		bDown = new Button(shell, SWT.PUSH);
		bDown.setImage(img);
		data = new GridData();
		data.heightHint = 48;
		data.widthHint = 48;
		data.horizontalAlignment = SWT.CENTER;
		data.verticalAlignment = SWT.CENTER;
		data.verticalIndent = 25;
		bDown.setLayoutData(data);
		bDown.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int tx = table.getSelectionIndex();
				table.setSelection(tx+1);
				setXYFields();
				enableNavigationButtons();
			}
		});
		
		img = new Image(disp, "resources/movedown.png");
		bMoveDown = new Button(shell, SWT.PUSH);
		bMoveDown.setImage(img);
		data = new GridData();
		data.heightHint = 48;
		data.widthHint = 48;
		data.horizontalAlignment = SWT.CENTER;
		data.verticalAlignment = SWT.CENTER;
		data.verticalIndent = 25;
		bMoveDown.setLayoutData(data);
		bMoveDown.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int tx = table.getSelectionIndex();
				TableItem tiTo = table.getItem(tx+1);
				TableItem tiFrom = table.getItem(tx);
				String xval = tiFrom.getText(1);
				String yval = tiFrom.getText(2);
				tiFrom.setText(1, tiTo.getText(1));
				tiFrom.setText(2, tiTo.getText(2));
				tiTo.setText(1, xval);
				tiTo.setText(2, yval);
				table.setSelection(tx+1);
				Point2D p = plist.get(tx);
				plist.set(tx,  plist.get(tx+1));
				plist.set(tx+1,  p);
				setXYFields();
				enableNavigationButtons();
			}
		});
		
		
		enableNavigationButtons();
		
		Label l = new Label(shell, SWT.NONE);
		l.setText("X/Y: ");
		l.setAlignment(SWT.RIGHT);
		GridData gridLabel = new GridData();
		gridLabel.widthHint = 30;
		l.setLayoutData(gridLabel);
		
		tX = new Text(shell, SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
		tX.setText(Double.toString(0));
		GridData gridText = new GridData();
		gridText.widthHint = 50;
		tX.setLayoutData(gridText);
		tX.addVerifyListener(verifyDouble);
		
		tY = new Text(shell, SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
		tY.setText(Double.toString(0));
		gridText = new GridData();
		gridText.widthHint = 50;
		tY.setLayoutData(gridText);
		tY.addVerifyListener(verifyDouble);
		
		img = new Image(disp, "resources/add.png");
		Button bAdd = new Button(shell, SWT.PUSH);
		bAdd.setImage(img);
		data = new GridData();
		data.heightHint = 48;
		data.widthHint = 48;
		data.horizontalAlignment = SWT.CENTER;
		data.verticalAlignment = SWT.CENTER;
		bAdd.setLayoutData(data);
		bAdd.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				double xval = Double.valueOf(tX.getText());
				double yval = Double.valueOf(tY.getText());
				plist.add(new Point2D(xval, yval));
				TableItem item = new TableItem (table, SWT.NONE);
				item.setText (0, String.valueOf(plist.size()));
				item.setText (1, Double.toString(xval));
				item.setText (2, Double.toString(yval));
				table.setSelection(plist.size()-1);
				enableNavigationButtons();
				bOK.setEnabled(plist.size() >= minPoints);
				setModified(true);
			}
		});
		
		img = new Image(disp, "resources/replace.png");
		Button bRepl = new Button(shell, SWT.PUSH);
		bRepl.setImage(img);
		data = new GridData();
		data.heightHint = 48;
		data.widthHint = 48;
		data.horizontalAlignment = SWT.CENTER;
		data.verticalAlignment = SWT.CENTER;
		bRepl.setLayoutData(data);
		bRepl.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				double xval = Double.valueOf(tX.getText());
				double yval = Double.valueOf(tY.getText());
				int tx = table.getSelectionIndex();
				TableItem item = table.getItem(tx);
				item.setText (1, Double.toString(xval));
				item.setText (2, Double.toString(yval));
				plist.set(tx,  new Point2D(xval, yval));
				setModified(true);
			}
		});
		
		img = new Image(disp, "resources/delete.png");
		Button bDel = new Button(shell, SWT.PUSH);
		bDel.setImage(img);
		data = new GridData();
		data.heightHint = 48;
		data.widthHint = 48;
		data.horizontalAlignment = SWT.CENTER;
		data.verticalAlignment = SWT.CENTER;
		bDel.setLayoutData(data);
		bDel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int tx = table.getSelectionIndex();
				table.remove(tx);
				plist.remove(tx);
				for (int i=tx; i<plist.size(); i++) {
					TableItem item = table.getItem(i);
					item.setText (0, String.valueOf(i+1));
				}
				table.setSelection(Math.min(tx, plist.size()-1));
				enableNavigationButtons();
				bOK.setEnabled(plist.size() >= minPoints);
				setModified(true);
			}
		});

		
		Composite c = new Composite(shell, SWT.NONE);
		RowLayout rl = new RowLayout();
		rl.spacing = 20;
		rl.marginWidth = 20;
		rl.marginBottom = 10;
		rl.center = true;
		c.setLayout(rl);
		
		img = new Image(disp, "resources/ok.png");
		bOK = new Button(c, SWT.PUSH);
		bOK.setImage(img);
		RowData rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bOK.setLayoutData(rd);
		bOK.setEnabled(plist.size() >= minPoints);
		bOK.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				rc = SWT.OK;
				shell.dispose();
			}
		});
		
		img = new Image(disp, "resources/cancel.png");
		Button bCancel = new Button(c, SWT.PUSH);
		bCancel.setImage(img);
		rd = new RowData();
		rd.height = 48;
		rd.width = 48;
		bCancel.setLayoutData(rd);
		bCancel.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (modified) {
					if (confirmDataLoss()) {
						rc = SWT.CANCEL;
						shell.dispose();
					}
				}
				else {
					rc = SWT.CANCEL;
					shell.dispose();
				}
			}
		});
		
		GridData cgd = new GridData();
		cgd.horizontalSpan = 6;
		cgd.horizontalAlignment = SWT.CENTER;
		cgd.grabExcessHorizontalSpace = true;
		c.setLayoutData(cgd);
		
		setXYFields();
	}
	
	public List<Point2D> getPoints() {
		return plist;
	}
	
	private boolean confirmDataLoss() {
		MessageBox messageBox = new MessageBox (shell, SWT.APPLICATION_MODAL | SWT.YES | SWT.NO);
		messageBox.setText ("Data Has Changed");
		messageBox.setMessage ("Changes will be lost.  Continue with cancel?");
		if (messageBox.open () == SWT.NO) {
			return false;
		}
		return true;
	}
	
	private void enableNavigationButtons() {
		maxTx = plist.size();

		if (maxTx == 0) {
			bUp.setEnabled(false);
			bMoveUp.setEnabled(false);
			bDown.setEnabled(false);
			bMoveDown.setEnabled(false);
			return;
		}
		
		int tx = table.getSelectionIndex();
		if (tx == 0 && maxTx == 1) {
			bUp.setEnabled(false);
			bMoveUp.setEnabled(false);
			bDown.setEnabled(false);
			bMoveDown.setEnabled(false);
			return;
		}
		
		if (tx == 0) {
			bUp.setEnabled(false);
			bMoveUp.setEnabled(false);
			bDown.setEnabled(true);
			bMoveDown.setEnabled(true);
			return;
		}
		
		if (tx == maxTx-1) {
			bUp.setEnabled(true);
			bMoveUp.setEnabled(true);
			bDown.setEnabled(false);
			bMoveDown.setEnabled(false);
			return;
		}
		
		bUp.setEnabled(true);
		bMoveUp.setEnabled(true);
		bDown.setEnabled(true);
		bMoveDown.setEnabled(true);
	}
	
	private void setXYFields() {
		if (plist.size() == 0)
			return;
		
		TableItem ti = table.getSelection()[0];
		tX.setText(ti.getText(1));
		tY.setText(ti.getText(2));
	}

}
