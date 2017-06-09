package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.LocationCollection;

/**
 * A strategy combines the detectors and the resulting location collections.
 * 
 * @author Matthias Mohr
 */
public interface Strategy {
	
	/**
	 * A strategy uses the parsed figures of a single file and detects locations
	 * for them with a certain strategy implemented here. It also combines all 
	 * locations collected to a single location collections.
	 * 
	 * @param figures
	 * @return
	 */
	public LocationCollection execute(FigureCollection figures);
	
}
