package main.java.jcnc.box;

import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import main.java.jcnc.Point2D;

public class Visualizer {
	Composite cmp;
	Canvas canvas;
	boolean rendered;
	
	double height, width, depth, wall, toolrad;
	String relief = "";

	Color white, black, dimGray, brtGray, red, blue, green;
	
	double scale = 1.0;
	double zoom = 1.0;
	final double MAXZOOM = 10.0;
	final double MINZOOM = 0.5;
	final double ZOOMDELTA = 0.1;
	
	boolean lbuttondown, rbuttondown;
	boolean fDrawGrid = true;
	
	Point2D offset = new Point2D(-50.0, 50.0);
	Point2D start = new Point2D(0.0, 0.0);
	Point2D startOffset = new Point2D(0.0, 0.0);
	Point2D buildArea = new Point2D(500.0, 500.0);
	Point2D shift = new Point2D(0.0, 0.0);
	
	String currentFace = "Front";
	
	HashMap<String, TabInfo> tabType = new HashMap<String, TabInfo>();
	HashMap<String, Face> faceMap = new HashMap<String, Face>();

	
	public Visualizer() {
		rendered = false;
		tabType.put("FBLR", new TabInfo("slots", 0, 10.0));
		tabType.put("FBTB", new TabInfo("slots", 0, 10.0));
		tabType.put("LRTB", new TabInfo("slots", 0, 10.0));
	}
	
	public void setHeight(double h) {
		if (h == this.height)
			return;
		
		this.height = h;
		if (rendered) {
			Face ff = faceMap.get("Front");
			Face fb = faceMap.get("Back");
			ff.setHValue(h);
			fb.setHValue(h);
			
			Face fl = faceMap.get("Left");
			Face fr = faceMap.get("Right");
			fl.setHValue(h);
			fr.setHValue(h);
			
			if (currentFace.equals("Front") || currentFace.equals("Back") || currentFace.equals("Left") || currentFace.equals("Right")) 
				canvas.redraw();
		}
	}
	
	public void setWidth(double w) {
		if (w == this.width)
			return;
		
		this.width = w;
		if (rendered) {
			Face ff = faceMap.get("Front");
			Face fb = faceMap.get("Back");
			ff.setWValue(w);
			fb.setWValue(w);
			
			Face fl = faceMap.get("Top");
			Face fr = faceMap.get("Bottom");
			fl.setWValue(w);
			fr.setWValue(w);
			
			if (currentFace.equals("Front") || currentFace.equals("Back") || currentFace.equals("Top") || currentFace.equals("Bottom")) 
				canvas.redraw();
		}
	}
	
	public void setDepth(double d) {
		if (d == this.depth)
			return;
		
		this.depth = d;
		if (rendered) {
			Face ff = faceMap.get("Left");
			Face fb = faceMap.get("Right");
			ff.setWValue(d);
			fb.setWValue(d);
			
			Face fl = faceMap.get("Top");
			Face fr = faceMap.get("Bottom");
			fl.setHValue(d);
			fr.setHValue(d);
			
			if (currentFace.equals("Left") || currentFace.equals("Right") || currentFace.equals("Top") || currentFace.equals("Bottom")) 
				canvas.redraw();
		}
	}
	
	public void setWall(double w) {
		if (this.wall == w)
			return;
		
		this.wall = w;
		for (String k: faceMap.keySet()) {
            Face f = faceMap.get(k);
            f.setMaterialThickness(w);
		}
		if (rendered) 
			canvas.redraw();
	}
	
	public void setToolRad(double r) {
		if (this.toolrad == r)
			return;
		
		this.toolrad = r;
		for (String k: faceMap.keySet()) {
            Face f = faceMap.get(k);
            f.setToolRad(r);
		}
		if (rendered) 
			canvas.redraw();
	}
	
	public void setRelief(String r) {
		if (this.relief.equals(r))
			return;
		
		this.relief = r;
		for (String k: faceMap.keySet()) {
            Face f = faceMap.get(k);
            f.setRelief(r);
		}
		
		if (rendered) 
			canvas.redraw();
	}
	
	public void setSlotsTabs(String value) {
		String[] vals = value.split(":");
		TabInfo ti = tabType.get(vals[0]);
		ti.tType = vals[1];
		tabType.put(vals[0],  ti);
		if (rendered) {
			updateFaces(vals[0], ti);
		}
	}
	
	public void setNum(String tag, int value) {
		TabInfo ti = tabType.get(tag);
		ti.tCt = value;
		tabType.put(tag, ti);
		if (rendered) {
			updateFaces(tag, ti);
		}
	}
	
	public void setTabWidth(String tag, double value) {
		TabInfo ti = tabType.get(tag);
		ti.tLen = value;
		tabType.put(tag, ti);
		if (rendered) {
			updateFaces(tag, ti);
		}
	}
	
	public void setDrawGrid(boolean flag) {
		fDrawGrid = flag;
		if (rendered)
			canvas.redraw();
	}
	
