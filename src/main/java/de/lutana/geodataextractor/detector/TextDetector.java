package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.LocationCollection;

/**
 * Interface for different location information detection approaches based on text.
 * 
 * @author Matthias Mohr
 */
public interface TextDetector {
	
	/**
	 * Returns a list of detected locations for the specified text.
	 * 
	 * @param text
	 * @param locations
	 */
	public void detect(String text, LocationCollection locations);
	
}
