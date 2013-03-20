/**
 * 
 */
package viewer;

import info.monitorenter.util.collections.RingBufferArrayFast;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Jama.Matrix;

import model.Arm;
import model.ArmModelListener;

/**
 * Dessine le bras comme une suite de segments bleus.
 * Les axes sont aussi dessiné entre -1 et 1.
 * 
 * TODO Garder posX et posY ou passer aux Point3d ?
 * 
 * @author alain.dutech@loria.fr
 *
 */
@SuppressWarnings("serial")
public class JArm2D extends JPanel implements ArmModelListener {
	
	/** Bounds of the model_canvas */
	double _minX = -1.0, _maxX = 1.0;
	/** Bounds of the model_canvas */
	double _minY = -1.0, _maxY = 1.0;
	/** Bounds of the window_canvas */
	Dimension _size;
	
	/** Model : HumanArm */
	Arm _arm;
	/** Memory of end points */
	RingBufferArrayFast<Double> _endX, _endY;
	/** Display Memory */
	boolean _fg_memory = true;
	
	/** Position of the goal */
	double _goalX = 0.0, _goalY = 0.0;
	/** Radius of Goal for displaying */
	double _goalRadius = 0.01;
	/** Display goals */
	boolean _fg_goal = true;
	
	/** Display the reaching area */
	boolean _fg_reaching = false;

	public JArm2D( Arm model ) {
		super();
		_arm = model;
		_endX = new RingBufferArrayFast<Double>(100);
		_endY = new RingBufferArrayFast<Double>(100);
	}
	public JArm2D( Arm model, double minX, double maxX, double minY, double maxY ) {
		super();
		_arm = model;
		_endX = new RingBufferArrayFast<Double>(100);
		_endY = new RingBufferArrayFast<Double>(100);
		
		_minX = minX;
		_maxX = maxX;
		_minY = minY;
		_maxY = maxY;
	}
	@Override
	public void update(Arm model, Object o) {
		if (o != null && o instanceof Matrix) {
			Matrix goal = (Matrix)o;
			_goalX = goal.get(0, 0);
			_goalY = goal.get(0, 1);
			System.out.println("Goal : " + _goalX + " " + _goalY);
		}
		this.repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
        super.paintComponent(g);       
        
        _size = this.getSize();
        
        // Essayer de tracer une croix -1,1; -1,1
        g.drawLine(xWin(-1.0),yWin(0.0), xWin(1.0), yWin(0.0) );
        g.drawLine(xWin(0.0),yWin(-1.0), xWin(0.0), yWin(1.0) );

        // Zone accessible
        if (_fg_reaching) drawReachingArea(g);

        // Bras
        drawArm(g);
        
        // But
        if (_fg_goal) drawGoal(g);
    }

	/** Draw the reaching area.
	 * 
	 * TODO: avoid magic number compute from the arm characteristics.
	 */
	private void drawReachingArea(Graphics g) {
		g.setColor(Color.LIGHT_GRAY);

		final double l0 = _arm.getLength()[0];
		final double l1 = _arm.getLength()[1];

		double centre_x = 0;
		double centre_y = 0;
		double angle = 0;

		this.drawArc(g, 0, 0, (l0 + l1), -30, 170);

		angle = _arm.getConstraints()._minq[0];
		centre_x = l0 * Math.cos(angle);
		centre_y = l0 * Math.sin(angle);
		this.drawArc(g, centre_x, centre_y, l1, -30, 160);

		angle = _arm.getConstraints()._maxq[0];
		centre_x = l0 * Math.cos(angle);
		centre_y = l0 * Math.sin(angle);
		this.drawArc(g, centre_x, centre_y, l1, 140, 160);

		// Thx Al-Kashi
		// double c_2 = l0*l0 + l1*l1 -
		// 2*l0*l1*Math.cos(Math.toRadians(180-160));
		// radius = Math.sqrt(c_2);
		// startAngle =
		// (Math.toDegrees(Math.acos((l1*l1-c_2-l0*l0)/-2*radius*l1))-20);
		// arcAngle = 140 + 30;
		this.drawArc(g, 0, 0, 0.123, 70, 170);
	}

