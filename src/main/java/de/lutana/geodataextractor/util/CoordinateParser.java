package de.lutana.geodataextractor.util;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.MGRSRef;
import uk.me.jstott.jcoord.NotDefinedOnUTMGridException;
import uk.me.jstott.jcoord.OSRef;
import uk.me.jstott.jcoord.UTMRef;

public class CoordinateParser {
	
	/**
	 * Detects UTM coorinates.
	 * First and second matches are the grid number (longitude zone as number, latitude zone as letter), third is the easting value and fourth is the northing value.
	 */
	public static final Pattern UTM_PATTERN = Pattern.compile("\\b(\\d{1,2})\\s?([A-Z])\\s(\\d{6})\\s(\\d{1,7})\\b");
	
	/**
	 * Detects: Dezimalgrad, Grad Minuten and Grad Minuten Sekunden
	 * First match is degree, second is minutes (might be empty), third is seconds (might be empty) and third is the cardinal point (N/S/E/W).
	 */
	public static final Pattern WGS84_PATTERN = Pattern.compile("\\b(-?\\d+(?:\\.\\d+)?)°(?:\\s*(\\d+(?:\\.\\d+)?)')?(?:\\s*(\\d+(?:\\.\\d+)?)(?:\"|''))?\\s*(N|S|W|E)\\b");
	
	/**
	 * Detects Ordnance Survey coordinates.
	 * First match are the grid letters, second is the easting value and third is the northing value.
	 * If there is a fourth match there is no second and third match. The fourth match then includes both easting and northing, which need to be splitted in the middle.
	 */
	public static final Pattern OS_PATTERN = Pattern.compile("\\b(H[PTUW-Z]|N[A-DF-KL-OR-UW-Z]|OV|S[CDEHJKM-PR-Z]|T[AFGLMQRV])(?:\\s?(\\d{2,5})\\s(\\d{2,5})|(\\d{4}|\\d{6}|\\d{8}|\\d{10})(?!\\s\\d{2,5}))\\b");
	
	/**
	 * Detects Military Grid Reference System.
	 * Regexp is a little to simple, so might parse some invalid coordinates.
	 * First match is the grid code, second match are the grid numbers (might be separated by a space).
	 */
	public static final Pattern MGRS_PATTERN = Pattern.compile("\\b(\\d{1,2}[C-X][A-HJ-NP-Z]{2})\\s?(\\d{1,5}\\s?\\d{1,5})\\b");
	
	private boolean removeWgs84Outliers;
	
	public CoordinateParser() {
		this(false);
	}
	
	public CoordinateParser(boolean removeWgs84Outliers) {
		this.removeWgs84Outliers = removeWgs84Outliers;
	}
	
	public void parse(String text, LocationCollection locations) {
		if (text == null) {
			return;
		}

		CoordinatePairs wgsCP = this.parseWgsCoordinates(text);
		if (wgsCP.hasLocation()) {
			locations.add(wgsCP.getLocation(this.removeWgs84Outliers));
		}

		CoordinatePairs utmCP = this.parseUtmCoordinates(text);
		if (utmCP.hasLocation()) {
			locations.add(utmCP.getLocation());
		}

		CoordinatePairs osCP = this.parseOsCoordinates(text);
		if (osCP.hasLocation()) {
			locations.add(osCP.getLocation());
		}

		CoordinatePairs mgrsCP = this.parseMgrsCoordinates(text);
		if (mgrsCP.hasLocation()) {
			locations.add(mgrsCP.getLocation());
		}
	}

	/**
	 * Detect Military Grid Reference System coordinates.
	 * 
	 * We assume it's not Bessel based as we can't really know.
	 * This detects only full coordinates (including both easting and northing).
	 * 
	 * @param text
	 * @return 
	 */
	public CoordinatePairs parseMgrsCoordinates(String text) {
		CoordinatePairs cp = new CoordinatePairs();
		Matcher m = MGRS_PATTERN.matcher(text);
		while (m.find()) {
			String ref = m.group().replaceAll("\\s", "");
			try {
				MGRSRef mgrs = new MGRSRef(ref); // We assume it's not Bessel based as we can't really know.
				cp.addLatLng(mgrs.toLatLng());
			} catch(IllegalArgumentException | NotDefinedOnUTMGridException e) {
			}
		}
		return cp;
	}

