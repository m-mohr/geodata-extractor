package de.lutana.geodataextractor.strategy;

import de.lutana.geodataextractor.recognizer.DumbCountryTextRecognizer;
import de.lutana.geodataextractor.recognizer.CoordinateGraphicRecognizer;
import de.lutana.geodataextractor.recognizer.CoordinateTextRecognizer;
import de.lutana.geodataextractor.recognizer.GeoNamesGraphicRecognizer;
import de.lutana.geodataextractor.recognizer.GeoNamesTextRecognizer;
import de.lutana.geodataextractor.recognizer.WorldMapRecognizer;
import de.lutana.geodataextractor.recognizer.cv.CvGraphic;
import de.lutana.geodataextractor.recognizer.gazetteer.LuceneIndex;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.detector.MapDetector;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import de.lutana.geodataextractor.detector.GraphicDetector;
import de.lutana.geodataextractor.entity.locationresolver.JackardIndexResolver;
import de.lutana.geodataextractor.entity.locationresolver.LocationResolver;
import de.lutana.geodataextractor.recognizer.TextRecognizer;

/**
 * Default strategy to gather and combine location information.
 * 
 * Tries to find a sweet spot to deliver the best results, without thinking about speed.
 * 
 * @author Matthias Mohr
 */
public class DefaultStrategy extends AbstractStrategy {
	
	private LuceneIndex geoNamesIndex;
	protected GraphicDetector mapRecognizer;
	protected TextRecognizer geonamesTextDetector;
	protected GeoNamesGraphicRecognizer geonamesGraphicDetector;
	protected final CoordinateGraphicRecognizer coordinateGraphicDetector;
	protected final CoordinateTextRecognizer coordinateTextDetector;
	protected final WorldMapRecognizer worldMapDetector;
	
	public DefaultStrategy() {
		this(null);
	}
	
	public DefaultStrategy(LocationResolver locationResolver) {
		this(locationResolver, null);
	}
	
	public DefaultStrategy(LocationResolver locationResolver, LuceneIndex geoNamesIndex) {
		super(locationResolver != null ? locationResolver : new JackardIndexResolver());
		if (geoNamesIndex == null) {
			this.geoNamesIndex = new LuceneIndex();
			this.geoNamesIndex.load();
		}
		else {
			this.geoNamesIndex = geoNamesIndex;
		}
		this.mapRecognizer = new MapDetector(false);
		this.coordinateGraphicDetector = new CoordinateGraphicRecognizer();
		this.coordinateTextDetector = new CoordinateTextRecognizer();
		this.worldMapDetector = new WorldMapRecognizer();
		this.geonamesGraphicDetector = new GeoNamesGraphicRecognizer(this.geoNamesIndex);
		try {
			this.geonamesTextDetector = new GeoNamesTextRecognizer(this.geoNamesIndex);
		} catch (IOException | ClassNotFoundException ex) {
			LoggerFactory.getLogger(getClass()).error("Loading GeoNamesTextDetector failed. Continuing with the DumbCountryTextDetector. " + ex.getMessage());
			this.geonamesTextDetector = new DumbCountryTextRecognizer();
		}	
	}

	@Override
	public LocationCollection getDocumentLocations(Document document) {
		LocationCollection documentLocations = new LocationCollection();
		for(String text : document.getTexts()) {
			this.getLocationsFromText(text, documentLocations, 0.5);
		}
		return documentLocations;
	}
	
	public LocationCollection getMapLocations(Figure figure, CvGraphic graphic, LocationCollection documentLocations) {
		LocationCollection figureLocations = new LocationCollection(documentLocations);
		this.getLocationsFromText(figure.getCaption(), figureLocations, 0.75);
		boolean isWorldMap = this.worldMapDetector.recognize(graphic, figureLocations, 1);
		// Skip the slow stuff, as world map detection is pretty accurate (97% detection rate)
		if (!isWorldMap) {
			this.coordinateGraphicDetector.recognize(graphic, figureLocations, 1);
			if (this.geonamesGraphicDetector != null) {
				// should be executed last as it uses previous results for outlier detection
				this.geonamesGraphicDetector.recognize(graphic, figureLocations, 0.25);
			}
		}
		return figureLocations;
	}
	
	public void disableGeoNamesGraphicRecognizer() {
		this.geonamesGraphicDetector = null;
	}
	
	@Override
	public void extractFigureLocations(Figure figure, LocationCollection documentLocations) {
		CvGraphic cvGraphic = new CvGraphic(figure);

		// Detect whether it's a map or not
		float mapConfidence = this.mapRecognizer.detect(figure, cvGraphic);
		boolean isMap = (mapConfidence >= 0.4); // 10% tolerance
		LoggerFactory.getLogger(this.getClass()).debug((isMap ? "Map detected" : "NOT a map") + " (" + mapConfidence * 100 + "%)");
		if (isMap) {
			LocationCollection figureLocations = this.getMapLocations(figure, cvGraphic, documentLocations);
			this.resolveFigureLocation(figure, figureLocations);
		}
		cvGraphic.dispose();
	}
	
	public void getLocationsFromText(String text, LocationCollection locations, double weight) {
		if (text == null) {
			return;
		}
		this.coordinateTextDetector.recognize(text, locations, weight);
		this.geonamesTextDetector.recognize(text, locations, weight);
	}

	@Override
	public void shutdown() {
		if (this.geoNamesIndex != null) {
			this.geoNamesIndex.close();
		}
	}
	
}