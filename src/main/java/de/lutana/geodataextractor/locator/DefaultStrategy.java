package de.lutana.geodataextractor.locator;

import de.lutana.geodataextractor.detector.ClavinDetector;
import de.lutana.geodataextractor.detector.DumbCountryDetector;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import java.io.File;

/**
 * Default strategy to gather and combine location information.
 * 
 * @author Matthias Mohr
 */
public class DefaultStrategy implements Strategy {
	
	private DumbCountryDetector dumbDetector;
	private ClavinDetector clavinDetector;
	
	public DefaultStrategy() {
		this.dumbDetector = new DumbCountryDetector();
		this.clavinDetector = new ClavinDetector();
	}

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
		LocationCollection locations = new LocationCollection();

		this.getLocationsFromText(document.getTitle(), locations, 0.75);
		this.getLocationsFromText(document.getDescription(), locations, 0.5);

		FigureCollection figures = document.getFigures();
		for(Figure figure : figures) {
			this.getLocationsFromText(figure.getCaption(), locations, 0.9);
			this.getLocationsFromGraphic(figure.getGraphic(), locations, 1);

			Location location = locations.getMostLikelyLocation();
			figure.setLocation(location);
		}
		return true;
	}
	
	protected void getLocationsFromText(String text, LocationCollection locations, double weight) {
		locations.setWeight(weight);
		this.dumbDetector.detect(text, locations);
		this.clavinDetector.detect(text, locations);
		// ToDo: ...
		locations.resetWeight();
	}
	
	protected void getLocationsFromGraphic(File graphicFile, LocationCollection locations, double weight) {
		locations.setWeight(weight);
		// ToDo: ...
		locations.resetWeight();
	}
	
}
