/**
 * 
 */
package example;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import Jama.Matrix;

import model.CompleteArm;

import viewer.JArm2D;
import viewer.JArmLabel;

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
	JArmLabel _jInfo;
	
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
		_frame.setLayout(new BorderLayout());
		
		_jArm = new JArm2D(_arm.getArm());
		System.out.println("MemSize="+_jArm.getMemorySize());
		_frame.add(_jArm, BorderLayout.CENTER);
		_frame.add(_jArm.getControlPanel(), BorderLayout.SOUTH);
		_jInfo = new JArmLabel(_arm.getArm());
		_frame.add(_jInfo, BorderLayout.NORTH);
		
		_arm.getArm().addModelListener(_jArm);
		_arm.getArm().addModelListener(_jInfo);
		
		_frame.setVisible(true);
	}
	
	public void start() {
		_arm.getArm().setBounded(false);
		final Matrix inc = new Matrix( new double[][]{{Math.PI/10,Math.PI/15}});
		
		while (true) {
			Matrix ang = _arm.getArm().getArmPos();
			ang.plusEquals(inc);
			_arm.setup(ang.get(0,0), ang.get(0, 1));
//			_jArm.repaint();
//			_jInfo.modelChanged();
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
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
