/**
 * 
 */
package model;

import java.text.DecimalFormat;
import java.util.Vector;

import utils.JamaU;
import algo.Simplex;
import Jama.Matrix;

/**
 * D'après [Frolov00] : On the possibility of linear modelling the human arm
 * neuromuscular apparatus.
 * 
 * @author alain.dutech@loria.fr
 *
 */
public class ArmKaladjian {
	// Pour toutes les matrices, ligne : 1:épaule, 2:coude, 3:bi
	//                             col : 1:flex, 2:ext
	
	/** Moment des muscles Mono et Bi, en tenant compte des signes (évite les (-1)^j)*/
	Matrix _hm = new Matrix( new double[][] {{-0.03, 0.04}, {-0.04, 0.02}});
	Matrix _hb = new Matrix( new double[][] {{-0.05, -0.04}, {0.05, 0.02}});
	/** Coefficient liant section muscle à sa force */
	Matrix _ro = new Matrix( new double[][] {{6.8, 11}, {3.6, 6}, {2.1, 6.7}});
	
	/** entrée des neurones moteurs */
	Matrix _E = new Matrix(3, 2, 0.0);
	/** activité des neurones moteurs */
	Matrix _G = new Matrix(3, 2, 0.0);
	/** activité des éléments contractant, dérivée, dérivée seconde */
	Matrix _N = new Matrix(3, 2, 0.0);
	Matrix _NDot = new Matrix(3, 2, 0.0);
	Matrix _NDot2 = new Matrix(3, 2, 0.0);
	/** Longueur des éléments séquentiels */
	Matrix _lSE = new Matrix(3, 2, 0.0);
	Matrix _lSEDot = new Matrix(3, 2, 0.0);
	/** Forces contractile */
	Matrix _FSE = new Matrix(3, 2, 0.0);
	/** Force passive */
	Matrix _FP = new Matrix(3, 2, 0.0);
	
	/** Inertia Matrix */
	Matrix _I = new Matrix(2, 2, 0.0);
	/** Forces centrifuges et Coriolis */
	Matrix _C = new Matrix(2, 2, 0.0);
	/** Couple (Torque en anglais) */
	Matrix _T = new Matrix(1, 2, 0.0);
	/** Torque-C*_thetaDot */
	Matrix _TCT = new Matrix(1, 2, 0.0);
	
	
	/** Delai dans la boucle de réflexe */
	double _tauDel = 0.025;
	/** Délai causé par la cinétique du calcium */
	double _tauN = 0.015;
	/** coefficicent proporionalité vitesse angulaire */
	double _mu = 0.15;
	/** */
	double _alpha = 112.0;
	/** gain elastique ?? */
	double _beta = 100.0;
	/** coefficient de proportionnalité SE */
	double _kSE = 60.0;
	/** coefficient de proportionnalité PE */
	double _kPE = 17.3;
	/** Coeffs de la courbe logistique */
	double _f3 = 0.6;
	double _f4 = 20.0;
	double _f2 = 1.0 / (Math.PI/2.0 + Math.atan(_f3));
	double _f1 = Math.PI/2.0 * _f2;
	/** Angles de repos */
	double[] _thetaR = {Math.PI/4.0, Math.PI/2.0};
	/** Parametres anthropométriques */
	double _z1 = 0.062 + 0.082 + 1.65 * 0.34*0.34;
	double _z2 = 1.65 * 0.34 * 0.19;
	double _z3 = 0.082;

	
	/** Delta_t de la simulation */
	double _dt = 0.005;
	
	/** Memory of angles et vitesse agulaire*/
	Vector<Matrix> _thetaMemory;
	Vector<Matrix> _thetaDotMemory;
	Matrix _null1x2 = new Matrix(1, 2, 0.0);
	
	DecimalFormat df3 = new DecimalFormat( "000" );
	
	/**
	 * 
	 */
	public ArmKaladjian() {
		_thetaMemory = new Vector<Matrix>();
		_thetaDotMemory = new Vector<Matrix>();
	}
	
