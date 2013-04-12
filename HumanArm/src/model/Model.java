/**
 * 
 */
package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic Model in MVC design,
 * as explained in http://onjava.com/pub/a/onjava/2004/07/07/genericmvc.html
 * 
 * @author alain.dutech@loria.fr
 */
@SuppressWarnings("rawtypes")
public class Model <L extends ModelListener> {

	/** A list of Listeners */
	private final List <L> _listeners;

	/** Creation : initialize _listeners */
	public Model ()
	{
		this._listeners = new ArrayList <L> ();
	}
	
	/**
	 * Add a Listener if not existing and notify it.
	 */
	public void addModelListener (final L listener)
	{
		if (! this._listeners.contains (listener)) {
			this._listeners.add (listener);
			notifyModelListener (listener);
		}
	}
	/**
	 * Remove Listener
	 */
	public void removeModelListener (final L listener)
	{
		this._listeners.remove (listener);
	}

	/**
	 * Notify all Listeners
	 */
	public void notifyModelListeners ()
	{
		for (final L listener : this._listeners) {
			notifyModelListener (listener);
		}
	}
	/** 
	 * Notify given Listener.
	 */
	@SuppressWarnings("unchecked")
	public void notifyModelListener (final L listener)
	{
	    listener.update (this, null);
	}
	/** 
	 * Notify given Listener.
	 */
	@SuppressWarnings("unchecked")
	public void notifyModelListener (final L listener, Object arg)
	{
	    listener.update (this, arg);
	}
}

