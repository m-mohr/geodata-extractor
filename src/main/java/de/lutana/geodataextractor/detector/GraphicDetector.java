package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.LocationCollection;
import java.io.File;

/**
 * Detects location information based on a graphic.
 * 
 * @author Matthias Mohr
 */
public abstract class GraphicDetector implements Detector {

	/**
	 * Returns a list of detected locations for the specified figure based on it's graphic.
	 * 
	 * @param figure
	 * @return 
	 */
	@Override
	public LocationCollection detect(Figure figure) {
		File file = figure.getGraphic();
		return this.detectFromGraphic(file);
	}
	
	/**
	 * Returns a list of detected locations for the specified figure based on it's graphic.
	 * 
	 * @param graphicFile
	 * @return 
	 */
	protected abstract LocationCollection detectFromGraphic(File graphicFile);
	
}
