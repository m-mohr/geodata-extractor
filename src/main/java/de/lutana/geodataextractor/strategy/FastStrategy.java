package de.lutana.geodataextractor.strategy;

import de.lutana.geodataextractor.detector.DumbCountryTextDetector;
import de.lutana.geodataextractor.detector.CoordinateTextDetector;
import de.lutana.geodataextractor.detector.GeoNamesTextDetector;
import de.lutana.geodataextractor.detector.TextDetector;
import de.lutana.geodataextractor.detector.WorldMapDetector;
import de.lutana.geodataextractor.detector.cv.CvGraphic;
import de.lutana.geodataextractor.detector.gazetteer.LuceneIndex;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.recognizor.MapRecognizer;
import de.lutana.geodataextractor.recognizor.Recognizor;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fast strategy to gather and combine location information as quickly as possible.
 * 
 * Tries to find locations as fast as possible.
 * Therefore doesn't work much on the images.
 * Might return less or worse results than other strategies.
 * 
 * @author Matthias Mohr
 */
public class FastStrategy implements Strategy {
	
	private LuceneIndex geoNamesIndex;
	private Recognizor mapRecognizer;
	private TextDetector geonamesTextDetector;
	private final CoordinateTextDetector coordinateTextDetector;
	private final WorldMapDetector worldMapDetector;
	
	public FastStrategy() {
		Logger logger = LoggerFactory.getLogger(getClass());
		this.geoNamesIndex = new LuceneIndex();
		this.geoNamesIndex.load();
		this.mapRecognizer = new MapRecognizer(false);
		this.coordinateTextDetector = new CoordinateTextDetector();
		this.worldMapDetector = new WorldMapDetector();
		try {
			this.geonamesTextDetector = new GeoNamesTextDetector(this.geoNamesIndex);
		} catch (IOException | ClassNotFoundException ex) {
			logger.error("Loading GeoNamesTextDetector failed. Continuing with the DumbCountryTextDetector. " + ex.getMessage());
			this.geonamesTextDetector = new DumbCountryTextDetector();
		}
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
		LocationCollection globalLocations = new LocationCollection();

		LoggerFactory.getLogger(this.getClass()).info("## Document: " + document);
		this.getLocationsFromText(document.getTitle(), globalLocations, 0.5);
		this.getLocationsFromText(document.getDescription(), globalLocations, 0.5);
		// ToDo: We could analyse more texts from the document here using getText/setText, e.g. full text publication contents

		FigureCollection figures = document.getFigures();
		for(Figure figure : figures) {
			if (page != null && !figure.getPage().equals(page)) {
				continue;
			}
			CvGraphic cvGraphic = new CvGraphic(figure.getGraphic());
	
			Logger logger = LoggerFactory.getLogger(this.getClass());
			logger.info("# " + figure);
			
			// Detect whether it's a map or not
			float result = this.mapRecognizer.recognize(figure);
			boolean isMap = (result >= 0.5);
			logger.debug((isMap ? "Map detected" : "NOT a map") + " (" + result * 100 + "%)");

			if (isMap) {
				LocationCollection figureLocations = new LocationCollection(globalLocations);
				this.getLocationsFromText(figure.getCaption(), figureLocations, 0.75);
				this.worldMapDetector.detect(cvGraphic, figureLocations, 1);

				if (figureLocations.size() > globalLocations.size()) {
					Location location = figureLocations.getMostLikelyLocation();
					figure.setLocation(location);
				}
				else if (isMap && globalLocations.size() > 0) {
					Location location = globalLocations.getMostLikelyLocation();
					figure.setLocation(location);
				}
			}
			cvGraphic.dispose();
		}
		return true;
	}

	@Override
	public void shutdown() {
		if (this.geoNamesIndex != null) {
			this.geoNamesIndex.close();
		}
	}
	
	protected void getLocationsFromText(String text, LocationCollection locations, double weight) {
		if (text == null) {
			return;
		}
		this.coordinateTextDetector.detect(text, locations, weight);
		this.geonamesTextDetector.detect(text, locations, weight);
	}
	
}