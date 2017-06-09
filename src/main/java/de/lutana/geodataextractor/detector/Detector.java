package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.LocationCollection;

/**
 * Interface for different location information detection approaches.
 * 
 * @author Matthias Mohr
 */
public interface Detector {
	
	/**
	 * Returns a list of detected locations for the specified figure.
	 * 
	 * @param figure
	 * @return 
	 */
	public LocationCollection detect(Figure figure);
	
}
