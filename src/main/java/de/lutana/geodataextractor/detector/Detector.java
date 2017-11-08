
package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Figure;

public interface Detector {
	
	/**
	 * Tries to detect whether the given figure shows a map or not.
	 * 
	 * Returns a probability between 0 and 1. Other values need to be considered as invalid.
	 * 
	 * @param f
	 * @return 
	 */
	public float detect(Figure f);
	
}
