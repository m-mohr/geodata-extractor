package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.detector.coordinates.CoordinateList;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.detector.coordinates.CoordinateParser;
import de.lutana.geodataextractor.entity.Location;
import org.slf4j.LoggerFactory;

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
	public boolean detect(String text, LocationCollection locations, double weight) {
		CoordinateParser p = new CoordinateParser();
		int before = locations.size();

		CoordinateList wgsCP = p.parseWgs84Coordinates(text, false);
		if (wgsCP.hasLocation()) {
			Location l = wgsCP.getLocation();
			if (l != null) {
				l.setWeight(weight);
				LoggerFactory.getLogger(getClass()).debug("Parsed location " + l + " from WGS84 text coordinates.");
				locations.add(l);
			}
		}

		CoordinateList utmCP = p.parseUtmCoordinates(text);
		if (utmCP.hasLocation()) {
			Location l = utmCP.getLocation();
			if (l != null) {
				l.setWeight(weight);
				LoggerFactory.getLogger(getClass()).debug("Parsed location " + l + " from UTM text coordinates.");
				locations.add(l);
			}
		}

		CoordinateList osCP = p.parseOsCoordinates(text);
		if (osCP.hasLocation()) {
			Location l = osCP.getLocation();
			if (l != null) {
				l.setWeight(weight);
				LoggerFactory.getLogger(getClass()).debug("Parsed location " + l + " from OS text coordinates.");
				locations.add(l);
			}
		}

		CoordinateList mgrsCP = p.parseMgrsCoordinates(text);
		if (mgrsCP.hasLocation()) {
			Location l = mgrsCP.getLocation();
			if (l != null) {
				l.setWeight(weight);
				LoggerFactory.getLogger(getClass()).debug("Parsed location " + l + " from MGRS text coordinates.");
				locations.add(l);
			}
		}

		return (before - locations.size()) > 0;
	}

}
