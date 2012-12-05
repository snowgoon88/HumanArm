/**
 * 
 */
package example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import Jama.Matrix;

import model.Command;
import model.CommandSequence;
import model.CompleteArm;

/**
 * En utilisant les consignes du fichier 'consigne_example.data', simule le bras.
 * Toutes les données de simulation sont enregistrées dans 'data/result_exemple.data'.
 * 
 * Les consignes du fichier 'data/consigne_example.data' activent les muscles 1 et 4
 * pendant 300ms. On peut recréer ces données en utilisant la méthode 'makeExample'.
 * 
 * @author Alain.Dutech@loria.fr
 */
public class ExperienceConsole {

	/** Array of CommandSequence */
	CommandSequence[] _consigne = new CommandSequence[6];
	
	/** Le bras complet */
	CompleteArm _arm = new CompleteArm();
	
	/** Pour écrire dans un beau fichier en sortie */
	FileWriter _file = null;
	BufferedWriter _bw = null;
	
	/**
	 * Lance l'application.
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ExperienceConsole app = new ExperienceConsole();
		
		// Si besoin , on peut recréer les données exemples
		// app.makeExample( "data/consigne_example.data" );
		
		// Lit les consigne
		app.readCommandSequences("data/consigne_example.data");
		// Ouvre le fichier pour stocker les résultats
		app.openWriteFile("data/result_example.data");
		// Simule à partir de la position (20,30), pd 5 secondes, avec dt=25ms
		app.run( 20.0, 30.0, 5.0, 0.025);
		// Ferme le fichier résultat
		app.closeWriteFile();
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
	 * @throws IOException 
	 */
	public void run(double degAng0, double degAng1, double maxTime, double dt) throws IOException {
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
			
			// Ecrit dans fichier
			if (_bw != null ) {
				_bw.write(Double.toString(t));
				// Pour chaque muscle
				Matrix act = _arm.getMuscleActivation();
				Matrix tau = _arm.getMuscles().getTension();
				for (int i = 0; i < _consigne.length; i++) {
					_bw.write("\t"+Double.toString(u.get(0, i))
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
					_bw.write("\t"+Double.toString(cpl.get(0, i))
							+"\t"+Double.toString(ang.get(0, i))
							+"\t"+Double.toString(spd.get(0, i))
							+"\t"+Double.toString(x[i])
							+"\t"+Double.toString(y[i]));
				}
				_bw.newLine();
			}
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

	/**
	 * Open a file for Writing.
	 * @param fileName
	 * @throws IOException
	 */
	public void openWriteFile(String fileName) throws IOException {
		// open a file
		_file = new FileWriter( fileName );
		_bw = new BufferedWriter( _file );
		
		_bw.write( "# temps; Muscles: 6x(consigne,act,tension); Articulation: 2x(couple,angle,vitesse,x,y) ");
		_bw.newLine();
	}
	/** 
	 * Close opened file.
	 * @throws IOException
	 */
	public void closeWriteFile() throws IOException {
		_bw.close();
		_file.close();
	}
	
	/**
	 * Create Commands and store them in fileName.
	 * @param fileName Where to store the created CommandSequences.
	 * @throws IOException
	 */
	public void makeExample(String fileName) throws IOException {
		// Open up a file
		FileWriter myFile = new FileWriter( fileName );
		BufferedWriter myWriter = new BufferedWriter( myFile );

		CommandSequence com = new CommandSequence();
		
		// Biceps_court => flexion du coude.
		com.clear();
		com.setName("0-BicepsCourt");
		com.add(new Command(0, 0));
		com.write(myWriter);
		
		// Triceps latéral => extension du coude.
		com.clear();
		com.setName("1-TricepsLat");
		com.add(new Command(0, 0.1));
		com.add(new Command(0.3, 0.0));
		com.write(myWriter);
		
		// Deltoid anterieur => flexion de l'épaule
		com.clear();
		com.setName("2-DeltoidAnt");
		com.add(new Command(0, 0));
		com.write(myWriter);
		
		// Deltoid posterieur => extension de l'épaule
		com.clear();
		com.setName("3-DeltoidPost");
		com.add(new Command(0, 0));
		com.write(myWriter);
		
		// biceps long => flexion (épaule+coude)
		com.clear();
		com.setName("4-BicepsLong");
		com.add(new Command(0, 0.1));
		com.add(new Command(0.3, 0.0));
		com.write(myWriter);
		
		// triceps long => extension (épaule+coude)
		com.clear();
		com.setName("5-TricepsLong");
		com.add(new Command(0, 0));
		com.write(myWriter);
		
		myWriter.close();
        myFile.close();
	}
}
