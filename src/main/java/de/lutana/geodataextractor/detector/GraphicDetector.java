package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.detector.cv.CvGraphic;
import de.lutana.geodataextractor.entity.LocationCollection;

/**
 * Interface for different location information detection approaches based on graphics.
 * 
 * @author Matthias Mohr
 */
public interface GraphicDetector {
	
	/**
	 * Returns a list of detected locations for the specified graphic.
	 * 
	 * @param graphic
	 * @param locations
	 * @param weight
	 * @return true when a location has been found, false if not.
	 */
	public boolean detect(CvGraphic graphic, LocationCollection locations, double weight);
	
}