	private String oppositeTabType(String tt) {
		if (tt.equals("slots"))
			return "tabs";
		
		return "slots";
	}
	
	private void updateFaces(String tag, TabInfo ti) {
		if (tag.equals("FBLR")) {
			faceMap.get("Front").setTabInfo(tag, ti);
			faceMap.get("Back").setTabInfo(tag, ti);
			String ttype = oppositeTabType(ti.tType);
			TabInfo ti2 = new TabInfo(ttype, ti.tCt, ti.tLen);
			faceMap.get("Left").setTabInfo(tag, ti2);
			faceMap.get("Right").setTabInfo(tag, ti2);
		}
		else if (tag.equals("FBTB")) {
			faceMap.get("Front").setTabInfo(tag, ti);
			faceMap.get("Back").setTabInfo(tag, ti);
			String ttype = oppositeTabType(ti.tType);
			TabInfo ti2 = new TabInfo(ttype, ti.tCt, ti.tLen);
			faceMap.get("Top").setTabInfo(tag, ti2);
			faceMap.get("Bottom").setTabInfo(tag, ti2);
		}
		else if (tag.equals("LRTB")) {
			faceMap.get("Left").setTabInfo(tag, ti);
			faceMap.get("Right").setTabInfo(tag, ti);
			String ttype = oppositeTabType(ti.tType);
			TabInfo ti2 = new TabInfo(ttype, ti.tCt, ti.tLen);
			faceMap.get("Top").setTabInfo(tag, ti2);
			faceMap.get("Bottom").setTabInfo(tag, ti2);
		}
		canvas.redraw();
	}
	
	public void selectFace(String fn) {
		if (currentFace.equals(fn))
			return;
		
		currentFace = fn;
		if (rendered)
			canvas.redraw();
	}
	
	private void resetView() {
		zoom = 1.0;
		offset.x = -50.0;
		offset.y = 50.0;
		shift.x = 0.0;
		shift.y = 0.0;
		
		canvas.redraw();
	}
	
	public void zoomIn() {
		if (zoom+ZOOMDELTA <= MAXZOOM) 
			setZoom(zoom + ZOOMDELTA);
	}

	public void zoomOut() {
		if (zoom-ZOOMDELTA >= MINZOOM)
			setZoom(zoom-ZOOMDELTA);
	}
	
	private void setZoom(double z) {
		double oldzoom, cx, cy;
		if (z > zoom) {
			oldzoom = zoom;
			zoom = z;
			cx = offset.x + buildArea.x/oldzoom/2.0;
			cy = offset.y - buildArea.y/oldzoom/2.0;
			offset.x = cx - buildArea.x/zoom/2.0;
			offset.y = cy + buildArea.y/zoom/2.0;
		}
		else {
			oldzoom = zoom;
			zoom = z;
			cx = offset.x + buildArea.x/oldzoom/2.0;
			cy = offset.y - buildArea.y/oldzoom/2.0;
			offset.x = cx - buildArea.x/zoom/2.0;
			offset.y = cy + buildArea.y/zoom/2.0;
		}
		
		if (offset.x < -buildArea.x)
			offset.x = -buildArea.x;
		if (offset.x > (buildArea.x-buildArea.x/zoom))
			offset.x = buildArea.x-buildArea.x/zoom;
			
		if (offset.y < (-buildArea.y+buildArea.y/zoom))
			offset.y = -buildArea.y+buildArea.y/zoom;
		if (offset.y > buildArea.y)
			offset.y = buildArea.y;

		canvas.redraw();
	}

	
	public Point2D  transform(Point2D pt) {
		double x = (pt.x - offset.x)*zoom*scale;
		double y = (pt.y + offset.y)*zoom*scale;
		return new Point2D(x+shift.x, buildArea.y-(y+shift.y));
	}
	
