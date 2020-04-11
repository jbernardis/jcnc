package main.java.jcnc;

public class Rotator {
	Double cosv, sinv;
	public Rotator(Double angle) {
		cosv = Math.cos(Math.toRadians(angle));
		sinv = Math.sin(Math.toRadians(angle));
	}        
	
	public Point2D rotate(Point2D p) {        
		Double nx = p.x*cosv-p.y*sinv;
		Double ny = p.x*sinv+p.y*cosv;
		return new Point2D(nx, ny);
	}	
}
