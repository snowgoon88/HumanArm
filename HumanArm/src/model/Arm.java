/**
 * 
 */
package model;

import java.util.ArrayList;

import javax.vecmath.Point3d;
import Jama.Matrix;
import utils.JamaU;


/**
 * 'Arm' implémente le modèle de bras à 2 dimensions de [Li, 06], réutilisé dans [Kaladjian, 98]
 * mais aussi [Marin, 11].
 * 
 * Le bras est composé de _dimQ=2 segments et son état est décrit par :
 *  - _q (jama.Matrix de 1 row et _dimQ colonnes) :   angles des segments (rad)
 *  - _dq (jama.Matrix de 1 row et _dimQ colonnes) :  vitesse angulaire des segments (rad/s)
 *  - _d2q (jama.Matrix de 1 row et _dimQ colonnes) : accélération angulaire des segments (rad/s/s)
 *  
 * Entrée : la commande est un vecteur indiquant les moments appliqués aux articulations pour
 * un temps donné. Cela se fait par le biais de la méthode 'applyTension( Matrix tau, double dt )'.
 *  - _tau (jama.Matrix de 1 row et _dimQ colonnes) : moments en N.m.
 * 
 * Sortie : l'état du bras.
 * 
 * De plus, Arm prévient ses Listeners des changements pour qu'ils se mettent à jour.
 * 
 * @author Alain.Dutech@loria.fr
 */
public class Arm extends Model<ArmModelListener> {
	
	/** Dimension of the state space */
	static int _dimQ = 2;
	/** Position : 2 angles in rad */
	Matrix _q = new Matrix(1, _dimQ);
	/** Speed : 2 angular speed in rad/s */
	Matrix _dq = new Matrix(1, _dimQ);
	/** Accel : 2 angular accel in rad/s/s */
	Matrix _d2q = new Matrix(1, _dimQ);
	/** Tension : 2 moments in N.m */
	Matrix _tau = new Matrix(1, _dimQ);
	
	/** In euclidian space */
	ArrayList<Point3d> _pos = new ArrayList<Point3d>();
	double [] _posX = new double[_dimQ+1];
	double [] _posY = new double[_dimQ+1];

	/** Inertia Matrix */
	Matrix _M = new Matrix(_dimQ,_dimQ);
	Matrix _Minv;
	/** Coriolis and centripete */
	Matrix _C = new Matrix(_dimQ, 1);
	
	
	/** Inertia moment : kg*m*m */
	double[] _I = {0.025, 0.045 };
	/** Masses : kg */
	double[] _m = {1.4, 1.1};
	/** Length : m */
	double[] _l = {0.30, 0.35};
	/** Distance to center of mass : m */
	double[] _s = {0.11, 0.16};
	/** Friction */
	double [][] inB = {{0.05, 0.025}, {0.025, 0.05}};
	Matrix _B = new Matrix(inB);
	
	/** Bound for values */
	boolean _fg_bounded = true;
	ArmConstraints _constraints = new ArmConstraints();
	
	/**
	 * Constructor with default initialization. 
	 */
	public Arm() {
		super();
		// Default arm position;
		_q = _q.times(0.0);
		_dq = _dq.times(0.0);
		_d2q = _d2q.times(0.0);
		_tau = _tau.times(0.0);
		updateModel();
		// and update the euclidian positions.
		for (int i = 0; i < _dimQ+1; i++) {
			_pos.add(new Point3d());
		}
		updateEuclidianPosition();
		
	}
	
	private void updateEuclidianPosition() {
		// initial angle is 0
		double angle = 0;
		// first point in Arm is at center
		_pos.get(0).set(0, 0, 0);
		// for each segment
		for (int indPos = 1; indPos < _pos.size(); indPos++) {
			Point3d segment = _pos.get(indPos);
			// add new angles
			angle += _q.get(0, indPos-1);
			segment.set( Math.cos(angle), Math.sin(angle), 0.0);
			// take length of segment into account
			segment.scale(_l[indPos-1]);
			// add to extremity of last segment
			segment.add(_pos.get(indPos-1));
		}
	}
	private void update2DPosition() {
		// initial angle is 0
		double angle = 0;
		double posX = 0.0;
		double posY = 0.0;
		// first point in Arm is at center
		_posX[0] = posX;
		_posY[0] = posY;
		// for each segment
		for (int i = 1; i < _posX.length; i++) {
			// add new angles
			angle += _q.get(0, i-1);
			_posX[i] = _posX[i-1] + _l[i-1] * Math.cos(angle);
			_posY[i] = _posY[i-1] + _l[i-1] * Math.sin(angle);
		}
	}
	
	/**
	 * Update _M and _C according to _q and _dq.
	 */
	private void updateModel() {
		double d1 = _I[0] + _I[1] + _m[1] * _l[0] * _l[0];
		double d2 = _m[1] * _l[0] * _s[1];
		double d3 = _I[1];
		
		// Inertia
		_M.set(0, 0, d1+2.0*d2*Math.cos(_q.get(0,1)));
		_M.set(0, 1, d3 + d2 * Math.cos(_q.get(0,1)));
		_M.set(1, 0, d3 + d2 * Math.cos(_q.get(0,1)));
		_M.set(1, 1, d3);
		_Minv = _M.inverse();
		
		// Coriolis
		_C.set(0, 0, -_dq.get(0,1) * (2.0 * _dq.get(0,0) + _dq.get(0,1)));
		_C.set(1, 0, _dq.get(0,0) * _dq.get(0,0));
		_C = _C.times( d2 * Math.sin(_q.get(0,1)) );		
	}
	
