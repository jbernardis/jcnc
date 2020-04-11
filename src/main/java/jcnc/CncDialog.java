package main.java.jcnc;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import main.java.jcnc.viewer.GViewer;

public class CncDialog extends Dialog {
	protected Shell shell;
	protected static int seqNo;
	protected String objectName;

	protected CncProperties props;
	protected String currentTool;
	protected HashMap<String, Object> toolInfo;
	protected String currentMaterial;
	protected HashMap<String, Object> toolMaterialInfo;

	protected boolean modified;
	protected boolean unsaved;
	protected boolean GCStale;
	protected boolean haveGCode;

    protected HashMap<String, Text> JSONText = new HashMap<String, Text>();
    protected List<String> JSONAllKeys = new ArrayList<String>();
    
	protected Text tXYNormalSpeed;
	protected Text tXYRapidSpeed;
	protected Text tZNormalSpeed;
	protected Text tZRapidSpeed;
	
	protected Button bAddSpeed;
	protected boolean addSpeedTerm;

	int decimals;
	String fmtDouble;

	protected VerifyListener verifyDouble = new VerifyListener() {
		@Override
		public void verifyText(VerifyEvent e) {
			setModified(true);
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

	protected VerifyListener verifyInt = new VerifyListener() {
		@Override
		public void verifyText(VerifyEvent e) {
			setModified(true);
			Text text = (Text) e.getSource();

			// get old text and create new text by using the VerifyEvent.text
			final String oldS = text.getText();
			String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);

			boolean isInt = true;
			try {
				Integer.parseInt(newS);
			} catch (NumberFormatException ex) {
				isInt = false;
			}
			if (!isInt)
				e.doit = false;
		}
	};

	@SuppressWarnings("unchecked")
	public CncDialog(Shell parent, CncProperties props) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.props = props;
		seqNo++;
		modified = false;
		GCStale = false;
		haveGCode = false;
		unsaved = false;
		currentMaterial = props.getCurrentMaterial();
		currentTool = props.getCurrentTool();
		toolInfo = props.getTool(currentTool);
		HashMap<String, Object> speeds = (HashMap<String, Object>) toolInfo.get("speeds");
		toolMaterialInfo = (HashMap<String, Object>) speeds.get(currentMaterial);
		
		decimals = 3;
		fmtDouble = "####0." + "0".repeat(decimals);
	}

