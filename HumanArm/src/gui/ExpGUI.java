/**
 * 
 */
package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import utils.GraphicHelper;
import viewer.JCommand;

import Jama.Matrix;

import model.Command;
import model.CommandSequence;
import model.CompleteArm;


/**
 * @author Alain.Dutech@loria.fr
 *
 */
public class ExpGUI {
	
	/** The complete arm simulated */
	CompleteArm _arm;
	/** Array of CommandSequence */
	CommandSequence[] _consigne = new CommandSequence[6];
	
	/** Simulation time */
	double _t = 0.0;
	/** Simulation _maxTime */
	double _maxTime = 20.0;
	/** Simulation DeltaT */
	double _dt = 0.025;
	
	/** Commands for Muscles */
	Matrix _u = new Matrix(1,6, 0.0);
	/** Simulation run */
	boolean _fg_run = false;
	
	/** Class for helping in designing GUI */
	GraphicHelper _gh = new GraphicHelper();
	/** Viewer for the Experience */
	JExperience _xpPanel;
	/** Kind of toolBar */
	JPanel _toolBar;
	/** Action to reset arm */
	Action _resetAct;
	/** Action to start simulation */
	Action _startAct;
	/** Action to stop simulation */
	Action _stopAct;
	/** Action to pause simulation */
	Action _pauseAct;
	/** Action to step simulation */
	Action _stepAct;

	/** Models for setting angle0 */
	SpinnerNumberModel _ang0SpinModel;
	/** Models for setting angle1 */
	SpinnerNumberModel _ang1SpinModel;
	/** Model for setting _maxTime */
	SpinnerNumberModel _maxTimeSpinModel;
	/** Model for setting _dt */
	SpinnerNumberModel _dtSpinModel;
	
	/**
	 * 
	 */
	public ExpGUI() {
		_arm = new CompleteArm();
		
		buildActions();
		buildGUI();
		
		setMaxTime( 15.0 );
		setDT( 0.025 );
	}
	private void buildActions() {
		_resetAct = new ResetAction("Reset", null,
				"Initialise le bras aux angles donnés, t=0",
				KeyEvent.VK_R);
		_startAct = new StartAction("Play", null, 
				"Lance la simulation", 
				KeyEvent.VK_L);
		_stopAct = new StopAction("Stop", null,
				"Stope la simulation",
				KeyEvent.VK_S);
		_pauseAct = new PauseAction("Pause", null,
				"Pause/reprend la simulation",
				KeyEvent.VK_P);
		_stepAct = new StepAction("Step", null, "Un pas de simulation", KeyEvent.VK_E);
	}
	private void buildGUI() {
		// Setup window
		JFrame frame = new JFrame("Human Arm");
		frame.setSize(1200,600);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.setLayout(new BorderLayout());

		_xpPanel = new JExperience(_arm );
		frame.add(_xpPanel, BorderLayout.CENTER);
		
		_toolBar = new JPanel();
		// Spinner for max Time
		_maxTimeSpinModel = _gh.addJSpinNumber(_toolBar, _maxTime, 0.0, 10000.0, 1.0, "maxTime", "000.0");
		_maxTimeSpinModel.addChangeListener(
				new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						_maxTime = _maxTimeSpinModel.getNumber().doubleValue();
					}

				});
		// Spinner for dt
		_dtSpinModel = _gh.addJSpinNumber(_toolBar, _dt, 0.0, 1.0, 0.005, "dt", "0.000");
		_dtSpinModel.addChangeListener(
				new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						_dt = _dtSpinModel.getNumber().doubleValue();
					}

				});
		
		// Spinners for angles
		_ang0SpinModel = _gh.addJSpinNumber(_toolBar, 0.0, 0.0, 360.0, 5.0, "ang0", "000.0");
		_ang1SpinModel =_gh.addJSpinNumber(_toolBar, 0.0, 0.0, 360.0, 5.0, "ang1", "000.0");
		
		// Button for control
		JButton resetBtn = new JButton(_resetAct);
		_toolBar.add(resetBtn);
		JButton startBtn = new JButton(_startAct);
		_toolBar.add(startBtn);
