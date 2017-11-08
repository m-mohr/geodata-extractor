package de.lutana.geodataextractor.recognizer;

import de.lutana.geodataextractor.entity.LocationCollection;

/**
 * Interface for different location information recognition approaches based on text.
 * 
 * @author Matthias Mohr
 */
public interface TextRecognizer {
	
	/**
	 * Returns a list of detected locations for the specified text.
	 * 
	 * @param text
	 * @param locations
	 * @param weight
	 * @return true when a location has been found, false if not.
	 */
	public boolean recognize(String text, LocationCollection locations, double weight);
	
}
