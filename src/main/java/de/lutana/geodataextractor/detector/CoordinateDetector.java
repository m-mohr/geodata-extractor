package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.util.CoordinateParser;

/**
 * Parses coordinates from text and converts them to decimal degree.
 *
 * Supports: 
 * - Decimal degree
 * - Degree Minutes
 * - Degree Minutes Seconds
 * - Universal Transverse Mercator (UTM)
 * - Ordnance Survey (OS)
 * - Military Grid Reference System (MGRS)
 *
 * @author Matthias Mohr
 */
public class CoordinateDetector implements TextDetector {

	@Override
	public void detect(String text, LocationCollection locations) {
		CoordinateParser p = new CoordinateParser();
		p.parseFullText(text, locations);
	}

}
