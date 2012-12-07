package gui;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import javax.swing.JPanel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Stroke;

import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import Jama.Matrix;

import model.CommandSequence;
import model.CompleteArm;
import model.NeuroControl;

import viewer.JArm2D;
import viewer.JArmLabel;
import viewer.JCommandSequence;

/**
 * @author Alain.Dutech@loria.fr
 *
 */
@SuppressWarnings("serial")
public class JExperience extends JPanel {

	
	/** A CompleteArm */
	CompleteArm _compArm;
	
	/** Default Color */
	Color [] _defColors = {Color.blue, Color.red, Color.green,
			Color.cyan, Color.magenta, Color.pink, Color.black };
	
	/** Traces for muscle consigne */
	ITrace2D [] _conTraces;
	/** Traces for muscle activation */
	ITrace2D [] _actTraces;
	/** Traces for muscle normalized tension */
	ITrace2D [] _tensionTraces;
	/** Traces for muscle real force */
	ITrace2D [] _forceTraces;
	/** Traces for joint torque */
	ITrace2D [] _cplTraces;
	/** Traces for angle position */
	ITrace2D [] _angTraces;
	/** Traces for angle speed */
	ITrace2D [] _spdTraces;
 	
	/** Panel for displaying Arm */
	JArm2D _jArm;
	/** Panel for editing consignes */
	JCommandSequence _comSeqViewer;
	
	final int _nbMuscles = 6;
	final int _nbJoint = 2;
	
	/**
	 * Create the panel.
	 */
	public JExperience( CompleteArm arm ) {
		super();
		
		this._compArm = arm;
		
		buildGUI();
	}
	
	private void buildGUI() {
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		add(splitPane, BorderLayout.CENTER);
		
		// Left : JArm2D above with JArmLabel and JArmControl
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout(0, 0));
		_jArm = new JArm2D(_compArm.getArm());
		_jArm.setMemorySize(20*40); // dt=25ms, 20 secondes
		leftPanel.add(_jArm, BorderLayout.CENTER);
		leftPanel.add(_jArm.getControlPanel(), BorderLayout.SOUTH);
		JArmLabel jArmInfo = new JArmLabel(_compArm.getArm());
		leftPanel.add(jArmInfo, BorderLayout.NORTH);
		_compArm.getArm().addModelListener(_jArm);
		_compArm.getArm().addModelListener(jArmInfo);
		
		// Right : Tabs of Chart2D
		splitPane.setLeftComponent(leftPanel);
		
		JTabbedPane rightTabPane = new JTabbedPane(JTabbedPane.TOP);
		splitPane.setRightComponent(rightTabPane);
		
		// CommandSequence Editor
		_comSeqViewer = new JCommandSequence();
		rightTabPane.addTab("Consignes", null, _comSeqViewer, null);
		
		// 
		Stroke dashStroke = new BasicStroke(1.0f,                      // Width
                BasicStroke.CAP_SQUARE,    // End cap
                BasicStroke.JOIN_MITER,    // Join style
                10.0f,                     // Miter limit
                new float[] {10.0f,10.0f}, // Dash pattern
                0.0f);                     // Dash phase
		Stroke pointStroke = new BasicStroke(1.0f,                      // Width
                BasicStroke.CAP_SQUARE,    // End cap
                BasicStroke.JOIN_MITER,    // Join style
                10.0f,                     // Miter limit
                new float[] {2.0f,8.0f}, // Dash pattern
                0.0f);                     // Dash phase
		
		// Chart2D for muscles : 6 x 3 Traces (consigne,act,tension)
		JPanel musclePanel = new JPanel(new BorderLayout());
		JLabel muscleLabel = new JLabel("... Consigne, - - - Activation, --- Tension");
		musclePanel.add(muscleLabel, BorderLayout.SOUTH);
		Chart2D muscleChart = new Chart2D();
		musclePanel.add(muscleChart, BorderLayout.CENTER);
		_conTraces = new Trace2DSimple[_nbMuscles];
		for (int i = 0; i < _conTraces.length; i++) {
			_conTraces[i] = new Trace2DSimple("con_"+i);
			muscleChart.addTrace(_conTraces[i]);
			_conTraces[i].setColor(_defColors[i % _defColors.length]);
			_conTraces[i].setStroke(pointStroke);
			_conTraces[i].setVisible(true);
		}
		_actTraces = new Trace2DSimple[_nbMuscles];
		for (int i = 0; i < _actTraces.length; i++) {
			_actTraces[i] = new Trace2DSimple("act_"+i);
			muscleChart.addTrace(_actTraces[i]);
			_actTraces[i].setColor(_defColors[i % _defColors.length]);
			_actTraces[i].setStroke(dashStroke);
			_actTraces[i].setVisible(true);
		}
		_tensionTraces = new Trace2DSimple[_nbMuscles];
		for (int i = 0; i < _tensionTraces.length; i++) {
			_tensionTraces[i] = new Trace2DSimple("tension_"+i);
			muscleChart.addTrace(_tensionTraces[i]);
			_tensionTraces[i].setColor(_defColors[i % _defColors.length]);
		}
		rightTabPane.addTab("Muscles", null, musclePanel, null);
		
		
		// Chart2D for forces: 6 x 1 Traces (Force)
		Chart2D forceChart = new Chart2D();
		_forceTraces = new Trace2DSimple[_nbMuscles];
		for (int i = 0; i < _forceTraces.length; i++) {
			_forceTraces[i] = new Trace2DSimple("for_"+i);
			forceChart.addTrace(_forceTraces[i]);
			_forceTraces[i].setColor(_defColors[i % _defColors.length]);
		}
		rightTabPane.addTab("Forces", null, forceChart, null);
		
