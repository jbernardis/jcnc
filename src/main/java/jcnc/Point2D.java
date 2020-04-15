package main.java.jcnc;

public class Point2D {
	public Double x, y;
	
	public Point2D(Double px, Double py) {
		x = px;
		y = py;
	}
	
	public String toString() {
		return "PT:(" + x + ", " + y + ")";
	}
}

