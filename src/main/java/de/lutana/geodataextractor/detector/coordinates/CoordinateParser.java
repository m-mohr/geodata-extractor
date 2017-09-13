package de.lutana.geodataextractor.detector.coordinates;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.me.jstott.jcoord.MGRSRef;
import uk.me.jstott.jcoord.NotDefinedOnUTMGridException;
import uk.me.jstott.jcoord.OSRef;
import uk.me.jstott.jcoord.UTMRef;

public class CoordinateParser {
	
	/**
	 * Detects UTM coorinates.
	 * First and second matches are the grid number (longitude zone as number, latitude zone as letter), third is the easting value and fourth is the northing value.
	 */
	public static final Pattern UTM_PATTERN = Pattern.compile("(?<![\\w\\.°'\"-])(\\d{1,2})\\s?([A-Z])\\s(\\d{6})\\s(\\d{1,7})(?![\\w°'\"-])");
	
	/**
	 * Detects: Dezimalgrad, Grad Minuten and Grad Minuten Sekunden
	 * First match is degree, second is minutes (might be empty), third is seconds (might be empty) and third is the cardinal point (N/S/E/W).
	 */
	public static final Pattern WGS84_PATTERN = Pattern.compile("(?<![\\w\\.°'\"-])(-?\\d+(?:\\.\\d+)?)°(?:\\s*(\\d+(?:\\.\\d+)?)')?(?:\\s*(\\d+(?:\\.\\d+)?)(?:\"|''))?\\s*(N|S|W|E)(?![\\w°'\"-])");
	
	/**
	 * Detects Ordnance Survey coordinates.
	 * First match are the grid letters, second is the easting value and third is the northing value.
	 * If there is a fourth match there is no second and third match. The fourth match then includes both easting and northing, which need to be splitted in the middle.
	 */
	public static final Pattern OS_PATTERN = Pattern.compile("(?<![\\w\\.°'\"-])(H[PTUW-Z]|N[A-DF-KL-OR-UW-Z]|OV|S[CDEHJKM-PR-Z]|T[AFGLMQRV])(?:\\s?(\\d{2,5})\\s(\\d{2,5})|(\\d{4}|\\d{6}|\\d{8}|\\d{10})(?!\\s\\d{2,5}))(?![\\w°'\"-])");
	
	/**
	 * Detects Military Grid Reference System.
	 * Regexp is a little to simple, so might parse some invalid coordinates.
	 * First match is the grid code, second match are the grid numbers (might be separated by a space).
	 */
	public static final Pattern MGRS_PATTERN = Pattern.compile("(?<![\\w\\.°'\"-])(\\d{1,2}[C-X][A-HJ-NP-Z]{2})\\s?(\\d{1,5}\\s?\\d{1,5})(?![\\w°'\"-])");
	
	public CoordinateList parse(String text) {
		CoordinateList list = new CoordinateList();
		if (text == null) {
			return list;
		}
		this.parseWgs84Coordinates(text, list);
		this.parseUtmCoordinates(text, list);
		this.parseOsCoordinates(text, list);
		this.parseMgrsCoordinates(text, list);
		return list;
	}

	/**
	 * See parseMgrsCoordinates(String text, CoordinatePairs clist) for details.
	 * 
	 * @param text
	 * @return 
	 */
	public CoordinateList parseMgrsCoordinates(String text) {
		CoordinateList clist = new CoordinateList();
		this.parseMgrsCoordinates(text, clist);
		return clist;
	}
	
	/**
	 * Detect Military Grid Reference System coordinates.
	 * 
	 * We assume it's not Bessel based as we can't really know.
	 * This detects only full coordinates (including both easting and northing).
	 * 
	 * @param text
	 * @param clist
	 */
	protected void parseMgrsCoordinates(String text, CoordinateList clist) {
		Matcher m = MGRS_PATTERN.matcher(text);
		while (m.find()) {
			String ref = m.group().replaceAll("\\s", "");
			try {
				MGRSRef mgrs = new MGRSRef(ref); // We assume it's not Bessel based as we can't really know.
				CoordinateFromText c = new CoordinateFromText(mgrs.toLatLng(), m.group(), m.start(), m.end(), 0.9);
				clist.add(c);
			} catch(IllegalArgumentException | NotDefinedOnUTMGridException e) {
			}
		}
	}

	/**
	 * See parseUtmCoordinates(String text, CoordinatePairs clist) for details.
	 * 
	 * @param text
	 * @return 
	 */
	public CoordinateList parseUtmCoordinates(String text) {
		CoordinateList clist = new CoordinateList();
		this.parseUtmCoordinates(text, clist);
		return clist;
	}

	/**
	 * Detect UTM coordinates.
	 * 
	 * This detects only full coordinates (including both easting and northing).
	 * 
	 * @param text
	 * @param clist
	 */
	protected void parseUtmCoordinates(String text, CoordinateList clist) {
		Matcher m = UTM_PATTERN.matcher(text);
		while (m.find()) {
			try {
				Integer lngZone = Integer.parseInt(m.group(1));
				Character latZone = m.group(2).charAt(0);
				Double easting = Double.parseDouble(m.group(3));
				Double northing = Double.parseDouble(m.group(4));
				UTMRef utm = new UTMRef(lngZone, latZone, easting, northing);
				CoordinateFromText c = new CoordinateFromText(utm.toLatLng(), m.group(), m.start(), m.end(), 0.9);
				clist.add(c);
			} catch(NotDefinedOnUTMGridException | NumberFormatException e) {
			}
		}
	}

	/**
	 * See parseOsCoordinates(String text, CoordinatePairs clist) for details.
	 * 
	 * @param text
	 * @return 
	 */
	public CoordinateList parseOsCoordinates(String text) {
		CoordinateList clist = new CoordinateList();
		this.parseOsCoordinates(text, clist);
		return clist;
	}

	/**
	 * Detect Ordnance Survey coordinates.
	 * 
	 * This detects only full coordinates (including both easting and northing).
	 * Returns the number of matches/coordinates found.
	 * 
	 * @param text
	 * @param clist
	 */
	protected void parseOsCoordinates(String text, CoordinateList clist) {
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
			CoordinateFromText c = new CoordinateFromText(os.toLatLng(), m.group(), m.start(), m.end(), 1);
			clist.add(c);
		}
	}

	/**
	 * See parseWgs84Coordinates(String text, CoordinatePairs clist) for details.
	 * 
	 * @param text
	 * @return 
	 */
	public CoordinateList parseWgs84Coordinates(String text) {
		CoordinateList clist = new CoordinateList();
		this.parseWgs84Coordinates(text, clist);
		return clist;
	}

	/**
	 * Detect Latitude/Longitude (WGS84) coordinates from decimal degrees or DM or DMS.
	 * 
	 * This also detects single longitude or latitude values.
	 * Returns the number of matches (which is NOT necessarily equal to ne number of full coordinates found).
	 * 
	 * @param text
	 * @param clist
	 */
	protected void parseWgs84Coordinates(String text, CoordinateList clist) {
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

			CoordinateFromText c;
			long roundedCoord = Math.round(coord);
			if (sigStr.equalsIgnoreCase("S") || sigStr.equalsIgnoreCase("N")) {
				if (roundedCoord > 90 || roundedCoord < -90) {
					continue;
				}
				c = new CoordinateFromText(coord, null, m.group(), m.start(), m.end(), 0.7);
			} else {
				if (roundedCoord > 180 || roundedCoord < -180) {
					continue;
				}
				c = new CoordinateFromText(null, coord, m.group(), m.start(), m.end(), 0.7);
			}
			clist.add(c);
		}
	}
	
}
