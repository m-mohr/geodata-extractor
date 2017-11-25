
package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.recognizer.cv.CvGraphic;

public interface GraphicDetector extends Detector {
	
	/**
	 * Tries to detect something from an image.
	 * 
	 * Returns a probability between 0 and 1. Other values need to be considered as invalid.
	 * 
	 * @param f
	 * @param g
	 * @return 
	 */
	public float detect(Figure f, CvGraphic g);
	
}