		// Chart2D for articulations : 2 x 1 Traces (Couple)
		JPanel jointPanel = new JPanel(new BorderLayout());
		JLabel jointLabel = new JLabel("... Couple, --- Angles, - - - Vitesse");
		jointPanel.add(jointLabel, BorderLayout.SOUTH);
		Chart2D jointChart = new Chart2D();
		jointPanel.add(jointChart, BorderLayout.CENTER);
		_cplTraces = new Trace2DSimple[_nbJoint];
		for (int i = 0; i < _cplTraces.length; i++) {
			_cplTraces[i] = new Trace2DSimple("cpl_"+i);
			jointChart.addTrace(_cplTraces[i]);
			_cplTraces[i].setColor(_defColors[i % _defColors.length]);
			_cplTraces[i].setStroke(pointStroke);
		}
		_angTraces = new Trace2DSimple[_nbJoint];
		for (int i = 0; i < _angTraces.length; i++) {
			_angTraces[i] = new Trace2DSimple("ang_"+i);
			jointChart.addTrace(_angTraces[i]);
			_angTraces[i].setColor(_defColors[i % _defColors.length]);
		}
		_spdTraces = new Trace2DSimple[_nbJoint];
		for (int i = 0; i < _spdTraces.length; i++) {
			_spdTraces[i] = new Trace2DSimple("spd_"+i);
			jointChart.addTrace(_spdTraces[i]);
			_spdTraces[i].setColor(_defColors[i % _defColors.length]);
			_spdTraces[i].setStroke(dashStroke);
		}
		rightTabPane.addTab("Articulations", null, jointPanel, null);

		this.setVisible(true);
	}
	
	public void reset() {
		for (int i = 0; i < _nbMuscles; i++) {
			_conTraces[i].removeAllPoints();
			_actTraces[i].removeAllPoints();
			_tensionTraces[i].removeAllPoints();
			_forceTraces[i].removeAllPoints();
		}
		for (int i = 0; i < _nbJoint; i++) {
			_cplTraces[i].removeAllPoints();
			_angTraces[i].removeAllPoints();
			_spdTraces[i].removeAllPoints();
		}
		_jArm.resetMemory();
		repaint();
	}

	public void update( double t) {
		NeuroControl[] con = _compArm.getArrayNeuroControlers();
		Matrix act = _compArm.getMuscleActivation();
		Matrix tau = _compArm.getMuscles().getTensionN();
		Matrix str = _compArm.getMuscles().getTension();
		for (int i = 0; i < _nbMuscles; i++) {
			_conTraces[i].addPoint(t, con[i].getU());
			_actTraces[i].addPoint(t, act.get(0,i));
			_tensionTraces[i].addPoint(t, tau.get(0,i));
			_forceTraces[i].addPoint(t, str.get(0,i));
		}
		Matrix cpl = _compArm.getMuscles().getTorque();
		Matrix ang = _compArm.getArm().getArmPos();
		Matrix spd = _compArm.getArm().getArmSpeed();
		double[] x = _compArm.getArm().getArmX();
		double[] y = _compArm.getArm().getArmY();
		for (int i = 0; i < _nbJoint; i++) {
			_cplTraces[i].addPoint(t, cpl.get(0, i));
			_angTraces[i].addPoint(t, ang.get(0, i));
			_spdTraces[i].addPoint(t, spd.get(0, i));
		}
		
		repaint();
	}
	
	public void addConsigne( CommandSequence consigne ) {
			_comSeqViewer.add( consigne );
			consigne.addModelListener(_comSeqViewer);
			
			repaint();
	}
}
