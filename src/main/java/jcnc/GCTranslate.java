package main.java.jcnc;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCTranslate {
	Pattern pattXParm = Pattern.compile("(X *-{0,1}\\d*\\.{0,1}\\d+)");
	Pattern pattYParm = Pattern.compile("(Y *-{0,1}\\d*\\.{0,1}\\d+)");
	
	double dx, dy;
	
	public GCTranslate(double dx, double dy) {
		this.dx = dx;
		this.dy = dy;
	}
	
	public String shift(String gl) {
		if (dx != 0)
			gl = shiftx(gl);
		
		if (dy != 0)
			gl = shifty(gl);
		
		return gl;
	}
	
	private String shiftx(String gl) {
        Matcher matcher = pattXParm.matcher(gl);
        if (matcher.find()) {
            String pstr = matcher.group();
            String tag = pstr.substring(0, 1);
            String sval = pstr.substring(1);
            double val = Double.valueOf(sval);
            
            DecimalFormat fmt = getFormat(sval);
            
            val = val + dx;
            String newStr = tag + fmt.format(val);
            gl = matcher.replaceFirst(newStr);
        }

        return gl;
	}
	
	private String shifty(String gl) {
        Matcher matcher = pattYParm.matcher(gl);
        if (matcher.find()) {
            String pstr = matcher.group();
            String tag = pstr.substring(0, 1);
            String sval = pstr.substring(1);
            double val = Double.valueOf(sval);
            
            DecimalFormat fmt = getFormat(sval);
            
            val = val + dy;
            String newStr = tag + fmt.format(val);;
            gl = matcher.replaceFirst(newStr);
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


