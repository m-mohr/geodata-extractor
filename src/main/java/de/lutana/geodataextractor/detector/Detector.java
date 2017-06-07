package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;

/**
 *
 * @author Matthias Mohr
 */
public interface Detector {
	
	public Location detect(Figure figure);
	
}
