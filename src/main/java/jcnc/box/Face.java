package main.java.jcnc.box;

import java.util.ArrayList;
import java.util.List;

import main.java.jcnc.Point2D;

public class Face {
	String name;
	double height, width;
	int hTabCount, wTabCount;
	double hTabLen, wTabLen;
	
	int hTabType, wTabType;
	final int ttSlots = 0;
	final int ttTabs = 1;
	
	int relief;
	final int reliefNone = 0;
	final int reliefH = 1;
	final int reliefW = 2;
	
	double matThk;
	double toolRad;
	
	List<Point2D> hLoTabs = new ArrayList<Point2D>();
	List<Point2D> hHiTabs = new ArrayList<Point2D>();
	List<Point2D> wLoTabs = new ArrayList<Point2D>();
	List<Point2D> wHiTabs = new ArrayList<Point2D>();
	
	public Face(String name, double h, String hTabType, int hTabs, double hTabLen, double w, String wTabType, int wTabs, double wTabLen, double thk, double toolrad, String relief) {
		this.name = name;
		this.height = h;
		this.hTabType = ttTabs;
		if (hTabType.equals("slots"))
			this.hTabType = ttSlots;
		this.hTabCount = hTabs;
		this.hTabLen = hTabLen;
		
		this.width = w;
		this.wTabType = ttTabs;
		if (wTabType.equals("slots"))
			this.wTabType = ttSlots;
		this.wTabCount = wTabs;
		this.wTabLen = wTabLen;

		this.relief = reliefNone;
		if (relief.equals("width"))
			this.relief = reliefW;
		else if (relief.equals("height"))
			this.relief = reliefH;
		
		this.matThk = thk;
		this.toolRad = toolrad;
		calculateTabs();
	}
	
	public void dump() {
		System.out.println("===== " + name + " ====== " + relief + " ============ " + matThk + " ===========================");
		System.out.println(height + ", " + hTabType + ", " + hTabCount + ", " + hTabLen);
		System.out.println(width + ", " + wTabType + ", " + wTabCount + ", " + wTabLen);
	}
	
	public void setHValue(double h) {
		this.height = h;
		calculateTabs();
	}
	
	public void setWValue(double w) {
		this.width = w;
		calculateTabs();
	}
	
	public void setRelief(String r) {
		this.relief = reliefNone;
		if (r.equals("width"))
			this.relief = reliefW;
		else if (r.equals("height"))
			this.relief = reliefH;
		calculateTabs();
	}
	
	public void setToolRad(double r) {
		this.toolRad = r;
		calculateTabs();
	}
	
	public void setMaterialThickness(double t) {
		this.matThk = t;
		calculateTabs();
	}
	
	public void setTabInfo(String tag, TabInfo ti) {
		boolean hTabs = false;
		if (name.equals("Front") || name.equals("Back")) {
			if (tag.contains("LR"))
				hTabs = true;
		} else if (name.equals("Left") || name.equals("Right")) {
			if (tag.contains("FB"))
				hTabs = true;
		} else {
			if (tag.contains("LR"))
				hTabs = true;
		}
		
		if (hTabs) {
			hTabType = ttTabs;
			if (ti.tType.equals("slots"))
				hTabType = ttSlots;
			hTabCount = ti.tCt;
			hTabLen = ti.tLen;		
		}
		else {
			wTabType = ttTabs;
			if (ti.tType.equals("slots"))
				wTabType = ttSlots;
			wTabCount = ti.tCt;
			wTabLen = ti.tLen;		
		}
		calculateTabs();
	}
	
