package model;

import Jama.Matrix;
//import utils.JamaU;


/**
 * Simule les muscles du bras.
 * 
 * Chaque muscle est un muscle "simple" qui est modélisé comme étant attaché aux
 * os par le biais d'une poulie. Sa longueur est donc directement proportionnelle
 * à la variation des angles (marche pour les mono- ou bi-articulaires).
 * 
 * Pour chaque muscle, il faut spécifier :
 *  - les moments d'action sur chaque articulation (_mom, en m)
 *  - son intervalle de variation de longueur normalisée (_minL0 et _maxL0, sans unité)
 * 
 * Pour chaque articulation, il faut spécifier :
 *  - son intervalle de variation d'angle (_minA, _maxA, en radians)
 * 
* @author Alain.Dutech@loria.fr
*
*/
public class SimpleMuscle {
	
	/** nb_muscle x nb_angles Matrix of moment */
	double [][] inMom = {{ 0.00, 0.04},{ 0.00, -0.04},{ 0.025, 0.00},{ -0.025, 0.00},
			{0.028,0.028},{-0.035,-0.035}};
	Matrix _mom = new Matrix(inMom);
	/** 1 x nb_muscle : length of muscle at min angles. */
	double [][] inMinL0 = {{1.2, 0.8, 1.2, 0.7, 1.1, 0.85}};
	Matrix _minL0 = new Matrix(inMinL0);
	/** 1 x nb_muscle : length of muscle at max angles. */
	double [][] inMaxL0 = {{0.6, 1.25, 0.7, 1.1, 0.6, 1.2}};
	Matrix _maxL0 = new Matrix(inMaxL0);
	/** 1 x nb_muscle cross section area in cm2 */
	double [][] inSec = {{18,14,22,12,5,10}};
	Matrix _sec = new Matrix(inSec);
	/** 1 x nb_muscle : ratio to go from length to normalized length */
	Matrix _k;
	/** 1 x nb_angle : min angle of joints */
	double [][] inMinA = {{Math.toRadians(-30), Math.toRadians(0)}};
	Matrix _minA = new Matrix(inMinA);
	/** 1 x nb_angle : max angle of joints */
	double [][] inMaxA = {{Math.toRadians(140), Math.toRadians(160)}};
	Matrix _maxA = new Matrix(inMaxA);
	
	
	/** 1 x nb_muscle : Muscle length normalized */
	private Matrix _ln;
	/** 1 x nb_muscle : Muscle speed normalized */
	private Matrix _vn;
	/** 1 x nb_muscle : Muscle tension normalized*/
	private Matrix _tn;
	/** 1 x nb_muscle : Muscle tension */
	private Matrix _t;
	/** 1 x nb_joint : Torque at each joint */
	private Matrix _cpl;
	
	
	/**
	 * Creation of one muscle.
	 * @param moment nb_segment lines, 3 col : On each line, the influence on the given joint
	 * as in moment[i,0]+moment[i,1]*sin(moment[i,2]*angle)
	 * @param minL0 minimum size of muscle
	 * @param maxL0 maximum size of muscle
	 */
	public SimpleMuscle() {
		// Init
		_ln = _minL0.copy();
		_vn = new Matrix(1,_ln.getColumnDimension(), 0);
		_tn = new Matrix(1,_ln.getColumnDimension(), 0);
		_t = new Matrix(1,_ln.getColumnDimension(), 0);
		_cpl = new Matrix(1,_minA.getColumnDimension(), 0);
		
		// ratio l/l0=k
		//_k = (_maxL0-_minL0) / JamaU.dotP(_mom, _maxA.minus(_minA));
		_k = (_maxL0.minus(_minL0)).arrayRightDivide(
				(_maxA.minus(_minA)).times( _mom.transpose() ) );
		_k = _k.uminus();
	}
	
	/**
	 * Given an activation 'act', joints angles and speed, compute the tension
	 * applied on every joint.
	 * @param act in [0,1]
	 * @param angles (rad)
	 * @param angSpeed (rad/s)
	 * @return Torque (1xnb_joint) for each joint.
	 */
	public Matrix computeTorque( Matrix act, Matrix angles, Matrix angSpeed ) {
		
		// longueur du muscle
		_ln = computeLength(angles);
		// vitesse du muscle
		_vn = computeSpeed(angles, angSpeed);
		// tension (force)
		for (int col = 0; col < _tn.getColumnDimension(); col++) {
			double t = funA(act.get(0,col),_ln.get(0,col))*
				(funFl(_ln.get(0,col))*funFv(_ln.get(0,col),_vn.get(0,col))
				+funFp(_ln.get(0,col)));
			_tn.set(0, col, t);
		}
		// Real tension
		_t = _tn.arrayTimes(_sec).times(31.8);
		// Torque
		_cpl = _t.times(_mom);
		
		return _cpl;
	}
	
	/** 
	 * Assume une relation linéaire entre les angles et la longueur,
	 * les moments faisant office de "rayons". En normalisant, on peut oublier ce moment.
	 * 
	 * @param angles (in rad)
	 * @return normalized length of muscle.
	 */
	public Matrix computeLength( Matrix angles ) {
		Matrix l = _minL0.minus( _k.arrayTimes( (angles.minus(_minA)).times( _mom.transpose()) ));
		return l;
	}
	/**
	 * Assume une relation linéaire entre les angles et la longueur,
	 * les moments faisant office de "rayons". En normalisant, on peut oublier ce moment.
	 * Ici, les angles sont inutiles...
	 * @param angles (in rad)
	 * @param angSpeed (in rad/s)
	 * @return normalized speed on muscle.
	 */
	public Matrix computeSpeed( Matrix angles, Matrix angSpeed ) {
		Matrix v = _k.arrayTimes( angSpeed.times( _mom.transpose()  ));
		return v;
	}
	
	
	static public double funA(double act, double len) {
		double nf = 2.11 + 4.16 * (1/len -1);
				
		return 1 - Math.exp(-Math.pow((act/0.56/nf), nf));
	}
	static public double funFl(double len) {
		return Math.exp(-Math.pow(Math.abs((Math.pow(len, 1.93)-1)/1.03), 1.87));
	}
	static public double funFv(double len, double spd) {
		if (spd<=0) {
			return (-5.72 - spd)/(-5.72 + spd * (1.38 + 2.09 * len));
		}
		else {
			return (0.62 - spd * (-3.12+4.21 * len - 2.67 * len * len))/(0.62 + spd);
		}
	}
	static public double funFp( double len ) {
		return -0.02 * Math.exp(13.8 - 18.7 * len);
	}

	/**
	 * Normalized length of the Muscles
	 * @return _ln
	 */
	public Matrix getLengthN() {
		return _ln;
	}
	/**
	 * Normalized speed of the Muscles.
	 * @return _vn
	 */
	public Matrix getSpeedN() {
		return _vn;
	}
	/**
	 * Normalized Tension of the Muscles
	 * @return _tn
	 */
	public Matrix getTensionN() {
		return _tn;
	}
	/**
	 * Real Tension of the Muscles
	 * @return _t
	 */
	public Matrix getTension() {
		return _t;
	}
	/**
	 * Torque on the joints.
	 * @return _cpl
	 */
	public Matrix getTorque() {
		return _cpl;
	}
	
}
