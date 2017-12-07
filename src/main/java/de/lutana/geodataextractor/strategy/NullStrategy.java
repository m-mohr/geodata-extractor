package de.lutana.geodataextractor.strategy;

import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.entity.locationresolver.LocationResolver;
import de.lutana.geodataextractor.entity.locationresolver.UnionResolver;

/**
 * Doesn't perform any task.
 * 
 * Could be useful to only parse the document, but avoid location detection.
 * 
 * @author Matthias
 */
public class NullStrategy extends AbstractStrategy {
	
	public NullStrategy() {
		this(new UnionResolver());
	}

	public NullStrategy(LocationResolver locationResolver) {
		super(locationResolver);
	}

	@Override
	public LocationCollection getDocumentLocations(Document document) {
		return new LocationCollection();
	}

	@Override
	public void extractFigureLocations(Figure figure, LocationCollection documentLocations) {}
	
}
