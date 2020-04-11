package main.java.jcnc;

import java.util.ArrayList;
import java.util.List;

public class Offset {
	
	private class Segment {
		Point2D a, b;
		public Segment(Point2D a, Point2D b) {
			this.a = a;
			this.b = b;
		}
		public String toString() {
			return "A: (" + a.x + ", " + a.y + ") to B: (" + b.x + ", " + b.y + ")";
		}
	}
	public Offset() {
		
	}
	
	Segment offsetSegment(Segment s, double d) {
		// calculate vector
		double[] v = new double[2];
		v[0] = s.b.x - s.a.x;
		v[1] = s.b.y - s.a.y;

		// normalize vector
		double root = Math.sqrt(Math.pow(v[0], 2) + Math.pow(v[1], 2));
		v[0] = v[0]/root;
		v[1] = v[1]/root;
		if (Double.isInfinite(v[0]) || Double.isInfinite(v[1]))
			return null;

		// perpendicular unit vector
		double[] vnp = new double[2];
		vnp[0] = -v[1];
		vnp[1] = v[0];

		Segment result = new Segment(
				new Point2D(s.a.x + d*vnp[0], s.a.y + d*vnp[1]),
				new Point2D(s.b.x + d*vnp[0], s.b.y + d*vnp[1]));
		return result;
	}

	Point2D intersection(Segment s1, Segment s2) {
		double dx = s1.b.x - s1.a.x;
		double dy = s1.b.y - s1.a.y;

		boolean vert1 = false;
		double vertx = 0;
		double m1 = 0;
		double d1 = 0;
		m1 = dy / dx;
		if (Double.isInfinite(m1)) {			
			vert1 = true;
			vertx = s1.a.x;
		}
		else {
			d1 = s1.a.y - s1.a.x * m1;
		}

		dx = s2.b.x - s2.a.x;
		dy = s2.b.y - s2.a.y;

		boolean vert2 = false;
		double m2 = 0;
		double d2 = 0;
		m2 = dy / dx;
		if (Double.isInfinite(m2)) {
			vert2 = true;
			vertx = s2.a.x;			
		}
		else {
			d2 = s2.a.y - s2.a.x * m2;
		}

		double nx = 0;
		double ny = 0;
		if (vert1 && !vert2) {
			nx = vertx;
			ny = m2 * nx + d2;
		}
		else if (vert2 && !vert1) {
			nx = vertx;
			ny = m1 * nx + d1;
		}
		else if ((vert1 && vert2) || (m1 == m2)) {
			// slopes are identical - just return the common point
			if (s1.a.x == s2.b.x  && s1.a.y == s2.b.y) { 
				nx = s1.a.x;
				ny = s1.a.y;
			}
			else if (s1.b.x == s2.a.x && s1.b.y == s2.a.y) {
				nx = s1.b.x;
				ny = s1.b.y;
			}
			else {
				return null;
			}
		}
		else {
			nx = (d1 - d2) / (m2 - m1);
			ny = m1 * nx + d1;
		}	
		return new Point2D(nx, ny);
	}


	public List<Point2D> offsetPath(List<Point2D> pts, double d, boolean closePath) {
		List<Point2D> result = new ArrayList<Point2D>();
		result.clear();
		if (pts.size() == 2) {
			Segment s = offsetSegment(new Segment(pts.get(0), pts.get(1)), d);	
			if (s == null)
				return null;
			result.add(s.a);
			result.add(s.b);
			return result;
		}
		List<Segment> segments = new ArrayList<Segment>();
		segments.clear();
		if (closePath) {
			Point2D plast = pts.get(pts.size()-1);
			segments.add(offsetSegment(new Segment(plast, pts.get(0)), d));
		}

		for (int i=1; i<pts.size(); i++) {
			Segment s = offsetSegment(new Segment(pts.get(i-1), pts.get(i)), d);
			if (s == null)
				return null;
			segments.add(s);
		}
		
		Point2D saveStart = null;
		Point2D saveEnd = null;
		if (!closePath) {
			// save the initial offsets for the first and last points
			// if we are not going to close the path
			saveStart = segments.get(0).a;
			saveEnd = segments.get(segments.size()-1).b;
		}

		List<Point2D> newPoints = new ArrayList<Point2D>();
		if (!closePath)
			newPoints.add(saveStart);

		for (int i = 1; i<segments.size(); i++) {
			Point2D np = intersection(segments.get(i-1), segments.get(i));
			if (np == null)
				return null;
			newPoints.add(np);
		}
		if (closePath) {
			Point2D np = intersection(segments.get(segments.size()-1), segments.get(0));
			if (np == null)
				return null;
			newPoints.add(np);
		}
		if (!closePath)
			newPoints.add(saveEnd);
			
		return newPoints;
	}
}