	protected void open() {
		// Create the dialog window
		shell = new Shell(getParent(), getStyle());
	    shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				if (modified) {
					MessageBox messageBox = new MessageBox (shell, SWT.APPLICATION_MODAL | SWT.YES | SWT.NO);
					messageBox.setText ("Data Has Changed");
					messageBox.setMessage ("G Code not generated.  Continue with exit?");
					if (messageBox.open () == SWT.NO) {
						event.doit = false;
						return;
					}
				}
				if (unsaved) {
					MessageBox messageBox = new MessageBox (shell, SWT.APPLICATION_MODAL | SWT.YES | SWT.NO);
					messageBox.setText ("Unsaved G Code");
					messageBox.setMessage ("G Code will be lost.  Continue with exit?");
					if (messageBox.open () == SWT.NO) {
						event.doit = false;
						return;
					}
				}
				event.doit = true;
			}
		});

		setTitle();
	}
		
	protected void dlgLoop() {
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

	protected Text addDoubleText(double iValue) {
		Text t = new Text(shell, SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
		t.setText(Double.toString(iValue));
		GridData gridText = new GridData();
		gridText.widthHint = 50;
		t.setLayoutData(gridText);
		t.addVerifyListener(verifyDouble);

		return t;
	}

	protected Text addIntText(int iValue) {
		Text t = new Text(shell, SWT.RIGHT | SWT.BORDER | SWT.SINGLE);
		t.setText(Integer.toString(iValue));
		GridData gridText = new GridData();
		gridText.widthHint = 50;
		t.setLayoutData(gridText);
		t.addVerifyListener(verifyInt);

		return t;
	}

	protected Label addLabel(String text) {
		Label l = new Label(shell, SWT.NONE);
		l.setText(text);
		l.setAlignment(SWT.RIGHT);
		GridData gridLabel = new GridData();
		gridLabel.widthHint = 110;
		l.setLayoutData(gridLabel);

		return l;
	}

	protected void setModified(boolean flag) {
		modified = flag;
		if (modified && haveGCode)
			GCStale = true;
		setTitle();
	}
	
	protected void setTitle() {
		String title = "G Code Generator - " + objectName + " - ";
		if (modified)
			title = title + "mod/";
		else
			title = title + "   /";
			
		if (GCStale)
			title = title + "stale/";
		else
			title = title + "     /";
		
		if (unsaved)
			title = title + "unsaved";

		shell.setText(title);
	}
	
	protected void enableSpeedControls() {
		tXYNormalSpeed.setEnabled(addSpeedTerm);
		tZNormalSpeed.setEnabled(addSpeedTerm);
		tXYRapidSpeed.setEnabled(addSpeedTerm);
		tZRapidSpeed.setEnabled(addSpeedTerm);
	}

	protected void viewGCode(Text tGCode) {
		if (GCStale) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);

			messageBox.setText("Warning - Stale G Code");
			messageBox.setMessage("Content has changed since last generate.  Continue with view?");
			int buttonID = messageBox.open();
			switch (buttonID) {
			case SWT.YES:
				break;
			case SWT.NO:
				return;
			}
		}
		
		GViewer gv = new GViewer(shell, objectName);
		gv.addGCode(tGCode.getText().split("\n"));
	}

	protected void saveGCode(Text tGCode) {
		if (GCStale) {

			MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);

			messageBox.setText("Warning - Stale G Code");
			messageBox.setMessage("Content has changed since last generate.  Continue with save?");
			int buttonID = messageBox.open();
			switch (buttonID) {
			case SWT.YES:
				break;
			case SWT.NO:
				return;
			}
		}

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
		setModified(false);
	}
	
	
	public String fmtDbl(Double val) {
		DecimalFormat df2 = new DecimalFormat(fmtDouble);
		return df2.format(val);
	}

	public boolean checkJSONKeys(JSONObject j, List<String> keys) {
		Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
        	String k = it.next();
        	if (!j.containsKey(k))
        		return false;
        }
		return true;
	}
	
	public JSONObject retrieveJSONFile(List<String> keys) {
		String jd = props.getPropLastJSONDir();
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		fd.setText("Load Data");
		fd.setFilterPath(jd);
		String[] filterExt = { "*.json", "*.*" };
		fd.setFilterExtensions(filterExt);
		String selected = fd.open();
		if (selected == null)
			return null;

		int n = selected.lastIndexOf("\\");
		String dname;
		if (n < 0) {
			dname = "";
		}
		else {
			dname = selected.substring(0, n+1);
		}

		if (!dname.equals(jd)) {
			props.setPropLastJSONDir(dname);
		}
		
        JSONParser parser = new JSONParser();
        JSONObject objData;

        try (Reader reader = new FileReader(selected)) {
            objData = (JSONObject) parser.parse(reader);
        } catch (IOException e) {
    		MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
    		messageBox.setText("I/O EWrror");
    		messageBox.setMessage("An I/O Error ocurred reading file " + selected);
    		messageBox.open();
            return null;
        } catch (ParseException e) {
    		MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
    		messageBox.setText("Invalid JSON");
    		messageBox.setMessage("Unable to parse JSON from file " + selected);
    		messageBox.open();
            return null;
        }
        
        boolean rc = checkJSONKeys(objData, keys);
        if (rc)
        	return objData;
        
		MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		messageBox.setText("Missing fields");
		messageBox.setMessage("File " + selected + " does not have all required fields");
		messageBox.open();
       
        return null;
	
	}
	
	public boolean saveJSONFile(JSONObject j) {
		String jd = props.getPropLastJSONDir();
		FileDialog fd = new FileDialog(shell, SWT.SAVE);
		fd.setText("Save Data");
		fd.setFilterPath(jd);
		fd.setOverwrite(true);
		String[] filterExt = { "*.json", "*.*" };
		fd.setFilterExtensions(filterExt);
		String selected = fd.open();
		if (selected == null)
			return false;

		int n = selected.lastIndexOf("\\");
		String dname;
		if (n < 0) {
			dname = "";
		}
		else {
			dname = selected.substring(0, n+1);
		}

		if (!dname.equals(jd)) {
			props.setPropLastJSONDir(dname);
		}
		
		Writer writer = new JSONWriter(); 
		try {
			j.writeJSONString(writer);
		}
		catch (IOException e) {
    		MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
    		messageBox.setText("I/O EWrror");
    		messageBox.setMessage("An I/O Error ocurred writing file " + selected);
    		messageBox.open();
            return false;
		}
		
		try {
			FileWriter myWriter = new FileWriter(selected);
			myWriter.write(writer.toString());
			myWriter.close();
		} catch (IOException e) {
    		MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
    		messageBox.setText("I/O EWrror");
    		messageBox.setMessage("An I/O Error ocurred writing file " + selected);
    		messageBox.open();
            return false;
		}
		
		MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
		messageBox.setText("Success");
		messageBox.setMessage("File " + selected + " successfully written");
		messageBox.open();

		return true;
	}
	
	public String moveZ(Double zval, String cmd, String st) {
		return cmd + " Z" + fmtDbl(zval) + st + "\n";
	}
	
	public String moveXY(Double xval, Double yval, String cmd, String st) {
		return cmd + " X" + fmtDbl(xval) + " Y" + fmtDbl(yval) + st + "\n";
	}


}
