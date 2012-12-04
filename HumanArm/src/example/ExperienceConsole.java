/**
 * 
 */
package example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import Jama.Matrix;

import model.CommandSequence;
import model.CompleteArm;

/**
 * En utilisant les consignes du fichier 'consigne_example.data', simule le bras.
 * 
 * @author Alain.Dutech@loria.fr
 */
public class ExperienceConsole {

	/** Array of CommandSequence */
	CommandSequence[] _consigne = new CommandSequence[6];
	
	/** Le bras complet */
	CompleteArm _arm = new CompleteArm();
	
	/**
	 * Lance l'application.
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ExperienceConsole app = new ExperienceConsole();
		app.readCommandSequences("consigne_example.data");
		app.run( 20.0, 30.0, 5.0, 0.025);

	}
	
	/**
	 * En partant d'une position donnée (ang0,ang1) en degrés, simule le bras
	 * avec les consignes lues, pour une donnée de 'maxTime', avec un intervale 
	 * de temps de dt.
	 * 
	 * @param degAng0 Angle0 initial, en degré.
	 * @param degAng1 Angle1 initial, en degré.
	 * @param maxTime Temps maximum de la simulation, en secondes.
	 * @param dt Intervale de temps pour la simulation, en secondes.
	 */
	public void run(double degAng0, double degAng1, double maxTime, double dt) {
		// Setup in resting position
		_arm.setup(Math.toRadians(degAng0), Math.toRadians(degAng1));
		System.out.println(_arm.toString());
		
		// Un vecteur (Matrix 1x6) de consignes musculaires, initialisée à 0.0.
		Matrix u = new Matrix(1,6, 0.0);

		// Utilise les CommandSequence pour avoir les consignes envoyées aux muscles.
		// Simulation pendant 5 secondes, par intervalles de 25 ms
		for (double t=0; t<maxTime; t += dt ) {
			// Quelles sont les consignes
			for (int i = 0; i < _consigne.length; i++) {
				CommandSequence cs = _consigne[i];
				// la valeur de la consigne est copiée dans le vecteur u
				u.set(0,i, cs.getValAtTimeFocussed(t));
			}
			// Applique les consignes sur le bras
			_arm.applyCommand(u, dt);
			System.out.println("TIME = "+t);
			System.out.println(_arm.toString());
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
		}
        
        myReader.close();
        myFile.close();
	}

}
