/**
 * 
 */
package example;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

import Jama.Matrix;

import model.CompleteArm;

import utils.JamaU;
import viewer.JArm2D;

/**
 * @author alain.dutech@loria.fr
 *
 */
public class ArmGraphic {

	/** Fenetre principale de l'application */
	JFrame _frame;
	/** Le bras complet */
	CompleteArm _arm = new CompleteArm();
	/** Pour l'afficher */
	JArm2D _jArm;
	
	public ArmGraphic() {
		// Setup in resting position
		_arm.setup(0.0, Math.toRadians(45));
		
		// Setup window
		_frame = new JFrame("Arm - Java2D API");
		_frame.setSize(600,600);
		_frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		
		_jArm = new JArm2D(_arm.getArm());
		_frame.add(_jArm);
		
		_frame.setVisible(true);
	}
	
	public void start() {
		_arm.getArm().setBounded(false);
		final Matrix inc = new Matrix( new double[][]{{Math.PI/10,Math.PI/15}});
		
		while (true) {
			Matrix ang = _arm.getArm().getArmPos();
			ang.plusEquals(inc);
			_arm.setup(ang.get(0,0), ang.get(0, 1));
			_jArm.repaint();
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArmGraphic app = new ArmGraphic();
		app.start();
	}

}
