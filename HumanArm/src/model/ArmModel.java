/**
 * 
 */
package model;

import java.util.Observable;

/**
 * 
 * @author alain.dutech@loria.fr
 *
 */
public class ArmModel extends Observable {

	/** Dimension of the state space */
	static int _dimQ = 2;
	/** end points */
	double [] _posX = new double[_dimQ+1];
	double [] _posY = new double[_dimQ+1];
	
	/** Length : m */
	double[] _l = null;
	/** Bound for values */
	boolean _fg_bounded = true;
	ArmConstraints _constraints = new ArmConstraints();
	
	
	/**
	 * 
	 */
	public ArmModel() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Get the x-positions of base and segment endpoints of Arm.
	 * @return _posX
	 */
	public double [] getArmX() {
		return _posX;
	}
	/**
	 * Get the y-position of base and segment endpoints of Arm.
	 * @return _posY
	 */
	public double [] getArmY() {
		return _posY;
	}
	
	/**
	 * Length of the segments.
	 * @return _l (length in meters)
	 */
	public double[] getLength() {
		return _l;
	}
	
	/**
	 * Get the constraints apply on the arm (boundaries).
	 * @return _contraints
	 */
	public ArmConstraints getConstraints() {
		return _constraints;
	}
	/**
	 * Change the arm constraints (boundaries).
	 * @param constraints
	 */
	public void setConstraints(ArmConstraints constraints) {
		this._constraints = constraints;
	}
	
	/**
	 * Test if a position (x,y) is reachable by the arm or not.
	 * 
	 * @param x the X-coordinate of the point to test
	 * @param y the Y-coordinate of the point to test
	 * @return true if the point is reachable, false if not.
	 */
	public boolean isPointReachable(double x, double y) {
		final double l0_2 = _l[0] * _l[0];
		final double l1_2 = _l[1] * _l[1];

		// In Big cicle
		double r = x * x + y * y;
		if (r > (_l[0] + _l[1]) * (_l[0] + _l[1]))
			return false;

		// Out of the little circle, thanks Al-Kashi
		double c_2 = l0_2 + l1_2 - 2 * _l[0] * _l[1]
				* Math.cos(Math.PI - _constraints._maxq[1]);
		if (r < c_2)
			return false;

		// In right circle
		double p_x = _l[0] * Math.cos(_constraints._minq[0]);
		double p_y = _l[0] * Math.sin(_constraints._minq[0]);
		r = (x - p_x) * (x - p_x) + (y - p_y) * (y - p_y);
		if (r < l1_2)
			return false;

		// In left circle
		p_x = _l[0] * Math.cos(_constraints._maxq[0]);
		p_y = _l[0] * Math.sin(_constraints._maxq[0]);
		r = (x - p_x) * (x - p_x) + (y - p_y) * (y - p_y);
		if (r < l1_2)
			return true;

		// In the range of (140:-30) degrees
		// Compute the angle in polar coordinate system
		double theta = Math.atan2(x, y);

		if (theta > _constraints._maxq[0] && theta < _constraints._minq[0])
			return false;

		// Else in the figure
		return true;
	}

}
