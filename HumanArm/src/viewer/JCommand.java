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
@SuppressWarnings("serial")
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
	/** Label for Y */
	JLabel _yLabel;
	/** Spinner for Y */
	JSpinner _ySpin;
	/** Spinner Model for X */
	SpinnerNumberModel _ySpinModel;
	
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
		((JSpinner.DefaultEditor)_xSpin.getEditor()).getTextField().setColumns(7);
		_xSpin.addChangeListener(
				new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						if (_com != null ) {
							_comSeq.changeCommand(_com, _xSpinModel.getNumber().doubleValue(), _com.val);
							System.out.println("xSpin changed");
						}
					}
					
				});
		add( _xSpin );
		
		_yLabel = new JLabel("Y:");
		add( _yLabel );
		
		_ySpinModel = new SpinnerNumberModel(0.0, 0.0, 10.0, 0.1);
		if (_com != null) {
			_ySpinModel.setValue(_com.val);
		}
		_ySpin = new JSpinner(_ySpinModel);
		((JSpinner.DefaultEditor)_ySpin.getEditor()).getTextField().setColumns(7);
		_ySpin.addChangeListener(
				new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						if (_com != null ) {
							_comSeq.changeCommand(_com, _com.time, _ySpinModel.getNumber().doubleValue());
							System.out.println("ySpin changed");
						}
					}
					
				});
		add( _ySpin );
	}
	
	public void setCommand( Command com, CommandSequence comSeq) {
		_com = com;
		_comSeq = comSeq;
		_xSpinModel = new SpinnerNumberModel(0.0, 0.0, 10.0, 0.1);
		if (_com != null) {
			_xSpinModel.setValue(_com.time);
		}
		_xSpin.setModel(_xSpinModel);
		((JSpinner.DefaultEditor)_xSpin.getEditor()).getTextField().setColumns(7);
		
		_ySpinModel = new SpinnerNumberModel(0.0, 0.0, 10.0, 0.1);
		if (_com != null) {
			_ySpinModel.setValue(_com.val);
		}
		_ySpin.setModel(_ySpinModel);
		((JSpinner.DefaultEditor)_ySpin.getEditor()).getTextField().setColumns(7);
	}
	public void update() {
		if (_com != null) {
			_xSpinModel.setValue(_com.time);
			_ySpinModel.setValue(_com.val);
		}	
	}
	
//	public Command getCommand() {
//		return _com;
//	}
}
