package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;
import java.io.File;

/**
 *
 * @author Matthias Mohr
 */
public abstract class GraphicDetector implements Detector {

	@Override
	public Location detect(Figure figure) {
		File file = figure.getGraphic();
		return this.detectFromGraphic(file);
	}
	
	protected abstract Location detectFromGraphic(File graphicFile);
	
}
