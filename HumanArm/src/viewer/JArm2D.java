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
import java.util.Observable;
import java.util.Observer;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.Arm;

/**
 * Dessine le bras comme une suite de segments bleus.
 * Les axes sont aussi dessiné entre -1 et 1.
 * 
 * TODO Garder posX et posY ou passer aux Point3d ?
 * TODO Tenter MVC ?
 * 
 * @author alain.dutech@loria.fr
 *
 */
@SuppressWarnings("serial")
public class JArm2D extends JPanel implements Observer {
	
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
	public void update(Observable model, Object o) {
		this.repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
        super.paintComponent(g);       
        
        _size = this.getSize();
        
        // Essayer de tracer une croix -1,1; -1,1
        g.drawLine(xWin(-1.0),yWin(0.0), xWin(1.0), yWin(0.0) );
        g.drawLine(xWin(0.0),yWin(-1.0), xWin(0.0), yWin(1.0) );
        // Bras
        drawArm(g);
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
	
	public JPanel getControlPanel() {
		return new JArmControl();
	}
	/**
	 * JPanel in order to control the JArm panel.
	 */
	class JArmControl extends JPanel {
		public JArmControl() {
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
