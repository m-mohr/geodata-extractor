package de.lutana.geodataextractor.locator;

import de.lutana.geodataextractor.detector.ClavinTextDetector;
import de.lutana.geodataextractor.detector.DumbCountryTextDetector;
import de.lutana.geodataextractor.detector.CoordinateGraphicDetector;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.Graphic;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;

/**
 * Default strategy to gather and combine location information.
 * 
 * @author Matthias Mohr
 */
public class DefaultStrategy implements Strategy {
	
	private DumbCountryTextDetector dumbDetector;
	private ClavinTextDetector clavinDetector;
	private CoordinateGraphicDetector ocrDetector;
	
	public DefaultStrategy() {
		this.dumbDetector = new DumbCountryTextDetector();
		this.clavinDetector = new ClavinTextDetector();
		this.ocrDetector = new CoordinateGraphicDetector();
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
		LocationCollection globalLocations = new LocationCollection();

		this.getLocationsFromText(document.getTitle(), globalLocations, 0.75);
		this.getLocationsFromText(document.getDescription(), globalLocations, 0.5);

		FigureCollection figures = document.getFigures();
		for(Figure figure : figures) {
			LocationCollection figureLocations = new LocationCollection(globalLocations);
			this.getLocationsFromText(figure.getCaption(), figureLocations, 0.9);
			this.getLocationsFromGraphic(figure.getGraphic(), figureLocations, 1);

			// TODO: Improve this - for now we only add the location to the figure in case we detected something from the figure itself
			if (figureLocations.size() > globalLocations.size()) {
	//			Location location = figureLocations.getMostLikelyLocation();
				Location location = figureLocations.getLocation();
				figure.setLocation(location);
			}
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
	
	protected void getLocationsFromGraphic(Graphic graphic, LocationCollection locations, double weight) {
		locations.setWeight(weight);
		this.ocrDetector.detect(graphic, locations);
		// ToDo: ...
		locations.resetWeight();
	}
	
}
