package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.LocationCollection;

/**
 * Default strategy to gather and combine location information.
 * 
 * @author Matthias Mohr
 */
public class DefaultStrategy implements Strategy {

	/**
	 * A strategy uses the parsed figures of a single file and detects locations
	 * for them with a certain strategy implemented here. It also combines all 
	 * locations collected to a single location collections.
	 * 
	 * @param figures
	 * @return
	 */
	@Override
	public LocationCollection execute(FigureCollection figures) {
		// ToDo: Dumb detector for testing only
		LocationCollection locations = new LocationCollection();
		DumbCountryDetector dumb = new DumbCountryDetector();
		for(Figure figure : figures) {
			LocationCollection collection = dumb.detect(figure);
			if (collection != null) {
				locations.addAll(collection);
			}
		}
		return locations;
	}
	
}
