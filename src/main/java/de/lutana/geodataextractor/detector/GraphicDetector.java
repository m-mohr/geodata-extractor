package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Graphic;
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
	 */
	public void detect(Graphic graphic, LocationCollection locations);
	
}