	private void calculateTabs() {
		hLoTabs.clear();
		hHiTabs.clear();
		if (hTabCount > 0) {
			double matAdj = matThk;
			if (hTabType == ttSlots)
				matAdj = -matThk;
			double segLen = height/(hTabCount+1);
			double y;
			for (int i=0; i<hTabCount; i++) {
				y = (i+1)*segLen;
				if (relief == reliefW && hTabType == ttTabs)
					hLoTabs.add(new Point2D(0.0-toolRad, y-hTabLen/2));
				hLoTabs.add(new Point2D(0.0-toolRad, y-hTabLen/2-toolRad));
				if (relief == reliefH && hTabType == ttTabs)
					hLoTabs.add(new Point2D(0.0, y-hTabLen/2-toolRad));
				
				if (relief == reliefH && hTabType == ttSlots)
					hLoTabs.add(new Point2D(0.0-matAdj, y-hTabLen/2-toolRad));
				hLoTabs.add(new Point2D(0.0-toolRad-matAdj, y-hTabLen/2-toolRad));
				if (relief == reliefW && hTabType == ttSlots) {
					hLoTabs.add(new Point2D(0.0-toolRad-matAdj, y-hTabLen/2-toolRad-toolRad));
					hLoTabs.add(new Point2D(0.0-toolRad-matAdj, y+hTabLen/2+toolRad+toolRad));
				}
				hLoTabs.add(new Point2D(0.0-toolRad-matAdj, y+hTabLen/2+toolRad));
				if (relief == reliefH && hTabType == ttSlots)
					hLoTabs.add(new Point2D(0.0-matAdj, y+hTabLen/2+toolRad));
				
				if (relief == reliefH && hTabType == ttTabs)
					hLoTabs.add(new Point2D(0.0, y+hTabLen/2+toolRad));
				hLoTabs.add(new Point2D(0.0-toolRad, y+hTabLen/2+toolRad));
				if (relief == reliefW && hTabType == ttTabs)
					hLoTabs.add(new Point2D(0.0-toolRad, y+hTabLen/2));
			}
			for (int i=hTabCount-1; i>= 0; i--) {
				y = (i+1)*segLen;
				if (relief == reliefW && hTabType == ttTabs)
					hHiTabs.add(new Point2D(width+toolRad, y+hTabLen/2));
				hHiTabs.add(new Point2D(width+toolRad, y+hTabLen/2+toolRad));
				if (relief == reliefH && hTabType == ttTabs)
					hHiTabs.add(new Point2D(width, y+hTabLen/2+toolRad));

				if (relief == reliefH && hTabType == ttSlots)
					hHiTabs.add(new Point2D(width+matAdj, y+hTabLen/2+toolRad));
				hHiTabs.add(new Point2D(width+toolRad+matAdj, y+hTabLen/2+toolRad));
				if (relief == reliefW && hTabType == ttSlots) {
					hHiTabs.add(new Point2D(width+toolRad+matAdj, y+hTabLen/2+toolRad+toolRad));
					hHiTabs.add(new Point2D(width+toolRad+matAdj, y-hTabLen/2-toolRad-toolRad));
				}
				hHiTabs.add(new Point2D(width+toolRad+matAdj, y-hTabLen/2-toolRad));
				if (relief == reliefH && hTabType == ttSlots)
					hHiTabs.add(new Point2D(width+matAdj, y-hTabLen/2-toolRad));
				
				if (relief == reliefH && hTabType == ttTabs)
					hHiTabs.add(new Point2D(width, y-hTabLen/2-toolRad));
				hHiTabs.add(new Point2D(width+toolRad, y-hTabLen/2-toolRad));
				if (relief == reliefW && hTabType == ttTabs)
					hHiTabs.add(new Point2D(width+toolRad, y-hTabLen/2));
			}
		}
		
		wLoTabs.clear();	
		wHiTabs.clear();	
		if (wTabCount > 0) {
			double matAdj = matThk;
			if (wTabType == ttSlots)
				matAdj = -matThk;
			double segLen = width/(wTabCount+1);
			double x;
			for (int i=0; i<wTabCount; i++) {
				x= (i+1)*segLen;
				if (relief == reliefW && wTabType == ttTabs)
					wHiTabs.add(new Point2D(x-wTabLen/2, height+toolRad));
				wHiTabs.add(new Point2D(x-wTabLen/2-toolRad, height+toolRad));
				if (relief == reliefH && wTabType == ttTabs)
					wHiTabs.add(new Point2D(x-wTabLen/2-toolRad, height));
				
				if (relief == reliefH && wTabType == ttSlots) 
					wHiTabs.add(new Point2D(x-wTabLen/2-toolRad, height+matAdj));
				wHiTabs.add(new Point2D(x-wTabLen/2-toolRad, height+toolRad+matAdj));
				if (relief == reliefW && wTabType == ttSlots) {
					wHiTabs.add(new Point2D(x-wTabLen/2-toolRad-toolRad, height+toolRad+matAdj));
					wHiTabs.add(new Point2D(x+wTabLen/2+toolRad+toolRad, height+toolRad+matAdj));
				}
				wHiTabs.add(new Point2D(x+wTabLen/2+toolRad, height+toolRad+matAdj));
				if (relief == reliefH && wTabType == ttSlots) 
					wHiTabs.add(new Point2D(x+wTabLen/2+toolRad, height+matAdj));
				
				if (relief == reliefH && wTabType == ttTabs)
					wHiTabs.add(new Point2D(x+wTabLen/2+toolRad, height));
				wHiTabs.add(new Point2D(x+wTabLen/2+toolRad, height+toolRad));
				if (relief == reliefW && wTabType == ttTabs)
					wHiTabs.add(new Point2D(x+wTabLen/2, height+toolRad));
					
			}
			for (int i=wTabCount-1; i>= 0; i--) {
				x = (i+1)*segLen;
				if (relief == reliefW && wTabType == ttTabs)
					wLoTabs.add(new Point2D(x+wTabLen/2, 0.0-toolRad));
				wLoTabs.add(new Point2D(x+wTabLen/2+toolRad, 0.0-toolRad));
				if (relief == reliefH && wTabType == ttTabs)
					wLoTabs.add(new Point2D(x+wTabLen/2+toolRad, 0.0));

				if (relief == reliefH && wTabType == ttSlots)
					wLoTabs.add(new Point2D(x+wTabLen/2+toolRad, 0.0-matAdj));
				wLoTabs.add(new Point2D(x+wTabLen/2+toolRad, 0.0-toolRad-matAdj));
				if (relief == reliefW && wTabType == ttSlots) {
					wLoTabs.add(new Point2D(x+wTabLen/2+toolRad+toolRad, 0.0-toolRad-matAdj));
					wLoTabs.add(new Point2D(x-wTabLen/2-toolRad-toolRad, 0.0-toolRad-matAdj));				
				}
				wLoTabs.add(new Point2D(x-wTabLen/2-toolRad, 0.0-toolRad-matAdj));
				if (relief == reliefH && wTabType == ttSlots)
					wLoTabs.add(new Point2D(x-wTabLen/2-toolRad, 0.0-matAdj));

				if (relief == reliefH && wTabType == ttTabs)
					wLoTabs.add(new Point2D(x-wTabLen/2-toolRad, 0.0));
				wLoTabs.add(new Point2D(x-wTabLen/2-toolRad, 0.0-toolRad));
				if (relief == reliefW && wTabType == ttTabs)
					wLoTabs.add(new Point2D(x-wTabLen/2, 0.0-toolRad));
			}
		}
	}
	
	public List<Point2D> renderPerimeter() {
		List<Point2D> coords = new ArrayList<Point2D>();
		
		coords.add(new Point2D(0.0-toolRad, 0.0-toolRad));
		coords.addAll(hLoTabs);
		coords.add(new Point2D(0.0-toolRad, height+toolRad));	
		coords.addAll(wHiTabs);
		coords.add(new Point2D(width+toolRad, height+toolRad));	
		coords.addAll(hHiTabs);
		coords.add(new Point2D(width+toolRad, 0.0-toolRad));
		coords.addAll(wLoTabs);
		coords.add(new Point2D(0.0-toolRad, 0.0-toolRad));
		
		return coords;
	}

}