	/**
	 * Detect UTM coordinates.
	 * 
	 * This detects only full coordinates (including both easting and northing).
	 * 
	 * @param text
	 * @return 
	 */
	public CoordinatePairs parseUtmCoordinates(String text) {
		CoordinatePairs cp = new CoordinatePairs();
		Matcher m = UTM_PATTERN.matcher(text);
		while (m.find()) {
			try {
				Integer lngZone = Integer.parseInt(m.group(1));
				Character latZone = m.group(2).charAt(0);
				Double easting = Double.parseDouble(m.group(3));
				Double northing = Double.parseDouble(m.group(4));
				UTMRef utm = new UTMRef(lngZone, latZone, easting, northing);
				cp.addLatLng(utm.toLatLng());
			} catch(NotDefinedOnUTMGridException | NumberFormatException e) {
			}
		}
		return cp;
	}

	/**
	 * Detect Ordnance Survey coordinates.
	 * 
	 * This detects only full coordinates (including both easting and northing).
	 * 
	 * @param text
	 * @return 
	 */
	public CoordinatePairs parseOsCoordinates(String text) {
		CoordinatePairs cp = new CoordinatePairs();
		Matcher m = OS_PATTERN.matcher(text);
		while (m.find()) {
			String zone = m.group(1);
			String eastingStr;
			String northingStr;
			if (m.groupCount() == 4 && m.group(4) != null) {
				String coords = m.group(4);
				int coordLength = coords.length() / 2;
				eastingStr = coords.substring(0, coordLength);
				northingStr = coords.substring(coordLength);
			}
			else {
				eastingStr = m.group(2);
				northingStr = m.group(3);
			}
			Integer easting = Integer.parseInt(eastingStr);
			Integer northing = Integer.parseInt(northingStr);
			OSRef os = new OSRef(zone, easting, northing);
			cp.addLatLng(os.toLatLng());
		}
		return cp;
	}

	/**
	 * Detect Latitude/Longitude (WGS84) coordinates from decimal degrees or DM or DMS.
	 * 
	 * This also detects single longitude or latitude values.
	 * 
	 * @param text
	 * @return 
	 */
	public CoordinatePairs parseWgsCoordinates(String text) {
		CoordinatePairs cp = new CoordinatePairs();
		Matcher m = WGS84_PATTERN.matcher(text);
		while (m.find()) {
			Double deg;
			try {
				deg = Double.parseDouble(m.group(1));
			} catch(NumberFormatException | NullPointerException e) {
				continue;
			}

			Double min = 0d;
			try {
				min = Double.parseDouble(m.group(2));
			} catch(NumberFormatException | NullPointerException e) {}

			Double sec = 0d;
			try {
				sec = Double.parseDouble(m.group(3));
			} catch(NumberFormatException | NullPointerException e) {}

			Double coord = deg + (min / 60d) + (sec / 3600d);

			String sigStr = m.group(4);
			if (sigStr.equalsIgnoreCase("S") || sigStr.equalsIgnoreCase("W")) {
				coord = -1 * coord;
			}

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
		
		public void addLatLng(LatLng ll) {
			if (ll == null) {
				return;
			}
			latitude.add(ll.getLatitude());
			longitude.add(ll.getLongitude());
		}

		public List<Double> longitude() {
			return this.longitude;
		}

		public List<Double> latitude() {
			return this.latitude;
		}
		
		public boolean hasLocation() {
			return (longitude.size() > 0 && latitude.size() > 0);
		}
		
		public Location getLocation() {
			return this.getLocation(false);
		}

		public Location getLocation(boolean removeOutliers) {
			if (longitude.size() > 0 && latitude.size() > 0) {
				if (removeOutliers) {
					SpatialOutlierDetector sod = new SpatialOutlierDetector();
					sod.detectWgs84Outliers(longitude, true);
					sod.detectWgs84Outliers(latitude, false);
				}

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
