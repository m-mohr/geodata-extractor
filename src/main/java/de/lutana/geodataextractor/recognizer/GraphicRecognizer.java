package de.lutana.geodataextractor.recognizer;

import de.lutana.geodataextractor.recognizer.cv.CvGraphic;
import de.lutana.geodataextractor.entity.LocationCollection;

/**
 * Interface for different location information recognition approaches based on graphics.
 * 
 * @author Matthias Mohr
 */
public interface GraphicRecognizer {
	
	/**
	 * Returns a list of detected locations for the specified graphic.
	 * 
	 * @param graphic
	 * @param locations
	 * @param weight
	 * @return true when a location has been found, false if not.
	 */
	public boolean recognize(CvGraphic graphic, LocationCollection locations, double weight);
	
}