	/** Draw an arc the the center of the circle q*/
	private void drawArc(Graphics g, double centre_x, double centre_y,
			double radius, int startAngle, int arcAngle) {
		int x = xWin(centre_x - radius);
		int y = yWin(centre_y + radius);
		int width = xWin(centre_x + radius) - x;
		int height = yWin(centre_y - radius) - y;
		g.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	/** Draw a circle centered on the goal */
	private void drawGoal(Graphics g) {
		if (_arm.isPointReachable(_goalX, _goalY))
			g.setColor(Color.GREEN);
		else
			g.setColor(Color.RED);
		drawArc(g, _goalX, _goalY, _goalRadius, 0, 360);
	}
	
	/** Draw Arm as a sequence of lines */
	private void drawArm(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		double [] posX = _arm.getArmX();
		double [] posY = _arm.getArmY();
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(3));
		g2.setColor(Color.blue);
		
		for (int i = 1; i < posX.length; i++) {
			g.drawLine(xWin(posX[i-1]),yWin(posY[i-1]), xWin(posX[i]), yWin(posY[i]) );
		}
		
		// Add end point to memory
		addEndPoint(posX[posX.length-1], posY[posY.length-1]);
		
		// draw trajectory
		if (_fg_memory) drawMemory(g2);
	}
	private void drawMemory(Graphics2D g2) {
		if (_endX.size() > 1 ) {
			g2.setStroke(new BasicStroke(1));
			g2.setColor(Color.magenta);
			double srcX = _endX.getOldest();
			double srcY = _endY.getOldest();
			Iterator<Double> iY = _endY.iterator();
			for (Iterator<Double> iX = _endX.iterator(); iX.hasNext();) {
				Double x = (double) iX.next();
				Double y = (double) iY.next();
				g2.drawLine(xWin(srcX),yWin(srcY), xWin(x),yWin(y));
				srcX = x;
				srcY = y;
			}	
		}
	}
	
	/**
	 * Compute the x_window_point from the x_model_point
	 * @return window x 
	 */
	private int xWin( double x) {
		double size = Math.min(_size.width, _size.height);
		return (int) ((x - _minX)/(_maxX-_minX) * size);
	}
	/**
	 * Compute the y_window_point from the y_model_point
	 * @return window y 
	 */
	private int yWin( double y) {
		double size = Math.min(_size.width, _size.height);
		return (int) (size - (y - _minY)/(_maxY-_minY) * size);
	}
	
	/**
	 * Get the size of the memory trajectory drawn
	 * @return _memSize
	 */
	public int getMemorySize() {
		return _endX.getBufferSize();
	}
	/**
	 * Set the size of the memory trajectory drawn
	 */
	public void setMemorySize(int size ) {
		_endX.setBufferSize(size);
		_endY.setBufferSize(size);
	}
	/** 
	 * Empty the memory trajectory
	 */
	public void resetMemory() {
		_endX.clear();
		_endY.clear();
	}
	
	/**
	 * Is the Memory drawn on JPanel
	 * @return the _fg_memory
	 */
	public boolean isMemoryDrawn() {
		return _fg_memory;
	}
	/**
	 * Decide if Memory is drawn on JPanel
	 */
	public void setMemoryDrawn(boolean drawMemory) {
		this._fg_memory = drawMemory;
	}
	private void addEndPoint( double x, double y) {
		_endX.add(x);
		_endY.add(y);
	}
	
	/**
	 * Set the goal position
	 * @param x
	 * @param y
	 */
	public void setGoal(double x, double y) {
		this._goalX = x;
		this._goalY = y;
	}
	
	/**
	 * Decide if Goal is drawn on JPanel
	 */
	public void setGoalDrawn(boolean drawGoal) {
		this._fg_goal = drawGoal;
	}
	
	public boolean isGoalDrawn() {
		return _fg_goal;
	}
	
	public boolean isReachingAreaDrawn() {
		return _fg_reaching;
	}
	public void setReachingAreaDrawn(boolean drawnReachingArea) {
		this._fg_reaching = drawnReachingArea;
	}

	public JPanel getControlPanel() {
		return new JArmControl();
	}
	/**
	 * JPanel in order to control the JArm panel.
	 */
	class JArmControl extends JPanel {
		public JArmControl() {
			// JCheckBox for drawing Reaching Area
			JCheckBox reachingCheck = new JCheckBox("Zone");
			reachingCheck.setSelected(isReachingAreaDrawn());
			reachingCheck.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					setReachingAreaDrawn(e.getStateChange() == ItemEvent.SELECTED);
				}
			});
			add(reachingCheck);

			// JCheckBox for drawing Goal
			JCheckBox goalCheck = new JCheckBox("But");
			goalCheck.setSelected(isGoalDrawn());
			goalCheck.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					setGoalDrawn(e.getStateChange() == ItemEvent.SELECTED);
				}
			});
			add(goalCheck);
			
			// JCheckBox for drawing Memory
			JCheckBox memCheck = new JCheckBox("Mémoire");
			memCheck.setSelected(isMemoryDrawn());
			memCheck.addItemListener(
					new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							if (e.getStateChange() == ItemEvent.SELECTED) {
								setMemoryDrawn(true);
							}
							else {
								setMemoryDrawn(false);
							}
						}
					});
			add(memCheck);
			
			// JSlider for Memory size
			JLabel memSizeLabel = new JLabel("Taille:");
			add(memSizeLabel);
			final SpinnerNumberModel memSizeModel = new SpinnerNumberModel(getMemorySize(), 2, 10000, 20);
			JSpinner memSpin = new JSpinner(memSizeModel);
			memSpin.addChangeListener(
					new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent e) {
							setMemorySize(memSizeModel.getNumber().intValue());
						}
					});
			add(memSpin);
		}
	}
}
