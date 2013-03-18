package model;

/**
 * Constraints to apply on the arm. Composed by angular bounds of acceleration,
 * speed and angle. These constraints can be dynamics.
 * 
 * TODO: use encapsulation ?
 * 
 * @author moinel
 */
public class ArmConstraints {

	double _mind2q = -128. * Math.PI; double _maxd2q = 128 * Math.PI;
	double _mindq = -8. * Math.PI;    double _maxdq = 8 * Math.PI;
	double[] _minq = {Math.toRadians(-30), Math.toRadians(0)};
	double[] _maxq = {Math.toRadians(140),   Math.toRadians(160)};
}
