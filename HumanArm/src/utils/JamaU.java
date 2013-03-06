package utils;

import java.text.DecimalFormat;

import javax.vecmath.Point3d;

import Jama.Matrix;

public class JamaU {
	
	/** Decimal formating */
	static DecimalFormat df5_3 = new DecimalFormat( "00.000" );
	
	/** 
	 * Compute the dot product. Suppose that Matrix are vector (column or row)
	 * and of the same dimension.
	 * @return sum(m1.*m2)
	 */
	static public double dotP( Matrix m1, Matrix m2 ) {
		double scalarProduct = 0;
		if (m1.getColumnDimension() == 1) {
			for (int row = 0; row < m1.getRowDimension(); row++) {
				scalarProduct += m1.get(row,  0) * m2.get(row, 0);
			}
		}
		else {
			for (int col = 0; col < m1.getColumnDimension(); col++) {
				scalarProduct += m1.get(0, col) * m2.get(0, col);
			}
		}
		return scalarProduct;
	}

	/**
	 * Change a Point3d in Matrix
	 *
	 * @param p the Point3d to change
	 * @return The Matrix of the Point3d p
	 */
	public static Matrix Point3dToMatrix(Point3d p) {
		Matrix m = new Matrix(1, 3);
		m.set(0, 0, p.x);
		m.set(0, 1, p.y);
		m.set(0, 2, p.z);
		return m;
	}

	/**
	 * Nice String from a vector (as a Matrix(1xn)).
	 */
	static public String vecToString( Matrix vec ) {
		String str = "";
		if (vec.getRowDimension() > 0 ) {
			str += "[";
			for (int i = 0; i < vec.getColumnDimension(); i++) {
				str += df5_3.format(vec.get(0, i))+"; ";
			}
			str += "]";
		}
		return str;
	}
	/**
	 * Nice String from a Matrix.
	 */
	static public String matToString( Matrix mat ) {
		String str = "";
		for (int i = 0; i < mat.getRowDimension(); i++) {
			str += "[";
			for (int j = 0; j < mat.getColumnDimension(); j++) {
				str += df5_3.format(mat.get(i, j))+"; ";
			}
			str += "]\n";
		}
		return str;
	}
}
