package main.java.jcnc;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCMirror {
	Pattern pattXParm = Pattern.compile("(X *-{0,1}\\d*\\.{0,1}\\d+)");
	Pattern pattYParm = Pattern.compile("(Y *-{0,1}\\d*\\.{0,1}\\d+)");
	Pattern pattIParm = Pattern.compile("(I *-{0,1}\\d*\\.{0,1}\\d+)");
	Pattern pattJParm = Pattern.compile("(J *-{0,1}\\d*\\.{0,1}\\d+)");
	
	public GCMirror() {
	}
	
	public String mirrorx(String gl) {
        Matcher matcher = pattYParm.matcher(gl);
        if (matcher.find()) {
            String pstr = matcher.group();
            String tag = pstr.substring(0, 1);
            String sval = pstr.substring(1);
            double val = Double.valueOf(sval);
            
            DecimalFormat fmt = getFormat(sval);
            
            val = -val;
            String newStr = tag + fmt.format(val);
            gl = matcher.replaceFirst(newStr);
        }
        matcher = pattJParm.matcher(gl);
        if (matcher.find()) {
            String pstr = matcher.group();
            String tag = pstr.substring(0, 1);
            String sval = pstr.substring(1);
            double val = Double.valueOf(sval);
            
            DecimalFormat fmt = getFormat(sval);
            
            val = -val;
            String newStr = tag + fmt.format(val);
            gl = matcher.replaceFirst(newStr);
        }
        if (gl.startsWith("G2")) {
        	gl = "G3" + gl.substring(2);
        }
        else if (gl.startsWith("G3")) {
        	gl = "G2" + gl.substring(2);
        }

        return gl;
	}
	
	public String mirrory(String gl) {
        Matcher matcher = pattXParm.matcher(gl);
        if (matcher.find()) {
            String pstr = matcher.group();
            String tag = pstr.substring(0, 1);
            String sval = pstr.substring(1);
            double val = Double.valueOf(sval);
            
            DecimalFormat fmt = getFormat(sval);
            
            val = -val;
            String newStr = tag + fmt.format(val);;
            gl = matcher.replaceFirst(newStr);
        }
        matcher = pattIParm.matcher(gl);
        if (matcher.find()) {
            String pstr = matcher.group();
            String tag = pstr.substring(0, 1);
            String sval = pstr.substring(1);
            double val = Double.valueOf(sval);
            
            DecimalFormat fmt = getFormat(sval);
            
            val = -val;
            String newStr = tag + fmt.format(val);
            gl = matcher.replaceFirst(newStr);
        }
        if (gl.startsWith("G2")) {
        	gl = "G3" + gl.substring(2);
        }
        else if (gl.startsWith("G3")) {
        	gl = "G2" + gl.substring(2);
        }

		return gl;
	}
	
	private DecimalFormat getFormat(String sval) {
		int ix = sval.indexOf(".");
		if (ix < 0) {			
			return new DecimalFormat("####0.0");
		}
		
		int ct = sval.length() - ix - 1;
		if (ct < 1)
			ct = 1;
		if (ct > 5)
			ct = 5;
		return new DecimalFormat("####0." + "0".repeat(ct));
	}
}
