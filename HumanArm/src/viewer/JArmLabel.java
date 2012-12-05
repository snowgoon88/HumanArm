/**
 * 
 */
package viewer;

import javax.swing.JLabel;
import utils.JamaU;
import model.Arm;
import model.ArmModelListener;

/**
 * Un JLabel qui affiche la position et la vitesse du bras.
 * 
 * @author alain.dutech@loria.fr
 *
 */
@SuppressWarnings("serial")
public class JArmLabel extends JLabel implements ArmModelListener {

	/** The model to display */
	Arm _arm;
	
	/**
	 * Construction avec un model (Arm).
	 */
	public JArmLabel(Arm model) {
		super();
		_arm = model;
		setText(getDisplayString());
	}

	/** 
	 * Pos et Speed.
	 */
	private String getDisplayString() {
		String str = "";
		// state
		str += "Pos="+JamaU.vecToString(_arm.getArmPos());
		str +="  Spd="+JamaU.vecToString(_arm.getArmSpeed());
		return str;
	}

	@Override
	public void modelChanged(Arm model) {
		setText(getDisplayString());
	}
}
