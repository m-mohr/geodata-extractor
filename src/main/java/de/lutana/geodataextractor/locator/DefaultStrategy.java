package de.lutana.geodataextractor.locator;

import de.lutana.geodataextractor.detector.ClavinTextDetector;
import de.lutana.geodataextractor.detector.DumbCountryTextDetector;
import de.lutana.geodataextractor.detector.CoordinateGraphicDetector;
import de.lutana.geodataextractor.detector.CoordinateTextDetector;
import de.lutana.geodataextractor.detector.cv.CvGraphic;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.Graphic;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.util.TensorFlowMapRecognizer;
import java.io.IOException;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default strategy to gather and combine location information.
 * 
 * @author Matthias Mohr
 */
public class DefaultStrategy implements Strategy {
	
	private TensorFlowMapRecognizer mapRecognizer;
	
	private DumbCountryTextDetector dumbCountryTextDetector;
	private ClavinTextDetector clavinTextDetector;
	private CoordinateGraphicDetector coordinateGraphicDetector;
	private CoordinateTextDetector coordinateTextDetector;
	
	public DefaultStrategy() {
		this.mapRecognizer = null;
		this.dumbCountryTextDetector = new DumbCountryTextDetector();
		this.clavinTextDetector = new ClavinTextDetector();
		this.coordinateGraphicDetector = new CoordinateGraphicDetector();
		this.coordinateTextDetector = new CoordinateTextDetector();
	}

	/**
	 * A strategy uses the parsed figures of a single file and detects locations
	 * for them with a certain strategy implemented here. It also combines all 
	 * locations collected to a single location collections.
	 * 
	 * If page is not null, only the selected page will be parsed and detected.
	 * 
	 * @param document
	 * @param page
	 * @return
	 */
	@Override
	public boolean execute(Document document, Integer page) {
		try {
			if (this.mapRecognizer == null) {
				this.mapRecognizer = TensorFlowMapRecognizer.getInstance();
			}
		} catch(URISyntaxException ex) {
			ex.printStackTrace();
			return false;
		}

		LocationCollection globalLocations = new LocationCollection();

		LoggerFactory.getLogger(this.getClass()).info("## Document: " + document);
		this.getLocationsFromText(document.getTitle(), globalLocations, 0.5);
		this.getLocationsFromText(document.getDescription(), globalLocations, 0.5);

		FigureCollection figures = document.getFigures();
		for(Figure figure : figures) {
			if (page != null && !figure.getPage().equals(page)) {
				continue;
			}
			Logger logger = LoggerFactory.getLogger(this.getClass());
			logger.info("# " + figure);
			
			boolean isMap = true;
			// Detect whether it's a map or not
			try {
				float result = this.mapRecognizer.recognize(figure.getGraphic());
				isMap = (result >= 0.4); // 0.1 (10%) tolerance
				logger.debug((isMap ? "Map detected" : "NOT a map") + " (" + result * 100 + "%)");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			LocationCollection figureLocations = new LocationCollection(globalLocations);
			this.getLocationsFromText(figure.getCaption(), figureLocations, 0.75);
			if (isMap) {
				this.getLocationsFromGraphic(figure.getGraphic(), figureLocations, 1);
			}

			// TODO: Improve this - for now we only add the location to the figure in case we detected something from the figure itself
			if (figureLocations.size() > globalLocations.size()) {
				Location location = figureLocations.getMostLikelyLocation();
				figure.setLocation(location);
			}
		}
		return true;
	}
	
	protected void getLocationsFromText(String text, LocationCollection locations, double weight) {
		locations.setWeight(weight);

		this.dumbCountryTextDetector.detect(text, locations);
		this.clavinTextDetector.detect(text, locations);
		this.coordinateTextDetector.detect(text, locations);
		// ToDo: ...

		locations.resetWeight();
	}
	
	protected void getLocationsFromGraphic(Graphic graphic, LocationCollection locations, double weight) {
		locations.setWeight(weight);
		CvGraphic cvGraphic = new CvGraphic(graphic);

		this.coordinateGraphicDetector.detect(cvGraphic, locations);
		// ToDo: ...
		
		cvGraphic.dispose();
		locations.resetWeight();
	}
	
}
