package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;

/**
 *
 * @author Matthias Mohr
 */
public abstract class CaptionDetector implements Detector {

	@Override
	public Location detect(Figure figure) {
		String caption = figure.getCaption();
		return this.detectFromCaption(caption);
	}
	
	protected abstract Location detectFromCaption(String caption);
	
}
