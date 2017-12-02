package de.lutana.geodataextractor.strategy;

import de.lutana.geodataextractor.recognizer.cv.CvGraphic;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.entity.locationresolver.HeatmapResolver;
import de.lutana.geodataextractor.entity.locationresolver.LocationResolver;

/**
 * Fast strategy to gather and combine location information as quickly as possible.
 * 
 * Tries to find locations as fast as possible.
 * Therefore doesn't work much on the images.
 * Might return less or worse results than other strategies.
 * 
 * @author Matthias Mohr
 */
public class FastStrategy extends DefaultStrategy {
	
	public FastStrategy() {
		this(new HeatmapResolver());
	}
	
	public FastStrategy(LocationResolver locationResolver) {
		super(locationResolver);
	}

	@Override
	protected LocationCollection getMapLocations(Figure figure, CvGraphic graphic, LocationCollection documentLocations) {
		LocationCollection figureLocations = new LocationCollection(documentLocations);
		this.getLocationsFromText(figure.getCaption(), figureLocations, 0.75);
		this.worldMapDetector.recognize(graphic, figureLocations, 1);
		return figureLocations;
	}
	
}