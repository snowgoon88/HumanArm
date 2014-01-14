/**
 * 
 */
package example;

import Jama.Matrix;
import model.CompleteArm;

/**
 * Exemple d'utilisation du package 'model' pour simuler un Bras complet.
 * 
 * @author Alain.Dutech@loria.fr
 *
 */
public class ArmConsole {

	/** Le bras complet */
	CompleteArm _arm = new CompleteArm();
	
	/**
	 * Après avoir mis le bras dans une position de départ (0,45°),
	 * on applique une consigne à 2 muscles pendant 300 ms avant
	 * de tout relacher. Le bras continue un peu sur sa lancée...
	 */
	public void run() {
		// Setup in resting position
		_arm.setup(0.0, Math.toRadians(45));
		System.out.println(_arm.toString());
		
		// Un vecteur (Matrix 1x6) de consignes musculaires, initialisée à 0.0.
		Matrix u = new Matrix(1,6, 0.0);
		
		// Contracte le triceps (no 1) et le biceps court (no 4) pendant 300 ms
		// Simulation pendant 5 secondes, par intervalles de 25 ms
		double dt = 0.025;
		u.set(0, 1, 0.1);
		u.set(0, 4, 0.1);
		for (double t=0; t<5.0; t += dt ) {
			_arm.applyCommand(u, dt);
			System.out.println("TIME = "+t);
			System.out.println(_arm.toString());
			
			if (t>0.300) {
				u.set(0, 1, 0.0);
				u.set(0, 4, 0.0);
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArmConsole app = new ArmConsole();
		app.run();
	}

}
