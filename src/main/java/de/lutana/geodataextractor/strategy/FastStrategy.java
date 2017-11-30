package de.lutana.geodataextractor.strategy;

import de.lutana.geodataextractor.recognizer.DumbCountryTextRecognizer;
import de.lutana.geodataextractor.recognizer.CoordinateTextRecognizer;
import de.lutana.geodataextractor.recognizer.GeoNamesTextRecognizer;
import de.lutana.geodataextractor.recognizer.WorldMapRecognizer;
import de.lutana.geodataextractor.recognizer.cv.CvGraphic;
import de.lutana.geodataextractor.recognizer.gazetteer.LuceneIndex;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.detector.MapDetector;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.lutana.geodataextractor.detector.GraphicDetector;
import de.lutana.geodataextractor.entity.locationresolver.HeatmapResolver;
import de.lutana.geodataextractor.entity.locationresolver.LocationResolver;
import de.lutana.geodataextractor.recognizer.TextRecognizer;

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
	private GraphicDetector mapRecognizer;
	private TextRecognizer geonamesTextDetector;
	private final CoordinateTextRecognizer coordinateTextDetector;
	private final WorldMapRecognizer worldMapDetector;
	
	public FastStrategy() {
		Logger logger = LoggerFactory.getLogger(getClass());
		this.geoNamesIndex = new LuceneIndex();
		this.geoNamesIndex.load();
		this.mapRecognizer = new MapDetector(false);
		this.coordinateTextDetector = new CoordinateTextRecognizer();
		this.worldMapDetector = new WorldMapRecognizer();
		try {
			this.geonamesTextDetector = new GeoNamesTextRecognizer(this.geoNamesIndex);
		} catch (IOException | ClassNotFoundException ex) {
			logger.error("Loading GeoNamesTextDetector failed. Continuing with the DumbCountryTextDetector. " + ex.getMessage());
			this.geonamesTextDetector = new DumbCountryTextRecognizer();
		}
	}
	
	public LocationResolver getLocationResolver() {
		return new HeatmapResolver();
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
			CvGraphic cvGraphic = new CvGraphic(figure);
	
			Logger logger = LoggerFactory.getLogger(this.getClass());
			logger.info("# " + figure);
			
			// Detect whether it's a map or not
			float result = this.mapRecognizer.detect(figure, cvGraphic);
			boolean isMap = (result >= 0.5);
			logger.debug((isMap ? "Map detected" : "NOT a map") + " (" + result * 100 + "%)");

			if (isMap) {
				LocationCollection figureLocations = new LocationCollection(globalLocations);
				this.getLocationsFromText(figure.getCaption(), figureLocations, 0.75);
				this.worldMapDetector.recognize(cvGraphic, figureLocations, 1);

				if (figureLocations.size() > globalLocations.size()) {
					Location location = figureLocations.resolveLocation(this.getLocationResolver());
					figure.setLocation(location);
				}
				else if (isMap && globalLocations.size() > 0) {
					Location location = globalLocations.resolveLocation(this.getLocationResolver());
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
		this.coordinateTextDetector.recognize(text, locations, weight);
		this.geonamesTextDetector.recognize(text, locations, weight);
	}
	
}