/**
 * 
 */
package utils;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Récupère un fichier image et le transforme en ImageIcon.
 * Le fichier image est cherché dans : 
 * 
 * @author alain.dutech@loria.fr
 */
public class GraphicHelper {
	
	/** Default Color */
	final public Color [] _defColors = {Color.blue, Color.red, Color.green,
			Color.cyan, Color.magenta, Color.pink, Color.black };
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	public ImageIcon createImageIcon(String path, String description) {
	    java.net.URL imgURL = getClass().getResource(path);
	    if (imgURL != null) {
	    	ImageIcon img = new ImageIcon(imgURL, description);
	        return img;
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}
	
	/**
	 * Create SpinnerNumber and add to panel a JSpinner with this model.
	 * @param panel where to add the JSpinner
	 * @param value Initial value of JSpinner
	 * @param min min value of JSpinner
	 * @param max max value of JSpinner
	 * @param stepSize stepsize of JSpinner
	 * @param label Label that we be on the left of JSpinner
	 * @param maxFormat to compute size of JSpinner (ex "00.000").
	 * @return a SpinnerNumberModel attached to the JSPinner.
	 */
	public SpinnerNumberModel addJSpinNumber( JPanel panel,
			double value, double min, double max, double stepSize,
			String label, String maxFormat ) {
		SpinnerNumberModel mod = new SpinnerNumberModel(value, min, max, stepSize);
		JSpinner spin = new JSpinner(mod);
		((JSpinner.DefaultEditor)spin.getEditor()).getTextField().setColumns(maxFormat.length());
		JLabel l = new JLabel(label+" : ");
		panel.add(l);
		panel.add(spin);
		return mod;
	}

}
