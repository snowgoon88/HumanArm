/**
 * 
 */
package viewer;

import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import javax.vecmath.Point3d;

import Jama.Matrix;

import model.Arm;
import model.CompleteArm;
import model.NeuroControl;

/**
 * Observe the CompleteArm as a one ligne String.
 * 
 * @author alain.dutech@loria.fr
 *
 */
public class SCompleteArm implements Observer {

	DecimalFormat df3_5 = new DecimalFormat( "000.00000" );
	
	/**
	 * Representation of Arm as a oneLineString.
	 */
	public String viewStr = "";
	/**
	 * Explanation of the data read.
	 */
	public String explainStr = "";
	
	/**
	 * Creation with a CompleteArm model.
	 * @param model
	 */
	public SCompleteArm(CompleteArm model) {
		int nbMuscle = model.getArrayNeuroControlers().length;
		viewStr = "";
		explainStr = "";
		explainStr += String.format("%9s", "ArmEndX")+"\t";
		explainStr += String.format("%9s", "ArmEndY")+"\t";
		// consigne
		for (int i = 0; i < nbMuscle; i++) {
			explainStr += String.format("%9s", "cons"+i)+"\t";
		}
		// activation
		for (int i = 0; i < nbMuscle; i++) {
			explainStr += String.format("%9s", "act"+i)+"\t";
		}
		// torque
		for (int i = 0; i < model.getArm().getTension().getColumnDimension(); i++) {
			explainStr += String.format("%9s", "torq"+i)+"\t";
		}
		// Arm State
		for (int i = 0; i < model.getArm().getArmPos().getColumnDimension(); i++) {
			explainStr += String.format("%9s", "pos"+i)+"\t";
		}
		for (int i = 0; i < model.getArm().getArmSpeed().getColumnDimension(); i++) {
			explainStr += String.format("%9s", "spd"+i)+"\t";
		}
		// points of arm
		for (int i = 0; i < model.getArm().getArmPoints().size(); i++) {
			explainStr += String.format("%9s", "x"+i)+"\t";
			explainStr += String.format("%9s", "y"+i)+"\t";
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (o != null && o instanceof CompleteArm) {
			CompleteArm cArm = (CompleteArm) o;
			viewStr = "";
			
			Arm arm = cArm.getArm();
			NeuroControl[] nc = cArm.getArrayNeuroControlers();
			// end point
			viewStr += df3_5.format(arm.getArmEndPointX())+"\t";
			viewStr += df3_5.format(arm.getArmEndPointY())+"\t";
			// consigne
			for (int i = 0; i < nc.length; i++) {
				viewStr += df3_5.format(nc[i].getU())+"\t";
			}
			// act
			for (int i = 0; i < nc.length; i++) {
				viewStr += df3_5.format(nc[i].getAct())+"\t";
			}
			// torque
			Matrix tau = cArm.getArm().getTension();
			for (int i = 0; i < tau.getColumnDimension(); i++) {
				viewStr += df3_5.format(tau.get(0, i))+"\t";
			}
			// Arm State
			Matrix pos = cArm.getArm().getArmPos();
			for (int i = 0; i < pos.getColumnDimension(); i++) {
				viewStr += df3_5.format(pos.get(0, i))+"\t";
			}
			Matrix spd = cArm.getArm().getArmSpeed();
			for (int i = 0; i < spd.getColumnDimension(); i++) {
				viewStr += df3_5.format(spd.get(0, i))+"\t";
			}
			// points of arm
			for (Point3d pt : cArm.getArm().getArmPoints()) {
				viewStr += df3_5.format(pt.x)+"\t"+df3_5.format(pt.y)+"\t";
			}
		}
	}

}
