package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.Location;
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
	 * @param document
	 * @return
	 */
	@Override
	public boolean execute(Document document) {
		// ToDo: Dumb detector for testing only
		DumbCountryDetector dumb = new DumbCountryDetector();
		FigureCollection figures = document.getFigures();
		for(Figure figure : figures) {
			LocationCollection collection = dumb.detect(figure);
			// ToDo: Union strategy for testing only
			Location location = collection.union();
			figure.setLocation(location);
		}
		return true;
	}
	
}
