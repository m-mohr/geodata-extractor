package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.detector.coordinates.CoordinateList;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.detector.coordinates.CoordinateParser;

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
public class CoordinateTextDetector implements TextDetector {

	@Override
	public void detect(String text, LocationCollection locations) {
		CoordinateParser p = new CoordinateParser();

		CoordinateList wgsCP = p.parseWgs84Coordinates(text);
		if (wgsCP.hasLocation()) {
			locations.add(wgsCP.getLocation());
		}

		CoordinateList utmCP = p.parseUtmCoordinates(text);
		if (utmCP.hasLocation()) {
			locations.add(utmCP.getLocation());
		}

		CoordinateList osCP = p.parseOsCoordinates(text);
		if (osCP.hasLocation()) {
			locations.add(osCP.getLocation());
		}

		CoordinateList mgrsCP = p.parseMgrsCoordinates(text);
		if (mgrsCP.hasLocation()) {
			locations.add(mgrsCP.getLocation());
		}
	}

}
