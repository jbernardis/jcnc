package main.java.jcnc.viewer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.glu.GLU;

import main.java.jcnc.viewer.CNC;
import main.java.jcnc.viewer.CNCPoint;


public class GViewer extends Dialog {
	Shell shell;

    int canvasHeight = 600;
    int canvasWidth = 1200;

	float anglex = 0.0f;
	float angley = 0.0f;
	float anglez = 0.0f;
	float transx = 0.0f;
	float transy = 0.0f;
	float transz = 0.0f;
	float zoom = 1.0f;
	float lastx = 0.0f;
	float x = 0.0f;
	float lasty = 0.0f;
	float y = 0.0f;
	Double minx = 0.0;
	Double miny = 0.0;
	Double maxx = 0.0;
	Double maxy = 0.0;
	Double midx = 0.0;
	Double midy = 0.0;
	Double xspan = 200.0;
	Double yspan = 200.0;
	boolean lbuttondown = false;
	boolean rbuttondown = false;
	boolean resetView = false;
	boolean drawgrid = true;
	boolean drawzgrid = true;
	
	List<CNCVertex> gridVertices = new ArrayList<CNCVertex>();
	List<CNCColor> gridColors = new ArrayList<CNCColor>();
	List<CNCPoint> dataPoints;
	CNCColor cRapid, cNormal;
	final char ltRapid = 'r';
	final char ltNormal = 'n';
	GLU glu;
	
	CNC cnc;
	String title;
	
	public GViewer(Shell parent, String title) {
		super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		this.title = title;
		cnc = new CNC();
		gridVertices.clear();
		gridColors.clear();
		cRapid = new CNCColor(0.0f, 1.0f, 1.0f, 1.0f);
		cNormal = new CNCColor(1.0f, 1.0f, 1.0f, 1.0f);

	}
	
