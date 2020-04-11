package main.java.jcnc.viewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import main.java.jcnc.viewer.CNCPoint;

public class CNC {
	Double curX = 0.0;
	Double curY = 0.0;
	Double curZ = 0.0;
	Double curI = 0.0;
	Double curJ = 0.0;
	Double curK = 0.0;
	char MT_NORMAL = 'n';
	char MT_RAPID = 'r';
	
	Double minX = 99999.0;
	Double minY = 99999.0;
	Double maxX = -99999.0;
	Double maxY = -99999.0;
	
	boolean relative = false;
	boolean metric = true;
	
	String lastCmd = "G1";
	GParse gp;
	List<CNCPoint> pointList = new ArrayList<CNCPoint>();
	
	public CNC() {
		gp = new GParse();
		recordPoint(0.0, 0.0, 0.0, MT_NORMAL);
	}

	public void execute(String gline) {
		HashMap<String, String> res = gp.parseGLine(gline);
		if (res == null)
			return;

		String cmd = res.get("_cmd_");
		if (cmd.equals("G0")) 
			moveFast(res);
		else if (cmd.equals("G1")) 
			moveNormal(res);
		else if (cmd.equals("G2")) 
			arcCW(res);
		else if (cmd.equals("G3")) 
			arcCCW(res);
		else if (cmd.equals("G20")) 
			setInches();
		else if (cmd.equals("G21")) 
			setMillimeters();
		else if (cmd.equals("G28")) 
			home(res);
		else if (cmd.equals("G90")) 
			setAbsolute();
		else if (cmd.equals("G91")) 
			setRelative();
		else if (cmd.equals("G92")) 
			axisReset(res);
		else
			System.out.println("Command not handled (" + cmd + ")");
	}
	
	private void moveFast(HashMap<String, String> parms) {
		lastCmd = "G0";
		move(parms, MT_RAPID);
	}

	private void moveNormal(HashMap<String, String> parms) {
		lastCmd = "G1";
		move(parms, MT_NORMAL);
	}
	
	private void move(HashMap<String, String> parms, char moveType) {
		checkCoords(parms);
		recordPoint(curX, curY, curZ, moveType);
	}
	
	private void arcCW(HashMap<String, String> parms) {
		arc(parms, true);
	}

	private void arcCCW(HashMap<String, String> parms) {
		arc(parms, false);
	}
	
	private void arc(HashMap<String, String> parms, boolean cw) {
		Double x = curX;
		Double y = curY;
		Double z = curZ;
		checkCoords(parms);
	
		Double cx = x + curI;
		Double cy = y + curJ;
		Double cz = z + curK;
		//
		//	calculate radius, start and end angles
		Double dx = x - cx;
		Double dy = y - cy;
		
		Double startang, endang;
		if (dy == 0)
			startang = Math.toRadians(0);
		else if (dx == 0)
			startang = Math.toRadians(90);
		else
			startang = Math.atan(dx/dy);
		
		startang = setQuadrant(startang, dx, dy);
		
		Double rad = Math.sqrt(dx*dx+dy*dy);
	
		dx = curX - cx;
		dy = curY - cy;
		if (dy == 0)
			endang = Math.toRadians(0);
		else if (dx == 0)
			endang = Math.toRadians(90);
		else
			endang = Math.atan(dx/dy);
		
		endang = setQuadrant(endang, dx, dy);

		drawArc(cx, cy, cz, rad, startang, endang, cw, 20);
	}
		
	private void setInches() {
		metric = false;
	}
	
	private void setMillimeters() {
		metric = true;
	}
	
	private void setAbsolute() {
		relative = false;
	}
	
	private void setRelative() {
		relative = true;
	}
	
	private void home(HashMap<String, String> parms) {
		int naxes = 0;
		if (parms.containsKey("X")) {
			curX = 0.0;
			naxes++;
		}
		if (parms.containsKey("Y")) {
			curY = 0.0;
			naxes++;
		}
		if (parms.containsKey("Z")) {
			curZ = 0.0;
			naxes++;
		}
		if (naxes == 0) {
			curX = 0.0;
			curY = 0.0;
			curZ = 0.0;
		}
		recordPoint(curX, curY, curZ, MT_NORMAL);
	}

