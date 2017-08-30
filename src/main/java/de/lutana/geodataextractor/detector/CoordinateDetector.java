package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.util.GeoTools;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;

/**
 * Parses coordinates from text and converts them to Dezimalgrad.
 *
 * Supports: 
 * - Dezimalgrad
 * - Grad Minuten
 * - Grad Minuten Sekunden
 * - UTM-Koordinaten
 *
 * @author Matthias
 */
public class CoordinateDetector implements TextDetector {

	@Override
	public void detect(String text, LocationCollection locations) {
		try {
			CoordinatePairs wgsCP = this.parseWgsCoordinates(text);
			locations.add(wgsCP.getLocation());
		} catch (NullPointerException e) {
		}

		try {
			CoordinatePairs utmCP = this.parseUtmCoordinates(text);
			locations.add(utmCP.getLocation());
		} catch (NullPointerException e) {
		}
	}

	public CoordinatePairs parseUtmCoordinates(String text) {
		CoordinatePairs cp = new CoordinatePairs();
		Matcher m = GeoTools.UTM_PATTERN.matcher(text);
		while (m.find()) {
			Integer lngZone = Integer.parseInt(m.group(1));
			Character latZone = m.group(2).charAt(0);
			Double easting = Double.parseDouble(m.group(3));
			Double northing = Double.parseDouble(m.group(4));
			UTMRef utm = new UTMRef(lngZone, latZone, easting, northing);
			LatLng latlng = utm.toLatLng();
			cp.latitude().add(latlng.getLatitude());
			cp.longitude().add(latlng.getLongitude());
		}
		return cp;
	}

	public CoordinatePairs parseWgsCoordinates(String text) {
		CoordinatePairs cp = new CoordinatePairs();
		Matcher m = GeoTools.WGS84_PATTERN.matcher(text);
		while (m.find()) {
			// These replaces try to reduce bad OCR detections.
			String degStr = m.group(1).replace(',', '.').replace('‚', '.').replace('’', '.').replace('-', '.').replace('_', '.');
			Double deg = Double.parseDouble(degStr);

			String minStr = m.group(2);
			Double min = 0d;
			if (minStr != null && !minStr.isEmpty()) {
				minStr = minStr.replace(',', '.').replace('‚', '.').replace('’', '.').replace('-', '.').replace('_', '.');
				min = Double.parseDouble(minStr);
			}

			String secStr = m.group(3);
			Double sec = 0d;
			if (secStr != null && !secStr.isEmpty()) {
				secStr = secStr.replace(',', '.').replace('‚', '.').replace('’', '.').replace('-', '.').replace('_', '.');
				sec = Double.parseDouble(secStr);
			}

			Double coord = deg + (min / 60d) + (sec / 3600d);

			String sigStr = m.group(4);
			if (sigStr.equalsIgnoreCase("S") || sigStr.equalsIgnoreCase("W")) {
				coord = -1 * coord;
			}

			// ToDO: Remove outliers
			if (sigStr.equalsIgnoreCase("S") || sigStr.equalsIgnoreCase("N")) {
				cp.latitude().add(coord);
			} else {
				cp.longitude().add(coord);
			}
		}
		return cp;
	}

	public class CoordinatePairs {

		private final List<Double> longitude = new ArrayList<>();
		private final List<Double> latitude = new ArrayList<>();

		public List<Double> longitude() {
			return this.longitude;
		}

		public List<Double> latitude() {
			return this.latitude;
		}

		public Location getLocation() {
			if (longitude.size() > 0 && latitude.size() > 0) {
				Double minLon = Collections.min(longitude);
				Double maxLon = Collections.max(longitude);
				Double minLat = Collections.min(latitude);
				Double maxLat = Collections.max(latitude);
				return new Location(minLon, maxLon, minLat, maxLat);
			} else {
				return null;
			}
		}

	}

}