	public void open() {
		// Create the dialog window
		shell = new Shell(getParent(), getStyle());
		shell.setText(title);
		shell.setSize(640, 480);
		createContents();
		shell.layout(true, true);
		final Point newSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		shell.setSize(newSize);
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private void createContents() {
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		//gl.verticalSpacing = 10;
		shell.setLayout(gl);

	    GLData gldata = new GLData();
	    gldata.doubleBuffer = true;
	    // need SWT.NO_BACKGROUND to prevent SWT from clearing the window
	    // at the wrong times (we use glClear for this instead)
		final GLCanvas glcanvas = new GLCanvas(shell, SWT.NO_BACKGROUND, gldata );
	    GridData gd = new GridData();
	    gd.heightHint = canvasHeight;
	    gd.widthHint = canvasWidth;
	    glcanvas.setLayoutData(gd);
	    
	    glcanvas.setCurrent();
	    GLProfile glprofile = GLProfile.getDefault();
	    final GLContext glcontext = GLDrawableFactory.getFactory( glprofile ).createExternalGLContext();

	    glcanvas.addListener( SWT.Resize, new Listener() {
	        public void handleEvent(Event event) {
	            Rectangle rectangle = glcanvas.getClientArea();
	            glcanvas.setCurrent();
	            glcontext.makeCurrent();
	            ncSetup( glcontext.getGL().getGL2(), rectangle.width, rectangle.height );
	            glcontext.release();        
	        }
	    });

	    glcanvas.addPaintListener( new PaintListener() {
	        public void paintControl( PaintEvent paintevent ) {
	            glcanvas.setCurrent();
	            glcontext.makeCurrent();
	            ncRender(glcontext.getGL().getGL2(), canvasWidth, canvasHeight);
	            glcanvas.swapBuffers();
	            glcontext.release();        
	        }
	    });
	    
	    glcanvas.addMouseWheelListener(new MouseWheelListener() {
	    	public void mouseScrolled(MouseEvent e) {
	    		if (rbuttondown) {	 
		    		if (e.count < 0) 
		    			transz = 2;
		    		else
		    			transz = -2;
		    	}
	    		else {
		    		if (e.count < 0)
		    			zoom = zoom * 0.9f;
		    		else
		    			zoom = zoom * 1.1f;
	    		}
	    		
	    		glcanvas.redraw();
	    	}
	    });
	    
	    glcanvas.addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseDoubleClick(MouseEvent e) {
	            resetView = true;
	            glcanvas.redraw();;
	        }

	        @Override
	        public void mouseDown(MouseEvent e) {
	            x = lastx = e.x;
	            y = lasty = e.y;
	            if (e.button == 1) {
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
	    
	    glcanvas.addMouseMoveListener(new MouseMoveListener() {
	    	public void mouseMove(MouseEvent e) {
	    		if (lbuttondown) {
	    			lastx = x;
	    			lasty = y;
	    			x = e.x;
	    			y = e.y;
	    			anglex = x - lastx;
	    			angley = y - lasty;
	    			transx = 0;
	    			transy = 0;
	    			glcanvas.redraw();
	    		}
	    		else if (rbuttondown) {
	    			lastx = x;
	    			lasty = y;
	    			x = e.x;
	    			y = e.y;
	    			anglex = 0;
	    			angley = 0;
	    			transx = (x-lastx)*zoom/3.0f;
	    			transy = -(y-lasty)*zoom/3.0f;
	    			glcanvas.redraw();
	    		}
	    	}
	    });
	    
		
		Composite c = new Composite(shell, SWT.NONE);
		RowLayout rl = new RowLayout();
		rl.spacing = 20;
		rl.marginWidth = 20;
		rl.marginBottom = 10;
		rl.center = true;
		c.setLayout(rl);
		
		Button bDrawGrid = new Button(c, SWT.CHECK);
		Button bDrawZGrid = new Button(c, SWT.CHECK);

		bDrawGrid.setText("Draw grid");
		bDrawGrid.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				drawgrid = bDrawGrid.getSelection();
				setGridArrays();
				glcanvas.redraw();
				bDrawZGrid.setEnabled(drawgrid);;
			}
		});
		bDrawGrid.setSelection(drawgrid);

		bDrawZGrid.setText("Draw Z grid");
		bDrawZGrid.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				drawzgrid = bDrawZGrid.getSelection();
				setGridArrays();
				glcanvas.redraw();
			}
		});
		bDrawZGrid.setSelection(drawzgrid);
		bDrawZGrid.setEnabled(drawgrid);;

		GridData cgd = new GridData();
		cgd.horizontalAlignment = SWT.CENTER;
		cgd.grabExcessHorizontalSpace = true;
		c.setLayoutData(cgd);
	    
		setGridArrays();
	}
	
	private void setGridArrays() {
		float v, vl;
		gridVertices.clear();
		gridColors.clear();

		if (drawgrid) {		
			gridVertices.add(new CNCVertex(-500f, 0f, 0f, 500f, 0f, 0f));
			gridColors.add(new CNCColor(1.0f, 0.0f, 0.0f, 1f));
			v = -500;
			while (v <= 500) {
				if (v % 50 == 0)
					vl = 4;
				else
					vl = 2;
				
				if (v != 0) {
					gridVertices.add(new CNCVertex(v, -vl, 0, v, vl, 0));
					gridColors.add(new CNCColor(1.0f, 0.0f, 0.0f, 1f));
				}
				v = v + 10;
			}
	
			gridVertices.add(new CNCVertex(0f, -500f, 0f, 0f, 500f, 0f));
			gridColors.add(new CNCColor(0.0f, 1.0f, 0.0f, 1f));
			v = -500;
			while (v <= 500) {
				if (v % 50 == 0)
					vl = 4;
				else
					vl = 2;
				if (v != 0) {
					gridVertices.add(new CNCVertex(-vl, v, 0, vl, v, 0));
					gridColors.add(new CNCColor(0.0f, 1.0f, 0.0f, 1));
				}
				v = v + 10;
			}
		}

		if (drawgrid && drawzgrid) {			
			gridVertices.add(new CNCVertex(0, 0, -500, 0, 0, 500));
			gridColors.add(new CNCColor(0.0f, 0.0f, 1.0f, 1));
			v = -500;
			while (v <= 500) {
				if (v % 50 == 0)
					vl = 4;
				else
					vl = 2;
				if (v != 0) {
					gridVertices.add(new CNCVertex(0, -vl, v, 0, vl, v));
					gridColors.add(new CNCColor(0.0f, 0.0f, 1.0f, 1));
				}
				v = v + 10;
			}
		}
	}
	
	private void ncSetup(GL2 gl2, int width, int height) {
        gl2.glMatrixMode( GL2.GL_PROJECTION );
        gl2.glLoadIdentity();
        glu = new GLU();
		
		gl2.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
		gl2.glColor3f(1F, 0F, 0F);
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		gl2.glEnable(GL2.GL_CULL_FACE);

		gl2.glEnable(GL2.GL_LIGHTING);
		gl2.glEnable(GL2.GL_LIGHT0);
		gl2.glEnable(GL2.GL_LIGHT1);

		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[] {0.5f,0.5f,0.5f,1.0f}, 0);
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, new float[] {0.5f,0.5f,0.5f,1.0f}, 0);
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, new float[] {1.0f,1.0f,1.0f,1.0f}, 0);
		gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, new float[] {1.0f,0.5f,0.5f,0.0f}, 0);
		gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, new float[] {0.5f,0.5f,0.5f,1.0f}, 0);
		gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, new float[] {1.0f,1.0f,1.0f,1.0f}, 0);

		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {0.5f,0.0f,0.3f,1.0f}, 0);
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[] {1.0f,1.0f,1.0f,1.0f}, 0);
		gl2.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 80);
		gl2.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, new float[] {0.1f, 0.1f, 0.1f, 0.9f}, 0);
		
		gl2.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[] {0.0f, 200.0f, 100.0f, 1.0f}, 0);
		gl2.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, new float[] {0.0f, -200.0f, 100.0f, 1.0f}, 0);
	}
	
	private void calculatePositions(Double[] v) {
		minx = v[0];
		miny = v[1];
		maxx = v[2];
		maxy = v[3];
	
		xspan = maxx - minx;
		yspan = maxy - miny;
		
		midx = minx + xspan/2.0;
		midy = miny + (maxy - miny)/2.0;
	}
	
    private void ncRender( GL2 gl2, int width, int height ) { 
		if (resetView) {
			zoom = 1.0f;
		}
		gl2.glViewport(0, 0, width, height);
        
		gl2.glMatrixMode(GL2.GL_PROJECTION);
		gl2.glLoadIdentity();
		
		float aspectRatio = (float) width/(float) height;
		
		Double eyeZ = xspan/2.0;
		Double frustrumNear = 50.0;
		if (eyeZ < 2*frustrumNear)
			eyeZ = 2*frustrumNear;
		
		glu.gluPerspective(60*zoom, aspectRatio, frustrumNear, 1000.0);
		glu.gluLookAt (midx, miny-eyeZ, eyeZ, midx, midy, 0.0, 0.0, 0.0, 1.0);

		gl2.glMatrixMode(GL2.GL_MODELVIEW);
		if (resetView) {
			gl2.glLoadIdentity();
			lastx = x = 0.0f;
			lasty = y = 0.0f;
			anglex = angley = anglez = 0.0f;
			transx = transy = 0.0f;
			resetView = false;
		}
			
		float w = Math.max(width, 1.0f);
		float h = Math.max(height, 1.0f);
		float xScale = 180.0f / w;
		float yScale = 180.0f / h;
		gl2.glRotatef(angley * yScale, 1.0f, 0.0f, 0.0f);
		gl2.glRotatef(anglex * xScale, 0.0f, 1.0f, 0.0f);
		gl2.glRotatef(anglez, 0.0f, 0.0f, 1.0f);
		gl2.glTranslatef(transx, transy, transz);

		gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl2.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
		gl2.glEnable(GL2.GL_COLOR_MATERIAL);

		gl2.glEnable(GL2.GL_LIGHTING);
		gl2.glEnable(GL2.GL_LIGHT0);
		gl2.glEnable(GL2.GL_LIGHT1);
 
		if (drawgrid) {
			gl2.glBegin(GL2.GL_LINES);
			
			for (int i=0; i<gridVertices.size(); i++) {
				CNCColor c = gridColors.get(i);
				gl2.glColor4f(c.r,  c.g,  c.b,  c.a);
				CNCVertex v = gridVertices.get(i);
				gl2.glVertex3f(v.x1, v.y1, v.z1);
				gl2.glVertex3f(v.x2, v.y2, v.z2);
			}

			gl2.glEnd();
		}

		if (dataPoints.size() > 0) {
			char currentLineType = ltRapid;
			gl2.glColor4f(cRapid.r, cRapid.g, cRapid.b, cRapid.a);
			
			gl2.glBegin(GL2.GL_LINE_STRIP);
			
			for (CNCPoint p : dataPoints) {
				if (p.getType() != currentLineType) {
						currentLineType = p.getType();
						if (currentLineType == ltRapid) 
							gl2.glColor4f(cRapid.r, cRapid.g, cRapid.b, cRapid.a);
						else
							gl2.glColor4f(cNormal.r, cNormal.g, cNormal.b, cNormal.a);
				}
				gl2.glVertex3d(p.getX(), p.getY(), p.getZ());
			}
				
			gl2.glEnd();
		}

    	
    	gl2.glDisable(GL2.GL_LIGHT0);
		gl2.glDisable(GL2.GL_LIGHT1);
		gl2.glDisable(GL2.GL_LIGHTING);
			
		anglex = angley = anglez = 0.0f;
		transx = transy = transz = 0.0f;
    }
	
	public void addGCode(String[] gcl) {
		for (String gl: gcl) {
			cnc.execute(gl);
		}	
		
		dataPoints = cnc.getPoints(); 
		calculatePositions(cnc.getMinMax());
		
        open();
	}
}
