package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.LocationCollection;
import java.io.File;

/**
 * Interface for different location information detection approaches based on graphics.
 * 
 * @author Matthias Mohr
 */
public interface GraphicDetector {
	
	/**
	 * Returns a list of detected locations for the specified graphic.
	 * 
	 * @param graphicFile
	 * @param locations
	 */
	public void detect(File graphicFile, LocationCollection locations);
	
}
