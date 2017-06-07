package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.LocationCollection;

/**
 *
 * @author Matthias Mohr
 */
public interface Strategy {
	
	public LocationCollection execute(FigureCollection col);
	
}
