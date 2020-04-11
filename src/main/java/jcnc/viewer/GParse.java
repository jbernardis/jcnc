package main.java.jcnc.viewer;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GParse {
	String lastCmd;
	Pattern pattCommand = Pattern.compile("^([GMT]\\d+)");
	Pattern pattParm = Pattern.compile("([XYZFIJK] *-{0,1}\\d*\\.{0,1}\\d+)");
	
	public GParse() {
		lastCmd = null;
	}
	
	public HashMap<String, String> parseGLine(String gline) {
		String cmd;
		
		HashMap<String, String> results = new HashMap<String, String>();
		results.clear();
		
		String gl = gline.trim();
		if (gl.contains("(")) {
			gl = gl.split("\\(", 2)[0];
		}
		else if (gl.contains(";")) {
			gl = gl.split(";", 2)[0];
		}
		else if (gl.contains("%")) {
			gl = gl.split("%", 2)[0];
		}
		gl = gl.trim();
		
		if (gl.length() == 0) {
			return null;
		}
		
        Matcher matcher = pattCommand.matcher(gl);
        if (matcher.find()) {
        	cmd = matcher.group();
        	if (cmd.equals("G1") || cmd.equals("G01") || cmd.equals("G0") || cmd.equals("G00")) {
        		lastCmd = cmd;       		
        	}
        }
        else {
        	if (lastCmd == null) {
        		System.out.println("Error - no last command");
        		return null;
        	}
        	else {
        		cmd = lastCmd;
        	}
        }
        results.put("_cmd_", cmd);
        
        matcher = pattParm.matcher(gl);
        while (matcher.find()) {
            String pstr = matcher.group();
            String tag = pstr.substring(0, 1);
            String val = pstr.substring(1);
            results.put(tag,  val);
         }
		
		return results;
	}
}
