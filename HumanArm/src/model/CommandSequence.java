/**
 * 
 */
package model;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author Alain.Dutech@loria.fr
 *
 */
public class CommandSequence {
	
	/** List of Commands */
	LinkedList<Command> _commands;
	/** Current Command */
	Command _current;
	/** Next Command */
	Command _next;
	/** Iterator on next Command */
	ListIterator<Command> _it_com;
	
	public CommandSequence() {
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
	
	public Iterator<Command> iterator() {
		return _commands.iterator();
	}

	public boolean add(Command obj) {
		boolean res = _commands.add( obj );
		Collections.sort(_commands);
		_next = null;
		_it_com = null;
		return res;
	}
	public boolean remove(Command obj) {
		boolean res=_commands.remove( obj );
		Collections.sort(_commands);
		_next = null;
		_it_com = null;
		return res;
	}
	
	public void clear() {
		_commands.clear();
	}

	/**
	 */
	@Override
	public String toString() {
		String str = "CommandSequence=" + _commands;
		str += "\n  Current "+_next;
		return str;
	}

}