	public void updateTension() {
		_tau = _M.times(_d2q.transpose());
		_tau = _tau.plus(_C);
		_tau = _tau.plus( _B.times(_dq.transpose()));
	}
	/**
	 * Apply a moment to the arm.
	 * @param tau Matrix(1,_dimQ) of moments.
	 * @param dt delta_t in seconds
	 */
	public void applyTension( Matrix tau, double dt ) {
		_tau = tau;
		// Make sure the model is correct
		updateModel();
		// Compute acceleration 
		Matrix accel = tau.transpose();
		accel = accel.minus(_C);
		accel = accel.minus( _B.times(_dq.transpose()));
		_d2q = _Minv.times(accel);
		_d2q = _d2q.transpose();
		if (_fg_bounded) {
			for (int i = 0; i < _d2q.getColumnDimension(); i++) {
				_d2q.set(0, i, Math.min(Math.max(_d2q.get(0, i), _constraints._mind2q), _constraints._maxd2q));
			}
		}
		// then update speed
		_dq = _dq.plus(_d2q.times(dt));
		if (_fg_bounded) {
			for (int i = 0; i < _dq.getColumnDimension(); i++) {
				_dq.set(0, i, Math.min(Math.max(_dq.get(0, i), _constraints._mindq), _constraints._maxdq));
			}
		}
		// then position
		_q = _q.plus(_dq.times(dt));
		if (_fg_bounded) {
			for (int i = 0; i < _q.getColumnDimension(); i++) {
				_q.set(0, i, Math.min(Math.max(_q.get(0, i), _constraints._minq[i]), _constraints._maxq[i]));
			}
		}
		
		// then update euclidian position
		updateEuclidianPosition();
		update2DPosition();
		notifyModelListeners();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = "";
		str += "Tau="+JamaU.vecToString(_tau)+" => ";
		// state
		str += "Pos="+JamaU.vecToString(_q)+"  Spd="+JamaU.vecToString(_dq)+"  Acc="+JamaU.vecToString(_d2q);
		// euclidian
		for (Point3d pt : _pos) {
			str += "\n segment at "+pt.toString();
		}
		
		return str;
	}

	/**
	 * Is the Arm bounded in speed, accel, position, torque...
	 * @return the _fg_bounded
	 */
	public boolean isBounded() {
		return _fg_bounded;
	}
	/**
	 * Set if the Arm is bounded in speed, accel, position, torque...
	 * @param bounded 
	 */
	public void setBounded(boolean bounded) {
		this._fg_bounded = bounded;
	}

	/**
	 * Get the vector of the arm angles.
	 * @return _q as a Jama.Matrix 
	 */
	public Matrix getArmPos() {
		return _q;
	}
	/**
	 * Set the vector of arm angles, using a Jama.Matrix.
	 * @param q, Jama.Matrix, of the right dimensions
	 */
	public void setArmPos(Matrix q) {
		if( q.getColumnDimension() == _q.getColumnDimension() && 
				q.getRowDimension() == _q.getRowDimension()) {
			this._q = q;
			updateEuclidianPosition();
			update2DPosition();
			notifyModelListeners();
		}
	}
	/**
	 * Set the vector of arm angles, using an array of doubles.
	 * @param an array of double with the right length
	 */
	public void setArmPos(double[] vecq) {
		if (vecq.length == _q.getColumnDimension()) {
			this._q = new Matrix(vecq, 1);
			updateEuclidianPosition();
			update2DPosition();
			notifyModelListeners();
		}
	}

	/**
	 * Get the vector of the arm angular speed.
	 * @return _dq as a jama.Matrix
	 */
	public Matrix getArmSpeed() {
		return _dq;
	}
	/**
	 * Set the vector of arm angular speed, using a Jama.Matrix.
	 * @param dq, jama.Matrix of the right dimensions
	 */
	public void setArmSpeed(Matrix dq) {
		if( dq.getColumnDimension() == _dq.getColumnDimension() && 
				dq.getRowDimension() == _dq.getRowDimension()) {
			this._dq = dq;
			notifyModelListeners();
		}
	}
	/**
	 * Set the vector of arm angular speed, using an array of doubles.
	 * @param an array of double with the right length
	 */
	public void setArmSpeed(double[] vecdq) {
		if (vecdq.length == _q.getColumnDimension()) {
			this._dq = new Matrix(vecdq, 1);
			notifyModelListeners();
		}
	}

	/**
	 * Get the position of base and segment endpoints of Arm.
	 * @return _pos
	 */
	public ArrayList<Point3d> getArmPoints() {
		return _pos;
	}
	/**
	 * Get the position the last endpoint of Arm.
	 * @return _pos[-1] (en python :o) )
	 */
	public Point3d getArmEndPoint() {
		return _pos.get(_pos.size()-1);
	}
	
	/**
	 * Get the position the last endpoint of Arm.
	 * @return Matrix of the last endpoint arm position 
	 */
	public Matrix getArmEndPointMatrix() {
		return JamaU.Point3dToMatrix(getArmEndPoint());
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
	 * Get the x-position the last endpoint of Arm.
	 * @return _posX[-1] (en python :o) )
	 */
	public double getArmEndPointX() {
		return _posX[_posX.length-1];
	}
	/**
	 * Get the y-position the last endpoint of Arm.
	 * @return _posY[-1] (en python :o) )
	 */
	public double getArmEndPointY() {
		return _posY[_posY.length-1];
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

	public double[] getLength() {
		return _l;
	}

}