	/**
	 * Initialise le modèle : position, vitesse => rad ou rad/s.
	 */
	public void init( double angShoulder, double angElbow, double spdShoulder, double spdElbow) {
		Matrix ang = new Matrix( new double[][] {{ angShoulder, angElbow}});
		Matrix angDot = new Matrix( new double[][] {{ spdShoulder, spdElbow}});
		_thetaMemory.add(ang);
		_thetaDotMemory.add(angDot);
	}
	/**
	 * Calcule Eij, puis Gij.
	 * 
	 * @param lambda
	 * @param t
	 */
	void actNij( Matrix lambda, double t) {
		// Matrice des angles
		Matrix ang = getTheta(t-_tauDel).plus(getThetaDot(t-_tauDel).times(_mu));
		System.out.println("ang="+JamaU.vecToString(ang));
		
		// Mono
		for( int i=1; i<3; i++) {
			for( int j=1; j<3; j++) {
				_E.set(i-1, j-1, _hm.get(i-1,j-1) * ang.get(0,i-1));
			}
		}
		// Bi
		_E.set(2, 0, _hb.get(0,0)*ang.get(0,0) + _hb.get(0,1)*ang.get(0,1));
		_E.set(2, 1, _hb.get(1,0)*ang.get(0,0) + _hb.get(1,1)*ang.get(0,1));
		System.out.println("_E="+JamaU.matToString(_E));
		
		Matrix comE = _E.minus(lambda);
		System.out.println("comE="+JamaU.matToString(comE));
		
		for( int i=1; i<4; i++) {
			for( int j=1; j<3; j++) {
				if (comE.get(i-1, j-1) >= 0.0) {
					_G.set(i-1, j-1, _ro.get(i-1,j-1) * Math.exp(_alpha*(_E.get(i-1, j-1)) - 1.0 ));
				}
				else {
					_G.set(i-1, j-1, 0.0);
				}
			}
		}
		System.out.println("_G="+JamaU.matToString(_G));
		
		// Dérivée seconde de Nij
		_NDot2 = _G.minus(_N).minus(_NDot.times(2.0 * _tauN));
		_NDot2.timesEquals(1.0 / (_tauN*_tauN));
		// Dérivée première
		_NDot.plusEquals(_NDot2.times(_dt));
		// Activité
		_N.plusEquals(_NDot.times(_dt));
		System.out.println("_NDot2="+JamaU.matToString(_NDot2));
		System.out.println("_NDot="+JamaU.matToString(_NDot));
		System.out.println("_N="+JamaU.matToString(_N));
	}
	/**
	 * Utilise le fait que FCE=FSE pour calculer, avec un schéma de Lagrange
	 * les valeurs des longueurs.
	 */
	void muscleLength(double t) {
		Matrix passF = _lSE.copy();
		// [exp(beta*lSE)-1]+
		for( int i=0; i < passF.getRowDimension(); i++) {
			for (int j=0; j < passF.getColumnDimension(); j++ ) {
				double val = Math.exp(_beta * passF.get(i, j)) - 1.0;
				if (val >= 0.0) {
					passF.set(i, j, val);
				}
				else {
					passF.set(i, j, 0.0);
				}
			}
		}
		System.out.println("passF="+JamaU.matToString(passF));
		// 
		passF.arrayRightDivideEquals(_N);
		System.out.println("passF="+JamaU.matToString(passF));
		passF.arrayTimesEquals(_ro);
		passF.minus( new Matrix(3, 2, _f1));
		passF.timesEquals(1.0/_f2);
		for( int i=0; i < passF.getRowDimension(); i++) {
			for (int j=0; j < passF.getColumnDimension(); j++ ) {
				passF.set(i,  j, (Math.tan( passF.get(i, j)) / _f4) );
				// 
			}
		}
		System.out.println("lCEDot="+JamaU.matToString(passF));
		// ajoute les influences des vitesses angulaires.
		Matrix ang = getThetaDot(t);
		// lDot
		// Mono
		for( int i=1; i<3; i++) {
			for( int j=1; j<3; j++) {
				_lSEDot.set(i-1, j-1, _hm.get(i-1,j-1) * ang.get(0,i-1) - passF.get(i-1,j-1));
			}
		}
		// Bi
		_lSEDot.set(2, 0, _hb.get(0,0)*ang.get(0,0) + _hb.get(0,1)*ang.get(0,1) - passF.get(2,0));
		_lSEDot.set(2, 1, _hb.get(1,0)*ang.get(0,0) + _hb.get(1,1)*ang.get(0,1) - passF.get(2,1));
		// l
		_lSE.plusEquals(_lSEDot.times(_dt));
		System.out.println("_lSEDot="+JamaU.matToString(_lSEDot));
		System.out.println("_lSE="+JamaU.matToString(_lSE));
	}
	/**
	 * Calcule la force de contraction active à partir de _lSE.
	 */
	void activeForce(double t) {
		for( int i=0; i < _FSE.getRowDimension(); i++) {
			for (int j=0; j < _FSE.getColumnDimension(); j++ ) {
				double val = Math.exp(_beta * _lSE.get(i, j)) - 1.0;
				if (val >= 0.0) {
					_FSE.set(i, j, val);
				}
				else {
					_FSE.set(i, j, 0.0);
				}
			}
		}
	}
	/**
	 * Calcule la force passive à partir de theta et thetaR.
	 * @param angles des articulations
	 */
	public Matrix passiveForce(Matrix ang) {
		//Matrix ang = getTheta(t);
		// Calcul des forces passives
		Matrix FP = new Matrix(3, 2, 0.0);
		// Mono
		for( int i=1; i<3; i++) {
			for( int j=1; j<3; j++) {
				double val = _hm.get(i-1,j-1) * (ang.get(0,i-1) - _thetaR[i-1]);
				if (val >= 0.0) {
					FP.set(i-1, j-1, val * _kPE * _ro.get(i-1, j-1));
				}
				else {
					FP.set(i-1, j-1, 0.0);
				}
			}
		}
		// Bi
		double val = _hb.get(0,0)*(ang.get(0,0) - _thetaR[0]) + _hb.get(0,1)* (ang.get(0,1) - _thetaR[1]);
		if (val >= 0.0) {
			FP.set(2, 0, val * _kPE * _ro.get(2,0));
		}
		else {
			FP.set(2, 0, 0.0);
		}
		val = _hb.get(1,0)*(ang.get(0,0) - _thetaR[0]) + _hb.get(1,1)* (ang.get(0,1) - _thetaR[1]);
		if (val >= 0.0) {
			FP.set(2, 1, val * _kPE * _ro.get(2,1));
		}
		else {
			FP.set(2, 1, 0.0);
		}
		
		return FP;
	}
	/**
	 * Mise à jour du bras.
	 */
	void armUpdate( double t) {
		Matrix ang = getTheta( t - _dt);
		Matrix angDot = getThetaDot( t - _dt);
		computeI(ang);
		computeC(ang, angDot);
		
		// Valeur de Torque
		double t1 = -(_hm.get(0,0) * (_FSE.get(0, 0) + _FP.get(0, 0)) 
				- _hm.get(0, 1) * (_FSE.get(0,1) + _FP.get(0,1)) ) 
				- (_hb.get(0, 0) * (_FSE.get(2, 0) + _FP.get(2, 0)) 
						- _hb.get(0, 1) * (_FSE.get(2, 1) + _FP.get(2, 1)) );
		double t2 = -(_hm.get(1,0) * (_FSE.get(1, 0) + _FP.get(1, 0)) 
				- _hm.get(1, 1) * (_FSE.get(1,1) + _FP.get(1,1)) ) 
				- (_hb.get(1, 0) * (_FSE.get(2, 0) + _FP.get(2, 0)) 
						- _hb.get(1, 1) * (_FSE.get(2, 1) + _FP.get(2, 1)) );
		_T.set(0, 0, t1);
		_T.set(0, 1, t2);
		_TCT = _T.minus(_C.times(_T.transpose()));
		
		Matrix angDotDot = (_I.inverse()).times(_TCT.transpose());
		// Integration
		angDot.plusEquals( angDotDot.times(_dt) );
		ang.plusEquals( angDot.times(_dt) );
		
	}
	
