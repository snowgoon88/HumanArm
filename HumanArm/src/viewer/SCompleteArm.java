/**
 * 
 */
package viewer;

import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import model.Arm;
import model.CompleteArm;

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
	 * Creation. Init viewStr and explainStr.
	 */
	public SCompleteArm() {
		viewStr = "";
		explainStr = "ArmEndX\tArmEndY";
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
			
			viewStr += df3_5.format(arm.getArmEndPointX())+"\t";
			viewStr += df3_5.format(arm.getArmEndPointY());
		}
	}

}
