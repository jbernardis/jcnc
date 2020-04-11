package main.java.jcnc.viewer;

public class CNCPoint {
	Double x, y, z;
	char mtype;
	
	public CNCPoint(Double px, Double py, Double pz, char ptype) {
		x = px;
		y = py;
		z = pz;
		mtype = ptype;
	}
	
	public Double getX() {
		return x;
	}
	
	public Double getY() {
		return y;
	}
	
	public Double getZ() {
		return z;
	}

	public char getType() {
		return mtype;
	}
}
