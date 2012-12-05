/**
 * 
 */
package utils;

import javax.swing.ImageIcon;

/**
 * Récupère un fichier image et le transforme en ImageIcon.
 * Le fichier image est cherché dans : 
 * 
 * @author alain.dutech@loria.fr
 */
public class IconLoader {
	
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

}
