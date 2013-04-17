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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import utils.GraphicHelper;

import Jama.Matrix;

import model.CommandSequence;
import model.CompleteArm;
import model.Consignes;


/**
 * Démonstration d'une application graphique évoluée pour "jouer" avec le bras.
 * Cycle de "travail" classique.
 * - charger des consignes ("Ouvrir")
 * - modifier ces consignes dans l'onglet "Consignes")
 * - (sauvegarder ces consignes ("Sauver")
 * - choisir et régler les paramètres de la simulation (maxTime, dt, ang0, ang1)
 * - lancer une simulation ("Reset" puis "Play", avec éventuellement "Pause")
 * - les différents onglets permettent de suivre l'évolution des variables internes importantes.
 * - une fois satisfait, on peut sauvergarder (loguer) cette simulation dans
 * un fichier ("Log Experience").
 * 
 * Il y a des choses qui pourraient être améliorées.
 * - faire de Experience une classe instanciant "Model" et de JExperience un "ModelListener".
 * - se donner la possibilité de calculer, sans l'appliquer, les forces et couples exercés
 * par les muscles sur le bras dans une position+vitesse donnée.
 * - mieux gérer les exceptions et "throws" un peu partout.
 * - Ajouter un Menu, des icones, etc.
 * 
 * @author Alain.Dutech@loria.fr
 *
 */
public class ExpGUI {
	
	/** The complete arm simulated */
	CompleteArm _arm;
	/** Array of CommandSequence */
	Consignes _consignes;
	
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
	/** Action to load consignes */
	Action _loadAct;
	/** Action to save consignes */
	Action _saveAct;
	/** Action to log experience into a file */
	Action _logAct;

	/** Models for setting angle0 */
	SpinnerNumberModel _ang0SpinModel;
	/** Models for setting angle1 */
	SpinnerNumberModel _ang1SpinModel;
	/** Model for setting _maxTime */
	SpinnerNumberModel _maxTimeSpinModel;
	/** Model for setting _dt */
	SpinnerNumberModel _dtSpinModel;
	
	/** FileChooser panel */
	JFileChooser _fileChooser;
	
	/**
	 * 
	 */
	public ExpGUI() {
		// Modèle initiaux.
		_arm = new CompleteArm();
		int nbConsigne = _arm.getArrayNeuroControlers().length;
		_consignes = new Consignes(nbConsigne);
		
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
				KeyEvent.VK_T);
		_pauseAct = new PauseAction("Pause", null,
				"Pause/reprend la simulation",
				KeyEvent.VK_P);
		_stepAct = new StepAction("Step", null,
				"Un pas de simulation",
				KeyEvent.VK_E);
		_loadAct = new LoadAction("Ouvrir", null,
				"Ouvre un nouveau fichier de consignes",
				KeyEvent.VK_O);
		_saveAct = new SaveAction("Sauver", null,
				"Sauve les consignes dans un fichier",
				KeyEvent.VK_S);
		_logAct = new LogAction("Log Experience", null,
				"Log une simulation dans un fichier",
				KeyEvent.VK_G);
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

		_xpPanel = new JExperience(_arm , _consignes);
		frame.add(_xpPanel, BorderLayout.CENTER);
		
		_toolBar = new JPanel();
		// Load Consignes
		JButton openBtn = new JButton(_loadAct);
		_toolBar.add(openBtn);
		// Save Consignes
		JButton saveBtn = new JButton(_saveAct);
		_toolBar.add(saveBtn);
		
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
		
		// Button for logging
		JButton logBtn = new JButton(_logAct);
		_toolBar.add(logBtn);
		
		frame.add(_toolBar, BorderLayout.NORTH);
		frame.setVisible(true);
		
