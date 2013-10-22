/**
 * 
 */
package test;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import utils.JamaU;
import model.ArmKaladjian;
import Jama.Matrix;


/**
 * @author alain.dutech@loria.fr
 */
public class TestArmKaladjian {

	JDialog _testDialog = new JDialog();
	/** Default Color */
	Color [] _defColors = {Color.blue, Color.red, Color.green,
			Color.cyan, Color.magenta, Color.pink, Color.black };
	
	/**
	 * Creation and all the tests
	 */
	public TestArmKaladjian() {
		System.out.println("***** TestArmKaladjian *****");
	}
		
	public void run(String[] args) {	
		boolean res;
		int nbTest = 0;
		int nbPassed = 0;

		// -------
		nbTest++;
		res = testPassiveForce(args);
		if (res) {
			System.out.println("testtestPassiveForce(args) >> " + res);
			nbPassed++;
		} else {
			System.err.println("testtestPassiveForce(args) >> " + res);
		}
		// -------
		nbTest++;
		res = testActiveForce(args);
		if (res) {
			System.out.println("testActiveForce >> " + res);
			nbPassed++;
		} else {
			System.err.println("testActiveForce >> " + res);
		}
		
		if (nbTest > nbPassed) {
			System.err.println("FAILURE : only "+nbPassed+" success out of "+nbTest);
			System.exit(1);
		}
		else {
			System.out.println("SUCCESS : "+nbPassed+" success out of "+nbTest);
			System.exit(0);
		}
	}
	
	/** 
	 * Génère et affiche les forces passives pour le coude, angle in [0,180].
	 * Progression qui doit être à peut près linéaire
	 * (modélisé comme des ressorts linéaires)
	 */
	boolean testPassiveForce(String[] args) {
		boolean res = true;
		ArmKaladjian arm = new ArmKaladjian();
		
		// Elements graphiques
		// Chart2D for articulations : 2 x 1 Traces (Couple)
		JPanel forcePanel = new JPanel(new BorderLayout());
		Chart2D forceChart = new Chart2D();
		forcePanel.add(forceChart, BorderLayout.CENTER);
		Trace2DSimple[] forceTraces = new Trace2DSimple[6];
		for (int i = 0; i < forceTraces.length; i++) {
			forceTraces[i] = new Trace2DSimple("vFP_"+i);
			forceChart.addTrace(forceTraces[i]);
			forceTraces[i].setColor(_defColors[i % _defColors.length]);
			forceTraces[i].setVisible(true);
		}
		forcePanel.setPreferredSize(new Dimension(600, 300));
		forcePanel.setVisible(true);
		
		// Angles
		Matrix theta = new Matrix(1, 2, 0.0);
		double[] vFP;
		theta.set(0, 0, 0);
		for (double ang2 = 0.0; ang2 < 180.0; ang2 += 1.0) {
			theta.set(0, 0, Math.toRadians(ang2)/2);
			theta.set(0, 1, Math.toRadians(ang2));
			vFP = arm.passiveForce(theta).getRowPackedCopy();
			for (int i = 0; i < forceTraces.length; i++) {
				forceTraces[i].addPoint(ang2, vFP[i]);
			}
		}
		
		res = testComponent("Forces Passives", forcePanel);

		return res;
	}
	/** 
	 * Génère et affiche Forces Actives minimales pour maintenir 
	 * le bras en (25,[0,180]).
	 */
	boolean testActiveForce(String[] args) {
		boolean res = true;
		ArmKaladjian arm = new ArmKaladjian();
		
		// Elements graphiques
		// Chart2D for articulations : 2 x 1 Traces (Couple)
		JPanel forcePanel = new JPanel(new BorderLayout());
		Chart2D forceChart = new Chart2D();
		forcePanel.add(forceChart, BorderLayout.CENTER);
		Trace2DSimple[] forceTraces = new Trace2DSimple[6];
		for (int i = 0; i < forceTraces.length; i++) {
			forceTraces[i] = new Trace2DSimple("vFA_"+i);
			forceChart.addTrace(forceTraces[i]);
			forceTraces[i].setColor(_defColors[i % _defColors.length]);
			forceTraces[i].setVisible(true);
		}
		forcePanel.setPreferredSize(new Dimension(600, 300));
		forcePanel.setVisible(true);
		
		// Angles
		Matrix theta = new Matrix(1, 2, 0.0);
		Matrix F;
		theta.set(0, 0, Math.toRadians(25));
//		theta.set(0, 1, Math.toRadians(170.0));
//		arm.findContractionLevel(theta);
		for (double ang2 = 0.0; ang2 < 180.0; ang2 += 1.0) {
			//theta.set(0, 0, Math.toRadians(ang2)/2);
			theta.set(0, 1, Math.toRadians(ang2));
			System.out.println("Looking for (25,"+ang2+") "+JamaU.vecToString(theta));
			F = arm.findContractionLevel(theta);
			for (int i = 0; i < forceTraces.length; i++) {
				forceTraces[i].addPoint(ang2, F.get(1, i));
			}
		}
		
		res = testComponent("Forces Active", forcePanel);

		return res;
	}
	
	
	/**
     * Utilise un JDialog modal (freeze until all event are processed)
     * pour afficher et tester un Component
     *
     * @param title
     * @param thing
     * @return
     */
    boolean testComponent(String title, Component thing) {
        _testDialog = new JDialog();
        _testDialog.setModal(true);
        _testDialog.setTitle(title);
        _testDialog.add(thing);
        _testDialog.pack();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
               
                @Override
                public void run() {
                    _testDialog.setVisible(true);
                   
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
       
        return true;
    }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestArmKaladjian app = new TestArmKaladjian();
		app.run(args);
	}

}
