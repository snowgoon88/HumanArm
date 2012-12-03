/**
 * 
 */
package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;

/**
 * @author Alain.Dutech@loria.fr
 *
 */
public class CommandSequence extends Model<CommandSequenceListener> implements Iterable<Command> {
	
	/** List of Commands */
	LinkedList<Command> _commands;
	/** Current Command */
	Command _current;
	/** Next Command */
	Command _next;
	/** Iterator on next Command */
	ListIterator<Command> _it_com;
	/** Name */
	String _name = "-";
	
	public CommandSequence() {
		_commands = new LinkedList<Command>();
		_current = null;
		_next = null;
		_it_com = null;
	}
	public CommandSequence(String name) {
		_name = name;
		_commands = new LinkedList<Command>();
		_current = null;
		_next = null;
		_it_com = null;
	}
	
	/**
	 * Quelle est la valeur de la consigne au temps 'time' ?
	 * Commence la recherche à partir du début de la liste des commandes.
	 * @param time temps où l'on veut la valeur de la consigne
	 * @return -1 (pas trouvé) ou Command.val de la bonne Command.
	 */
	public double getValAtTime(double time) {
		if (isEmpty()) {
			return -1;
		}
		
		// test first element
		Iterator<Command> it = _commands.iterator();
		Command com = it.next(); // first command
		if (com.time > time) { // Before the first Command
			return -1;
		}
		while (it.hasNext()) {
			Command next_com = (Command) it.next();
			// time lies between the two considered elements
			if (time >= com.time && time < next_com.time ) {
				return com.val;
			}
			com = next_com;
		}
		return com.val;
	}
	
	/**
	 * Quelle est la valeur de la consigne au temps 'time' ?
	 * Mémorise la Command courante et un itérateur qui pointe sur la 
	 * prochaine Command pour accélérer le calcul de la valeur 
	 * en passant par getValAtTimeFocussed.
	 * @param time
	 * @return
	 */
	public double focusAtTime(double time) {
		if (isEmpty()) {
			return -1;
		}
		
		_it_com = _commands.listIterator();
		_current = _it_com.next();
		while (_it_com.hasNext()) {
			_next = _it_com.next();
			if (time >= _current.time && time < _next.time ) {
				return _current.val;
			}
			_current = _next;
		}
		return _current.val;
	}
	/**
	 * Quelle est la valeur de consigne au temps 'time' ?
	 * Utilise la valeur mémorisée (par un focusAtTime) pour
	 * accélérer la recheche de la prochaine valeur.
	 * Marche le mieux quand c'est juste un peu plus "tard" que le dernier
	 * getValAtTimeFocussed ou focussAtTime.
	 * @param time
	 * @return
	 */
	public double getValAtTimeFocussed(double time) {
		// if not focussed, focus
		if (_current == null ) {
			return focusAtTime(time);
		}
		// if too early, refocus
		if (time < _current.time ) {
			return focusAtTime(time);
		}
		
		// Check if still in the right time intervale
		if (time < _next.time) {
			return _current.val;
		}
		
		// look for the next if possible
		while (_it_com.hasNext()) {
			_next = _it_com.next();
			if (time >= _current.time && time < _next.time ) {
				return _current.val;
			}
			_current = _next;
		}
		return _current.val;
	}
	
	public int size() {
		return _commands.size();
	}
	public boolean isEmpty() {
		return _commands.isEmpty();
	}
	
	public boolean contains(Command obj) {
		return _commands.contains( obj );
	}
	/** 
	 * Cherche un Command donné par (time,val).
	 * Retourne null si pas trouvé.
	 * @return Command trouvé ou null
	 */
	public Command finds(double time, double val) {
		for (Command com : _commands) {
			if (com.time == time && com.val == val) {
				return com;
			}
		}
		return null;
	}
	
	@Override
	public Iterator<Command> iterator() {
		return _commands.iterator();
	}

	public boolean add(Command obj) {
		boolean res = _commands.add( obj );
		Collections.sort(_commands);
		_next = null;
		_it_com = null;
		notifyModelListeners();
		return res;
	}
	public boolean remove(Command obj) {
		boolean res=_commands.remove( obj );
		Collections.sort(_commands);
		_next = null;
		_it_com = null;
		notifyModelListeners();
		return res;
	}
	public void changeCommand(Command obj, double time, double val ) {
		System.out.println("Changing "+obj.toString());
		obj.time = time;
		obj.val = val;
		Collections.sort(_commands);
		_next = null;
		_it_com = null;
		
		notifyModelListeners();
		System.out.println(this.toString());
	}
	
	public void clear() {
		_commands.clear();
		notifyModelListeners();
	}

	/**
	 */
	@Override
	public String toString() {
		String str = "CS["+_name+"]=" + _commands;
		str += "\n  Current "+_next;
		return str;
	}
	
	/**
	 * Ecrit une CommandSequence dans un fichier.
	 * @param fileName Nom du fichier.
	 * @throws IOException
	 */
	public void write(String fileName) throws IOException {
		// open a file
		FileWriter myFile = new FileWriter( fileName );
        BufferedWriter myWriter = new BufferedWriter( myFile );
        
        write( myWriter );
        
        myWriter.close();
        myFile.close();
	}
	public void write(BufferedWriter w) throws IOException {
		w.write( "# CommandSequence : "+_name);
        w.newLine();
        
        w.write(_name);
        w.newLine();
        
        w.write( Integer.toString(_commands.size()));
        w.newLine();
        
        for (Command com : _commands) {
			w.write(Double.toString(com.time)+"\t"+Double.toString(com.val));
			w.newLine();
		}
        
	}
	
	public void read(String fileName) throws IOException {
		FileReader myFile = new FileReader( fileName );
        BufferedReader myReader = new BufferedReader( myFile );
        
        read( myReader );
        
        myReader.close();
        myFile.close();
	}
	
	public void read( BufferedReader br) throws IOException {
		String lineRead = br.readLine();
        
        _current = null;
		_next = null;
		_it_com = null;
		_commands.clear();
        
		// read out name
		while(lineRead != null) {
			// ignore if begins with "#"
			if (lineRead.startsWith("#") ==  false) {
				StringTokenizer st = new StringTokenizer( lineRead, " \t");
				String token;
				// should read size
				token = st.nextToken();
				_name = token;
				lineRead = br.readLine();
				break;
			}
			lineRead = br.readLine();
		}
		// read out size
		int size = -1;
        while(lineRead != null && size < 0) {
        	// ignore if begins with "#"
        	if (lineRead.startsWith("#") ==  false) {
        		StringTokenizer st = new StringTokenizer( lineRead, " \t");
                String token;
                // should read size
                token = st.nextToken();
                size = Integer.parseInt(token);
        	}
        	lineRead = br.readLine();
        }
        int nbRead = 0;
        while(lineRead != null && nbRead < size) {
        	if (lineRead.startsWith("#") ==  false) {
        		StringTokenizer st = new StringTokenizer( lineRead, " \t");
                String token;

                // should read time
                token = st.nextToken();
                double t = Double.parseDouble(token);
                // then val
                token = st.nextToken();
                double v = Double.parseDouble(token);
                
                _commands.add( new Command(t,v));
                nbRead += 1;
        	}
        	lineRead = br.readLine();
        }
        Collections.sort(_commands);
		notifyModelListeners();
	}
	/**
	 * @return the _name
	 */
	public String getName() {
		return _name;
	}
	/**
	 * @param name the _name to set
	 */
	public void setName(String name) {
		this._name = name;
		notifyModelListeners();
	}
}