	public Canvas render(Composite c, Display d) {
		lbuttondown = false;
		rbuttondown = false;
		
		Device device = Display.getCurrent();
		
		red = new Color (device, 255, 0, 0);
		green = new Color (device, 0, 255, 0);
		blue = new Color (device, 0, 0, 255);
		white = d.getSystemColor(SWT.COLOR_WHITE);
		black = d.getSystemColor(SWT.COLOR_BLACK);
		dimGray = new Color(device, 25, 25, 25);
		brtGray = new Color(device, 100, 100, 100);

		TabInfo FBLR = tabType.get("FBLR");
		TabInfo FBTB = tabType.get("FBTB");
		TabInfo LRTB = tabType.get("LRTB");
		faceMap.put("Front", new Face("Front", height, FBLR.tType, FBLR.tCt, FBLR.tLen, width, FBTB.tType, FBTB.tCt, FBTB.tLen, wall, toolrad, relief));
		faceMap.put("Back", new Face("Back", height, FBLR.tType, FBLR.tCt, FBLR.tLen, width, FBTB.tType, FBTB.tCt, FBTB.tLen, wall, toolrad, relief));

		String htt = oppositeTabType(FBLR.tType);
		faceMap.put("Left", new Face("Left", height, htt, FBLR.tCt, FBLR.tLen, depth, LRTB.tType, LRTB.tCt, LRTB.tLen, wall, toolrad, relief));
		faceMap.put("Right", new Face("Right", height, htt, FBLR.tCt, FBLR.tLen, depth, LRTB.tType, LRTB.tCt, LRTB.tLen, wall, toolrad, relief));

		htt = oppositeTabType(LRTB.tType);
		String wtt = oppositeTabType(FBTB.tType);
		faceMap.put("Top", new Face("Top", depth, htt, LRTB.tCt, LRTB.tLen, width, wtt, FBTB.tCt, FBTB.tLen, wall, toolrad, relief));
		faceMap.put("Bottom", new Face("Bottom", depth, htt, LRTB.tCt, LRTB.tLen, width, wtt, FBTB.tCt, FBTB.tLen, wall, toolrad, relief));
		
		canvas = new Canvas(c, SWT.NONE);
		canvas.setSize(buildArea.x.intValue(), buildArea.y.intValue());
		canvas.setBackground(black);
	    canvas.addPaintListener(new PaintListener() {
	        public void paintControl(PaintEvent e) {
    			drawGrid(e.gc);
    			drawGraph(e.gc);
	        }
	    });
	    
	    canvas.addMouseWheelListener(new MouseWheelListener() {
	    	public void mouseScrolled(MouseEvent e) {
	    		if (e.count > 0)
	    			zoomOut();
	    		else
	    			zoomIn();
	    		
	    		canvas.redraw();
	    	}
	    });

	    canvas.addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseDoubleClick(MouseEvent e) {
	            resetView();;
	        }

	        @Override
	        public void mouseDown(MouseEvent e) {
	            if (e.button == 1) {
	        		start.x = (double) e.x;
	        		start.y = (double)e.y; 
	        		startOffset.x = offset.x;
	        		startOffset.y = offset.y;
	            	lbuttondown = true;
	            }
	            else if (e.button == 3) {
	            	rbuttondown = true;
	            }
	        }

	        @Override
	        public void mouseUp(MouseEvent e) {
	            if (e.button == 1) {
	            	lbuttondown = false;
	            }
	            else if (e.button == 3) {
	            	rbuttondown = false;
	            }
	        }
	    });
	    
	    canvas.addMouseMoveListener(new MouseMoveListener() {
	    	public void mouseMove(MouseEvent e) {
	    		if (lbuttondown) {
	    			double x = (double) e.x;
	    			double y = (double) e.y;
					double dx = x - start.x;
					double dy = y - start.y;
					offset.x = startOffset.x - dx/zoom;
					offset.y = startOffset.y - dy/zoom;
	    			canvas.redraw();
	    		}
	    	}
	    });
	    
		RowData rdCanvas = new RowData();
		rdCanvas.width = 500;
		rdCanvas.height = 500;
		canvas.setLayoutData(rdCanvas);
		
		rendered = true;
		return canvas;

	}
	
	private void drawGrid(GC gc) {
		if (!fDrawGrid)
			return;
		
		gc.setForeground(dimGray);
		for (int i = -buildArea.y.intValue(); i<=buildArea.y; i=i+10) {
			boolean skip = false;
			if (i%50 == 0) {
				if (i == 0) {
					gc.setForeground(green);
				}
				else {
					gc.setForeground(brtGray);
				}
			}
			else {
				gc.setForeground(dimGray);
				if (zoom < 0.75)
					skip = true;
			}
			
			if (!skip) {
				Point2D p1 = transform(new Point2D(-buildArea.x, (double) i));
				Point2D p2 = transform(new Point2D(buildArea.x, (double) i));
				gc.drawLine(p1.x.intValue(), p1.y.intValue(), p2.x.intValue(), p2.y.intValue());
			}
		}
		
		
		for (int i = -buildArea.x.intValue(); i<=buildArea.x; i=i+10) {
			boolean skip = false;
			if (i%50 == 0) {
				if (i == 0) {
					gc.setForeground(blue);
				}
				else {
					gc.setForeground(brtGray);
				}
			}
			else {
				gc.setForeground(dimGray);
				if (zoom < 0.75)
					skip = true;
			}
			
			if (!skip) {
				Point2D p1 = transform(new Point2D((double) i, -buildArea.y));
				Point2D p2 = transform(new Point2D((double) i, buildArea.y));
				gc.drawLine(p1.x.intValue(), p1.y.intValue(), p2.x.intValue(), p2.y.intValue());
			}
		}
	}
	
	private void drawGraph(GC gc) {
		gc.setForeground(red);

		Face f = faceMap.get(currentFace);
		List<Point2D> pts = f.renderPerimeter();
		
		int[] ipts = new int[pts.size()*2];
		int ix = 0;
		
		for (Point2D p : pts) {
			Point2D pt = transform(p);
			ipts[ix] = pt.x.intValue();
			ipts[ix+1] = pt.y.intValue();
			ix = ix + 2;
		}

		gc.drawPolyline(ipts);
	}

}
