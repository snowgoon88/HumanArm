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

	public double _mind2q = -128. * Math.PI;
	public double _maxd2q = 128. * Math.PI;
	public double _mindq = -8. * Math.PI;
	public double _maxdq = 8. * Math.PI;
	public double[] _minq = { Math.toRadians(-30), Math.toRadians(0) };
	public double[] _maxq = { Math.toRadians(140), Math.toRadians(160) };
}
