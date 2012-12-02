/**
 * 
 */
package viewer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.Command;
import model.CommandSequence;

/**
 * @author alain.dutech@loria.fr
 *
 */
public class JCommand extends JPanel {

	/** Linked to a Command */
	Command _com = null;
	/** That belongs to a CommandSequence */
	CommandSequence _comSeq = null;
	
	/** Label for X */
	JLabel _xLabel;
	/** Spinner for X */
	JSpinner _xSpin;
	/** Spinner Model for X */
	SpinnerNumberModel _xSpinModel;
	
	public JCommand( Command com, CommandSequence comSequence ) {
		super();
		
		_com = com;
		_comSeq = comSequence;
		
		buildGUI();
	}
	
	private void buildGUI() {
		_xLabel = new JLabel("X:");
		add( _xLabel );
		
		_xSpinModel = new SpinnerNumberModel(0.0, 0.0, 10.0, 0.1);
		if (_com != null) {
			_xSpinModel.setValue(_com.time);
		}
		_xSpin = new JSpinner(_xSpinModel);
		_xSpin.addChangeListener(
				new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						_comSeq.changeCommand(_com, _xSpinModel.getNumber().doubleValue(), _com.val);
						System.out.println("xSpin changed");
					}
					
				});
		add( _xSpin );
	}
	
	public void setCommand( Command com ) {
		_com = com;
		_xSpinModel = new SpinnerNumberModel(0.0, 0.0, 10.0, 0.1);
		if (_com != null) {
			_xSpinModel.setValue(_com.time);
		}
		_xSpin.setModel(_xSpinModel);
	}
}