	private void axisReset(HashMap<String, String> parms) {
		if (parms.containsKey("X"))
			curX = Double.valueOf(parms.get("X"));
		if (parms.containsKey("Y"))
			curY = Double.valueOf(parms.get("Y"));
		if (parms.containsKey("Z"))
			curZ = Double.valueOf(parms.get("Z"));
	}
	
	private void checkCoords(HashMap<String, String> parms) {
		if (relative) {
			if (parms.containsKey("X"))
				curX = curX + Double.valueOf(parms.get("X"));
			if (parms.containsKey("Y"))
				curY = curY + Double.valueOf(parms.get("Y"));
			if (parms.containsKey("Z"))
				curZ = curZ + Double.valueOf(parms.get("Z"));
		}
		else {
			if (parms.containsKey("X"))
				curX = Double.valueOf(parms.get("X"));
			if (parms.containsKey("Y"))
				curY = Double.valueOf(parms.get("Y"));
			if (parms.containsKey("Z"))
				curZ = Double.valueOf(parms.get("Z"));
		}
		
	
		curI = 0.0;
		curJ = 0.0;
		curK = 0.0;
		
		if (parms.containsKey("I"))
			curI = Double.valueOf(parms.get("I"));
		if (parms.containsKey("J"))
			curJ = Double.valueOf(parms.get("J"));
		if (parms.containsKey("K"))
			curK = Double.valueOf(parms.get("K"));
	}
	
	private Double setQuadrant(Double sa, Double dx, Double dy) {
		Double a = Math.toDegrees(sa);
		if (dy >= 0) {
			while (a < 0.0)
				a = a + 180;
			while (a > 180)
				a = a - 180;
		}
		else {
			while (a < 180)
				a = a + 180;
			while (a > 360)
				a = a - 180;
		}
			
		if (dx <= 0) {
			while (a > 270)
				a = a - 180;
			while (a < 90)
				a = a + 180;
		}
		else {
			while (a > 450)
				a = a - 180;
			while (a < 270)
				a = a + 180;
			if (a >= 360)
				a = a - 360;
		}
		
		return Math.toRadians(a);
	}
	
	private void drawArc(Double cx, Double cy, Double cz, Double rad, Double angstart, Double angend, boolean cw, int numsegments) {
		int sign;
		Double angdist;
		if (cw) {
			sign = -1;
			angdist = angstart - angend;
			while (angdist <= 0)
				angdist = angdist + 2 * Math.PI;
		}
		else {
			sign = 1;
			angdist = angend - angstart;
			while (angdist <= 0)
				angdist = angdist + 2*Math.PI;
		}

		int segs = (int) Math.toDegrees(angdist)/4;
		if (segs < numsegments)
			segs = numsegments;
			
		for (int i = 0; i<=segs; i++) { 
			Double theta = angstart + sign * (angdist * ((float) i) / ((float) segs));
			while (theta < 0)
				theta = theta + 2*Math.PI;

			Double dx = rad * Math.cos(theta);
			Double dy = rad * Math.sin(theta);

			recordPoint(cx + dx, cy + dy, cz, MT_NORMAL);
		}
	}

	
	private void recordPoint(Double x, Double y, Double z, char moveType) {
		pointList.add(new CNCPoint(x, y, z, moveType));
		if (x < minX) minX = x;
		if (x > maxX) maxX = x;
		if (y < minY) minY = y;
		if (y > maxY) maxY = y;
	}
	
	public Double[] getMinMax() {
		Double[] vals = new Double[4];
		vals[0] = minX;
		vals[1] = minY;
		vals[2] = maxX;
		vals[3] = maxY;
		
		return vals;		
	}
	
	public List<CNCPoint> getPoints() {
		return pointList;
	}

}
