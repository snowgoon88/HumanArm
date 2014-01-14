/**
 * 
 */
package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;

/**
 * 'Consignes' mémorise l'ensemble des consignes qui seront envoyées 
 * au Arm.
 * 
 * On peut les lire depuis un fichier (read), les sauvegardfer (write) et les modifiers.
 * 
 * Signaux envoyés aux Observers:
 *  - sans argument : tout à changé
 *  - int (index) : index du ComSeq qui a changé
 * 
 * @author Alain.Dutech@loria.fr
 *
 */
public class Consignes extends Observable {

	/** Array of CommandSequence */
	CommandSequence [] _comSeq;
	
	/**
	 * Création avec in nb donné de ComSeq initales.
	 */
	public Consignes( int nbConsignes ) {
		// Initialisation par défaut 
		_comSeq = new CommandSequence[nbConsignes];
		for (int i = 0; i < _comSeq.length; i++) {
			_comSeq[i] = new CommandSequence();
		}
		setChanged();
		notifyObservers();
	}
	
	/**
	 * La {@link CommandSequence} stockée à cet index.
	 */
	public CommandSequence get( int index) {
		return _comSeq[index];
	}
	/**
	 * Modifiela {@link CommandSequence} stockèe à cet index.
	 */
	public void set( int index, CommandSequence cs ) {
		_comSeq[index] = cs;
		setChanged();
		notifyObservers(index);
	}
	/** 
	 * Nombre de {@link CommandSequence} dans la Consigne.
	 */
	public int size() {
		return _comSeq.length;
	}
	
	/**
	 * Read from a file.
	 * @param fileName Name of the file
	 * @throws IOException
	 */
	public void read(String fileName) throws IOException {
		FileReader myFile = new FileReader( fileName );
        BufferedReader myReader = new BufferedReader( myFile );
        
        // Need to read 6 CommandSequence
        for (int i = 0; i < _comSeq.length; i++) {
        	
        	_comSeq[i] = new CommandSequence();
        	_comSeq[i].read(myReader);
		}
        setChanged();
        notifyObservers();
        
        myReader.close();
        myFile.close();
	}
	/**
	 * Write to a file.
	 * @param fileName Name of the file
	 * @throws IOException
	 */
	public void write(String fileName) throws IOException {
		FileWriter myFile = new FileWriter( fileName );
        BufferedWriter myWriter = new BufferedWriter( myFile );
        
        // Need to write 6 CommandSequence
        for (int i = 0; i < _comSeq.length; i++) {
        	_comSeq[i].write(myWriter);
		}
        
        myWriter.close();
        myFile.close();
	}

}
