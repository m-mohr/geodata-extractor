package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.LocationCollection;

/**
 * Detects location information based on a figure caption.
 * 
 * @author Matthias Mohr
 */
public abstract class CaptionDetector implements Detector {

	/**
	 * Returns a list of detected locations for the specified figure based on it's caption.
	 * 
	 * @param figure
	 * @return 
	 */
	@Override
	public LocationCollection detect(Figure figure) {
		String caption = figure.getCaption();
		return this.detectFromCaption(caption);
	}
	
	/**
	 * Returns a list of detected locations for the specified figure based on it's caption.
	 * 
	 * @param caption
	 * @return 
	 */
	protected abstract LocationCollection detectFromCaption(String caption);
	
}
