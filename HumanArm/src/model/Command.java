/**
 * 
 */
package model;

import java.text.DecimalFormat;

/**
 * Stocke un instant (double time) et une consigne (double val).
 * 
 * @author Alain.Dutech@loria.fr
 *
 */
public class Command implements Comparable<Command> {
	public double time;
	public double val;
	
	/** Decimal formating */
	static DecimalFormat df5_3 = new DecimalFormat( "00.000" );
	
	public Command() {
		this.time = 0.0;
		this.val = 0.0;
	}
	public Command(double time, double val) {
		super();
		this.time = time;
		this.val = val;
	}
	/**
	 * Une sortie formatée 
	 */
	@Override
	public String toString() {
		return df5_3.format(time)+": "+df5_3.format(val);
	}
	public String toStringP() {
		return "("+toString()+")";
	}

	@Override
	public int compareTo(Command c) {
        return Double.compare(this.time, c.time);
	}
	
}