	/**
	 * A partir d'angles donnés, trouve les Forces de contractions
	 * qui donne des couples nuls et tells que SUM(Fij) soit minimal.
	 * 
	 * @param ang Matrix 1x2
	 */
	public Matrix findContractionLevel( Matrix ang ) {
		// Matrix avec row FP et row FA
		Matrix forceMat = new Matrix(2, 6, 0.0);
		
		// Calcul des forces passives and set as vec
		Matrix FP = passiveForce(ang);
//		System.out.println("FP="+JamaU.matToString(FP));
		Matrix vFP = new Matrix( FP.getRowPackedCopy(), 1 /* 1 row */);
//		System.out.println("vFP="+JamaU.vecToString(vFP));
		
		// Matrice des contraintes A
		Matrix A = new Matrix(2, 6, 0.0);
		A.set(0, 0, - _hm.get(0, 0));
		A.set(0, 1, - _hm.get(0,  1));
		A.set(0, 4, - _hb.get(0, 0));
		A.set(0, 5, _hb.get(0, 1));
		A.set(1, 2, - _hm.get(1, 0));
		A.set(1, 3, - _hm.get(1,  1));
		A.set(1, 4, _hb.get(1, 1));
		A.set(1, 5, - _hb.get(1, 1));
//		System.out.println("A="+JamaU.matToString(A));
		
		// Matrice b
		Matrix b = A.times(vFP.transpose()).uminus();
//		System.out.println("b="+JamaU.matToString(b));
		
		// MAtrice c
		Matrix c = new Matrix(1, 6, 1.0);
		
		// Résolution par méthode du simplex
		// Il faut multiplier par -1 les lignes de A correspondant à des b négatifs.
		for (int row = 0; row < b.getRowDimension(); row++) {
			if (b.get(row, 0) < 0.0) {
				b.set(row, 0, - b.get(row,0));
				for (int col = 0; col < A.getColumnDimension(); col++) {
					A.set(row, col, - A.get(row, col));
				}
			}
		}
		Simplex simp = new Simplex( A, b, c);
		boolean hasSolution = simp.solveVerbeux();
		if (hasSolution) {
			Matrix vFA = simp.getFeasibleSolution();
			//double C = simp.getMinimum();
//			System.out.println("Sol vFA="+JamaU.vecToString(vFA));
//			System.out.println("Sol   C="+C);
			
			forceMat.setMatrix(0, 0, 0, 5, vFP);
			forceMat.setMatrix(1, 1, 0, 5, vFA);
			return forceMat;
		}
		else {
//			System.out.println("Sol NONE");
			forceMat.setMatrix(0, 0, 0, 5, vFP);
			// 2nd row reste nul
			return forceMat;
		}
	}
	
