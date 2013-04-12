/**
 * 
 */
package model;

import java.util.Random;

/**
 * @author Alain.Dutech@loria.fr
 *
 */
public class NeuroControl {
	
	/** Muscle Activation value */
	private double _act;
	/** Command */
	private double _u;
	
	/** Activation delay (s)*/
	private double _tAct = 0.030;
	/** Deactivation delay (s) */
	private double _tDeact = 0.066;
	/** Noise level */
	private double _sigma = 0.2;
	
	/** Random number generator */
	Random rd = new Random();
	
	
	/**
	 * Création. Valeurs pas défaut sont nulles.
	 */
	public NeuroControl() {
		super();
		_act = 0.0;
		_u = 0.0;
	}
	
	/**
	 * Applique une consigne/commande d'activation pendant dt secondes.
	 * @param u : consigne (dans [0,1])
	 * @param dt : intervalle en s
	 * @return _act : valeur actuelle d'activation dans [0:1].
	 */
	public double applyCommand( double u, double dt) {
		assert u >= 0.0 : "u < 0";
		assert u <= 1.0 : "u > 1";

		_u = u;
		// compute d_a/dt
		double da = ((1 + rd.nextGaussian()*_sigma) * _u - _act);
		//System.out.println("da="+da);
		if (_u > _act) {
			da = da / (_tDeact + _u * (_tAct - _tDeact));
		} else {
			da = da / _tDeact;
		}
		//System.out.println("da="+da);
		// update _act
		_act = _act + da * dt;

		// check boundaries
		if (_act < 0.0) _act = 0.0;
		else if (_act > 1.0) _act = 1.0;

		return _act;
	}

	/**
	 * @return the _act
	 */
	public double getAct() {
		return _act;
	}
	/**
	 * Set activation value.
	 * @param a
	 */
	public void setAct(double a) {
		_act = a;
	}

	/**
	 * @return the _u
	 */
	public double getU() {
		return _u;
	}
	/**
	 * Set command.
	 * @param u
	 */
	public void setU(double u) {
		_u = u;
	}

	/**
	 * @return the _tAct
	 */
	public double getTActivation() {
		return _tAct;
	}
	/**
	 * @param _tAct the _tAct to set
	 */
	public void setTActivation(double tAct) {
		this._tAct = tAct;
	}

	/**
	 * @return the _tDeact
	 */
	public double getTDeactivation() {
		return _tDeact;
	}
	/**
	 * @param _tDeact the _tDeact to set
	 */
	public void setTDeactivation(double tDeact) {
		this._tDeact = tDeact;
	}

	/**
	 * @return the _sigma
	 */
	public double getNoiseLevel() {
		return _sigma;
	}
	/**
	 * @param _sigma the _sigma to set
	 */
	public void setNoiseLevel(double sigma) {
		this._sigma = sigma;
	}
}