		// File Chooser
		_fileChooser = new JFileChooser("data");
	}

	public void reset( double degAng0, double degAng1) {
		// Setup in resting position
		_arm.setup(Math.toRadians(degAng0), Math.toRadians(degAng1));
		_ang0SpinModel.setValue(degAng0);
		_ang1SpinModel.setValue(degAng1);
		
		// Un vecteur (Matrix 1x6) de consignes musculaires, initialisée à 0.0.
		for (int i = 0; i < 6; i++) {
			_u.set(0,i, 0.0);
		}
		
		_t = 0.0;
		
		_xpPanel.reset();
	}
	private void step( double dt ) {
		for (int i = 0; i < _consignes.size(); i++) {
			CommandSequence cs = _consignes.get(i);
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
				step( _dt );
			}
			Thread.sleep(10);
			_xpPanel.repaint();
		}
	}
	public void runLogged(String fileName, double degAng0, double degAng1) throws IOException {
		// open a file
		FileWriter file = new FileWriter( fileName );
		BufferedWriter bw = new BufferedWriter( file );

		bw.write( "# temps; Muscles: 6x(consigne,act,tension); Articulation: 2x(couple,angle,vitesse,x,y) ");
		bw.newLine();
		
		reset(degAng0, degAng1);
		while( _t < _maxTime ) {
			step( _dt );

			// Ecrit dans fichier
			bw.write(Double.toString(_t));
			// Pour chaque muscle
			Matrix act = _arm.getMuscleActivation();
			Matrix tau = _arm.getMuscles().getTension();
			for (int i = 0; i < _consignes.size(); i++) {
				bw.write("\t"+Double.toString(_u.get(0, i))
						+"\t"+Double.toString(act.get(0, i))
						+"\t"+Double.toString(tau.get(0, i)));
			}
			// Pour chaque articulation
			Matrix cpl = _arm.getMuscles().getTorque();
			Matrix ang = _arm.getArm().getArmPos();
			Matrix spd = _arm.getArm().getArmSpeed();
			double[] x = _arm.getArm().getArmX();
			double[] y = _arm.getArm().getArmY();
			for (int i = 0; i < 2; i++) {
				bw.write("\t"+Double.toString(cpl.get(0, i))
						+"\t"+Double.toString(ang.get(0, i))
						+"\t"+Double.toString(spd.get(0, i))
						+"\t"+Double.toString(x[i])
						+"\t"+Double.toString(y[i]));
			}
			bw.newLine();
		}
		bw.close();
		file.close();
	}

	/**
	 * Read the 6 CommandSequence from a file.
	 * @param fileName Name of the file
	 * @throws IOException
	 */
	public void readCommandSequences(String fileName) throws IOException {
		_consignes.read(fileName);
		
        // Need to update _xpPanel
        for (int i = 0; i < _consignes.size(); i++) {
			_xpPanel.addConsigne(_consignes.get(i));
		}
	}
	/**
	 * Write the 6 CommandSequence to a file.
	 * @param fileName Name of the file
	 * @throws IOException
	 */
	public void writeCommandSequences(String fileName) throws IOException {
		_consignes.write(fileName);
	}

	/**
	 * Reset Arm.
	 */
	@SuppressWarnings("serial")
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
	@SuppressWarnings("serial")
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
	@SuppressWarnings("serial")
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
	@SuppressWarnings("serial")
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
	@SuppressWarnings("serial")
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
	 * Load Consignes as array of CommandSequence from a file.
	 */
	@SuppressWarnings("serial")
	class LoadAction extends AbstractAction {
		public LoadAction(String text, ImageIcon icon, String desc, Integer mnemonic)  {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			// First, remove old Consignes
			_xpPanel.removeAllConsigne();
			
			// Then ask about file to open
			int returnVal = _fileChooser.showOpenDialog(_xpPanel);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					readCommandSequences(_fileChooser.getSelectedFile().getAbsolutePath());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
		}
	}
	/** 
	 * Save Consignes in a file.
	 */
	@SuppressWarnings("serial")
	class SaveAction extends AbstractAction {
		public SaveAction(String text, ImageIcon icon, String desc, Integer mnemonic)  {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			// Ask about file to save
			int returnVal = _fileChooser.showSaveDialog(_xpPanel);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					writeCommandSequences(_fileChooser.getSelectedFile().getAbsolutePath());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
		}
	}
	/** 
	 * Log experience in a file
	 */
	@SuppressWarnings("serial")
	class LogAction extends AbstractAction {
		public LogAction(String text, ImageIcon icon, String desc, Integer mnemonic)  {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			// Ask about file to save
			int returnVal = _fileChooser.showDialog(_xpPanel, "Log into");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				_resetAct.setEnabled(false);
				try {
					runLogged(_fileChooser.getSelectedFile().getAbsolutePath(),
							_ang0SpinModel.getNumber().doubleValue(),
							_ang1SpinModel.getNumber().doubleValue());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				_resetAct.setEnabled(true);
				
				// say it ok
				JOptionPane.showMessageDialog(_xpPanel, "Expérience enregistrée dans "+_fileChooser.getSelectedFile().getAbsolutePath());
            }
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
