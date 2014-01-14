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
public class ArmKaladjian extends ArmModel {
	// Pour toutes les matrices, ligne : 1:épaule, 2:coude, 3:bi
	//                             col : 1:flex, 2:ext
	
	/** Moment des muscles Mono et Bi, en tenant compte des signes (évite les (-1)^j)*/
	Matrix _hm = new Matrix( new double[][] {{-0.03, 0.04}, {-0.04, 0.02}});
	Matrix _hb = new Matrix( new double[][] {{-0.05, -0.04}, {0.05, 0.02}});
	/** A partir de _hm et _hb, pour simplifier les calculs */
	Matrix _H = new Matrix( 2, 6, 0.0);
	/** Coefficient liant section muscle à sa force */
	Matrix _ro = new Matrix( new double[][] {{6.8, 11, 3.6, 6, 2.1, 6.7}});
	
	
	/** entrée des neurones moteurs, 1x6 : 11, 12, 21, 22, 31, 32 */
	Matrix _E = new Matrix(1, 6, 0.0);
	/** activité des neurones moteurs 1x6 : 11, 12, 21, 22, 31, 32 */
	Matrix _G = new Matrix(1, 6, 0.0);
	/** activité des éléments contractant, dérivée, dérivée seconde */
	Matrix _N = new Matrix(1, 6, 0.0);
	Matrix _NDot = new Matrix(1, 6, 0.0);
	Matrix _NDot2 = new Matrix(1, 6, 0.0);
	/** Longueur des éléments séquentiels */
	Matrix _lSE = new Matrix(1, 6, 0.0);
	Matrix _lSEDot = new Matrix(1, 6, 0.0);
	/** Forces contractile */
	Matrix _FSE = new Matrix(1, 6, 0.0);
	/** Force passive */
	Matrix _FP = new Matrix(1, 6, 0.0);
	
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
	Matrix _thetaR = new Matrix( new double[][] {{Math.PI/4.0, Math.PI/2.0}});
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
	 * Pas de contraintes par défaut.
	 */
	public ArmKaladjian() {
		_l = new double[] {0.34, 0.46};
		
		_thetaMemory = new Vector<Matrix>();
		_thetaDotMemory = new Vector<Matrix>();
		
		// Update _H
		_H.set(0, 0, - _hm.get(0, 0)); _H.set(0, 1, _hm.get(0, 1));
		_H.set(1, 2, - _hm.get(1, 0)); _H.set(1, 3, _hm.get(1, 1));
		_H.set(0, 4, - _hb.get(0, 0)); _H.set(0, 5, _hb.get(0, 1));
		_H.set(1, 4, - _hb.get(1, 0)); _H.set(1, 5, _hb.get(1, 1));
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
	 * Calcule Eij, puis Gij à partir de theta et thetaDot à (t-_tauDel).
	 * 
	 * @param theta angles in 1x2 Matrix
	 * @param thetaDot speed in 1x2 Matrix
	 * @param lambda 1x6 Matrix
	 */
	void actNij( Matrix theta, Matrix thetaDot, Matrix lambda) {
		// Matrice des angles
		Matrix ang = theta.plus(thetaDot.times(_mu));
		System.out.println("ang="+JamaU.vecToString(ang));
		
		// _E
		_E = ang.times(_H);
		System.out.println("_E="+JamaU.vecToString(_E));
		
		Matrix comE = _E.minus(lambda);
		System.out.println("comE="+JamaU.vecToString(comE));
		
		// _G
		for( int col=0; col<_E.getColumnDimension(); col++) {
			if (comE.get(0, col) >= 0.0) {
				_G.set(0, col, _ro.get(0, col) * Math.exp(_alpha*(comE.get(0, col)) - 1.0 ));
			}
			else {
				_G.set(0, col, 0.0);
			}
		}
		System.out.println("_G="+JamaU.vecToString(_G));
		
		// Dérivée seconde de Nij
		_NDot2 = _G.minus(_N).minus(_NDot.times(2.0 * _tauN));
		_NDot2.timesEquals(1.0 / (_tauN*_tauN));
		// Dérivée première
		_NDot.plusEquals(_NDot2.times(_dt));
		// Activité
		_N.plusEquals(_NDot.times(_dt));
		System.out.println("_NDot2="+JamaU.vecToString(_NDot2));
		System.out.println("_NDot="+JamaU.vecToString(_NDot));
		System.out.println("_N="+JamaU.vecToString(_N));
	}
	/**
	 * Utilise le fait que FCE=FSE pour calculer, avec un schéma de Lagrange
	 * les valeurs des longueurs.
	 */
	void muscleLength(Matrix thetaDot) {
		Matrix passF = _lSE.copy();
		// [exp(beta*lSE)-1]+
		for (int col=0; col < passF.getColumnDimension(); col++ ) {
			double val = Math.exp(_beta * passF.get(0, col)) - 1.0;
			if (val >= 0.0) {
				passF.set(0, col, val);
			}
			else {
				passF.set(0, col, 0.0);
			}
		}
		System.out.println("passF="+JamaU.vecToString(passF));
		// 
		passF.arrayRightDivideEquals(_N);
		System.out.println("passF="+JamaU.vecToString(passF));
		passF.arrayTimesEquals(_ro);
		passF.minus( new Matrix(1, 6, _f1));
		passF.timesEquals(1.0/_f2);
		for (int col=0; col < passF.getColumnDimension(); col++ ) {
			passF.set(0,  col, ( (Math.tan( passF.get(0, col))- _f3) / _f4) );
			// 
		}

		System.out.println("lCEDot="+JamaU.vecToString(passF));
		// ajoute les influences des vitesses angulaires.
		// lDot
		_lSEDot = thetaDot.times(_H);
		_lSEDot.minusEquals(passF);
		
		// l
		_lSE.plusEquals(_lSEDot.times(_dt));
		System.out.println("_lSEDot="+JamaU.vecToString(_lSEDot));
		System.out.println("_lSE="+JamaU.vecToString(_lSE));
	}
	/**
	 * Calcule la force de contraction active à partir de _lSE.
	 */
	void activeForce(Matrix lSE) {
		for (int col=0; col < _FSE.getColumnDimension(); col++ ) {
			double val = Math.exp(_beta * _lSE.get(0, col)) - 1.0;
			if (val >= 0.0) {
				_FSE.set(0, col, val);
			}
			else {
				_FSE.set(0, col, 0.0);
			}
		}
	}
	/**
	 * Calcule la force passive à partir de theta et thetaR.
	 * @param angles des articulations
	 */
	public Matrix passiveForce(Matrix theta) {
		// Calcul des forces passives
		Matrix FP = new Matrix(1, 6, 0.0);
		Matrix ang = theta.minus(_thetaR);
		
		FP = ang.times(_H);
		FP = FP.arrayTimes(_ro).times(_kPE);
		// []+
		for( int col=0; col<FP.getColumnDimension(); col++) {
			double val = FP.get(0,  col);
			if (val > 0.0) {
				FP.set(0, col, val * _kPE * _ro.get(0, col));
			}
			else {
				FP.set(0, col, 0.0);
			}
		}
			
		return FP;
	}
	/**
	 * Mise à jour du bras.
	 */
	void armUpdate( double t ) {
		Matrix ang = getTheta( t - _dt);
		Matrix angDot = getThetaDot( t - _dt);
		computeI(ang);
		computeC(ang, angDot);
		
		_T = _H.times(_FSE.plus(_FP)).uminus();
		_TCT = _T.minus(_C.times(ang.transpose()));
		
		Matrix angDotDot = (_I.inverse()).times(_TCT.transpose());
		// Integration
		angDot.plusEquals( angDotDot.times(_dt) );
		ang.plusEquals( angDot.times(_dt) );
		
	}
	
	/**
	 * A partir d'angles donnés, trouve les Forces de contractions
	 * qui donne des couples nuls et tells que SUM(Fij) soit minimal.
	 * 
	 * @param theta Matrix 1x2
	 */
	public Matrix findContractionForce( Matrix theta ) {
		// Matrix avec row FP et row FA
		Matrix forceMat = new Matrix(2, 6, 0.0);
		
		// Calcul des forces passives and set as vec
		Matrix vFP = passiveForce(theta);
		System.out.println("vFP="+JamaU.matToString(vFP));
//		Matrix vFP = new Matrix( FP.getRowPackedCopy(), 1 /* 1 row */);
//		System.out.println("vFP="+JamaU.vecToString(vFP));
		
		// Matrice des contraintes A
		Matrix A = _H.uminus();
//		Matrix A = new Matrix(2, 6, 0.0);
//		A.set(0, 0, - _hm.get(0, 0));
//		A.set(0, 1, - _hm.get(0,  1));
//		A.set(0, 4, - _hb.get(0, 0));
//		A.set(0, 5, _hb.get(0, 1));
//		A.set(1, 2, - _hm.get(1, 0));
//		A.set(1, 3, - _hm.get(1,  1));
//		A.set(1, 4, _hb.get(1, 1));
//		A.set(1, 5, - _hb.get(1, 1));
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
	 * Compute Fa given Fa and Fp at minimum SUM(F),
	 * with a proportionnal coef of 'k'.
	 * => new.Fa = k x Fa + (k-1) x Fp
	 * 
	 * @param F Matrix 2x6 of {{Fa},{Fp}} (given by findContractionForce)
 	 * @param k proportionalite coef (>= 1).
	 * @return Matrix 1x6 of lambda, as a command
	 */
	public Matrix getLambdaFromForce(Matrix F, double k, Matrix theta) {
		System.out.println("F=\n"+JamaU.matToString(F));
		// active force
		Matrix Fp = F.getMatrix(0, 0, 0, F.getColumnDimension()-1);
		Matrix Fa = F.getMatrix(1, 1, 0, F.getColumnDimension()-1);
		
		Fa.timesEquals(k);
		Fa.plusEquals(Fp.times(k-1.0));
		System.out.println("Fa="+JamaU.vecToString(Fa));
		
		// compute [E-lamba]+
		for (int col = 0; col < Fa.getColumnDimension(); col++) {
			if (Fa.get(0, col) > 0.0) {
				Fa.set(0,  col,  (Math.log( (Fa.get(0, col)/_ro.get(0, col))+1.0  )) / _alpha);	
			}
			else {
				Fa.set(0,  col, 0.0);
			}
			
		}
		System.out.println("[E-lambda]+ ="+JamaU.vecToString(Fa));
		
		// Reflexe input
		// _E
		Matrix Esr = theta.times(_H);
		System.out.println("Esr="+JamaU.vecToString(Esr));
		
		// Command
		Matrix com = new Matrix(1, 6, 0.0);
		for (int col = 0; col < com.getColumnDimension(); col++) {
			double val = Fa.get(0, col);
			if (val >= 0) {
				com.set(0, col, Esr.get(0,  col) - val);
			}
			else {
				System.err.println("getLambdaFromForce : comment gérer quand c'est négatif ??");
				System.err.println("Esr="+JamaU.vecToString(Esr));
				System.exit(-1);
			}
		}
		System.out.println("lambda="+JamaU.vecToString(com));
		
		return com;
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
	
	public double[] getLength() {
		return _l;
	}
	
	
}
