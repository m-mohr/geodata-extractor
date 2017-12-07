package de.lutana.geodataextractor.strategy;

import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.entity.locationresolver.LocationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStrategy implements Strategy {

	private LocationResolver locationResolver;
	
	public AbstractStrategy(LocationResolver locationResolver) {
		this.locationResolver = locationResolver;
	}
	
	public LocationResolver getLocationResolver() {
		return this.locationResolver;
	}
	
	public void setLocationResolver(LocationResolver locationResolver) {
		this.locationResolver = locationResolver;
	}

	@Override
	public boolean execute(Document document, Integer page) {
		Logger logger = LoggerFactory.getLogger(this.getClass());
		logger.info("## Document: " + document);

		LocationCollection documentLocations = this.getDocumentLocations(document);

		FigureCollection figures = document.getFigures();
		for(Figure figure : figures) {
			if (page != null && !figure.getPage().equals(page)) {
				continue;
			}
			logger.info("# " + figure);
	
			this.extractFigureLocations(figure, documentLocations);
		}

		return true;
	}
	
	public abstract LocationCollection getDocumentLocations(Document document);
	
	public abstract void extractFigureLocations(Figure figure, LocationCollection documentLocations);

	public void resolveFigureLocation(Figure figure, LocationCollection locations) {
		Location location = locations.resolveLocation(this.getLocationResolver());
		figure.setLocation(location);
	}

	@Override
	public void shutdown() {}
	
}