	/**
	 * Valeur de theta(index) à l'instant time. 
	 * @param index
	 * @param time
	 * @return 0.0 si time < 0;
	 */
	Matrix getTheta(double time) {
		if (time<0) {
			return _null1x2;
		}
		int tIndex = (int) Math.round(time/_dt);
		return _thetaMemory.get(tIndex);
	}
	/**
	 * Valeur de thetaDot(index) à l'instant time. 
	 * @param index
	 * @param time
	 * @return 0.0 si time < 0;
	 */
	Matrix getThetaDot(double time) {
		if (time<0) {
			return _null1x2;
		}
		int tIndex = (int) Math.round(time/_dt);
		return _thetaDotMemory.get(tIndex);
	}
	String dumpMem() {
		String str = "_thetaMem\n";
		for (int i = 0; i < _thetaMemory.size(); i++) {
			Matrix theta = _thetaMemory.get(i);
			Matrix thetaDot = _thetaDotMemory.get(i);
			str += "  "+df3.format(i)+" : "+JamaU.vecToString(theta)+" /  "+JamaU.vecToString(thetaDot)+"\n";
		}
		return str;
	}
	
	/**
	 * Calcule la valeur de la matrice d'Inertie.
	 * @param theta
	 */
	void computeI(Matrix theta) {
		_I.set(0, 0, _z1 + 2 * _z2 * Math.cos(theta.get(0,0)));
		_I.set(0, 1, _z3 + _z2 * Math.cos(theta.get(0,1)));
		_I.set(1, 0, _z3 + _z2 * Math.cos(theta.get(0,1)));
		_I.set(1, 1, _z3 );
	}
	/**
	 * Calcule la valeur de la matrice de Coriolis+Centrifuge
	 * @param theta
	 * @param thetaDot
	 */
	void computeC(Matrix theta, Matrix thetaDot) {
		_C.set(0, 0, - _z2 * Math.sin(theta.get(0,1)) * thetaDot.get(0, 1));
		_C.set(0, 1, - _z2 * Math.sin(theta.get(0, 1)) * (thetaDot.get(0,0) + thetaDot.get(0,1)));
		_C.set(1, 0, _z2 * Math.sin(theta.get(0,1)) * thetaDot.get(0, 0) );
		_C.set(1, 1, 0.0);
	}
	
	public static void main(String[] args) {
		ArmKaladjian arm = new ArmKaladjian();
		System.out.println(JamaU.matToString(arm._hm));
		System.out.println("_m[0,0]="+arm._hm.get(0, 0));
		
//		Matrix lambda = new Matrix( new double[][] {{0.0, 0.0}, {0.0, 0.0}, {0.0, 0.0}});
//		System.out.println("*** Init *****************************");
//		arm.init(Math.toRadians(25), Math.toRadians(35), 0.0, 0.0);
//		System.out.println(arm.dumpMem());
//		arm.actNij(lambda, 0.0);
//		arm.muscleLength( 0.0);
		
		// Test de la recherche de contraction pour position donnée
		Matrix theta = new Matrix( new double[][] {{Math.toRadians(25), Math.toRadians(35)}});
		Matrix F = arm.findContractionLevel(theta);
		
	}
	
	
}