//		JButton stopBtn = new JButton(_stopAct);
//		_toolBar.add(stopBtn);
		JButton pauseBtn = new JButton(_pauseAct);
		_toolBar.add(pauseBtn);
		JButton stepBtn = new JButton(_stepAct);
		_toolBar.add(stepBtn);
		
		
		
		frame.add(_toolBar, BorderLayout.NORTH);
		
		frame.setVisible(true);
	}

	public void reset( double degAng0, double degAng1) {
		// Setup in resting position
		_arm.setup(Math.toRadians(degAng0), Math.toRadians(degAng1));
		
		// Un vecteur (Matrix 1x6) de consignes musculaires, initialisée à 0.0.
		for (int i = 0; i < 6; i++) {
			_u.set(0,i, 0.0);
		}
		
		_t = 0.0;
		
		_xpPanel.reset();
	}
	private void step( double dt ) {
		for (int i = 0; i < _consigne.length; i++) {
			CommandSequence cs = _consigne[i];
			// la valeur de la consigne est copiée dans le vecteur u
			_u.set(0,i, cs.getValAtTimeFocussed(_t));
		}
		// Applique les consignes sur le bras
		_arm.applyCommand(_u, dt);
		System.out.println("TIME = "+_t);
		System.out.println(_arm.toString());
		
		_t += dt;
		
		_xpPanel.update(_t);
	}
	public void run() throws InterruptedException {
		while(true) {
			if (_fg_run && _t < _maxTime ) {
				step( 0.025 );
			}
			Thread.sleep(10);
			_xpPanel.repaint();
		}
	}

	/**
	 * Read the 6 CommandSequence from a file.
	 * @param fileName Name of the file
	 * @throws IOException
	 */
	public void readCommandSequences(String fileName) throws IOException {
		FileReader myFile = new FileReader( fileName );
        BufferedReader myReader = new BufferedReader( myFile );
        
        // Need to read 6 CommandSequence
        for (int i = 0; i < _consigne.length; i++) {
			_consigne[i] = new CommandSequence();
			_consigne[i].read(myReader);
			_xpPanel.addConsigne(_consigne[i]);
		}
        
        myReader.close();
        myFile.close();
	}

	/**
	 * Reset Arm.
	 */
	class ResetAction extends AbstractAction {
		public ResetAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			reset( _ang0SpinModel.getNumber().doubleValue(),
					_ang1SpinModel.getNumber().doubleValue());
		}
	}
	/**
	 * Start Simulation.
	 */
	class StartAction extends AbstractAction {
		public StartAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			_fg_run = true;
		}
	}
	/**
	 * Stop Simulation.
	 */
	class StopAction extends AbstractAction {
		public StopAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			_fg_run = false;
			reset( _ang0SpinModel.getNumber().doubleValue(),
					_ang1SpinModel.getNumber().doubleValue());
		}
	}
	/**
	 * Pause Simulation.
	 */
	class PauseAction extends AbstractAction {
		public PauseAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			_fg_run = !_fg_run;
		}
	}
	/**
	 * Step Simulation.
	 */
	class StepAction extends AbstractAction {
		public StepAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			step(0.025);
		}
	}
	
	/**
	 * Temps maximum de la simulation.
	 * @return the _maxTime
	 */
	public double getMaxTime() {
		return _maxTime;
	}
	/**
	 * Défini le temps maximum de la simulation.
	 * @param maxTime the _maxTime to set
	 */
	public void setMaxTime(double maxTime) {
		this._maxTime = maxTime;
		_maxTimeSpinModel.setValue(_maxTime);
	}
	/**
	 * Chaque pas de simulation prend 'dt' secondes.
	 * @return the _dt
	 */
	public double getDT() {
		return _dt;
	}
	/**
	 * Choisi le deltaT de la simulation.
	 * @param dt the _dt to set
	 */
	public void setDT(double dt) {
		this._dt = dt;
		_dtSpinModel.setValue(_dt);
	}
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		ExpGUI app = new ExpGUI();
		app.readCommandSequences("data/consigne_example.data");
		app.reset( 10, 20);
		app.run();
	}
}
