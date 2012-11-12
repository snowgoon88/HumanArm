/**
 * 
 */
package viewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import model.Arm;

/**
 * @author alain.dutech@loria.fr
 *
 */
@SuppressWarnings("serial")
public class JArm extends JPanel {
	
	/** Bounds of the model_canvas */
	double _minX = -1.0, _maxX = 1.0;
	/** Bounds of the model_canvas */
	double _minY = -1.0, _maxY = 1.0;
	/** Bounds of the window_canvas */
	Dimension _size;
	
	/** Model : HumanArm */
	Arm _arm;
	
	public JArm( Arm model ) {
		super();
		_arm = model;
	}
	public JArm( Arm model, double minX, double maxX, double minY, double maxY ) {
		super();
		_arm = model;
		_minX = minX;
		_maxX = maxX;
		_minY = minY;
		_maxY = maxY;
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
	}
	
	/**
	 * Compute the x_window_point from the x_model_point
	 * @return window x 
	 */
	private int xWin( double x) {
		return (int) ((x - _minX)/(_maxX-_minX) * _size.width);
	}
	/**
	 * Compute the y_window_point from the y_model_point
	 * @return window y 
	 */
	private int yWin( double y) {
		return (int) (_size.height - (y - _minY)/(_maxY-_minY) * _size.height);
	}
}
