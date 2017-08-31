package de.lutana.geodataextractor.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import de.lutana.geodataextractor.entity.Location;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 *
 * @author Matthias Mohr
 */
public class GeoTools {
	
	private static GeometryFactory geometryFactory = new GeometryFactory();
	private static final int LAT_LON_PRECISION = 4;
	
	/**
	 * Detects UTM coorinates.
	 * First and second matches are the grid number (longitude zone as number, latitude zone as letter), third is the easting value and fourth is the northing value.
	 */
	public static final Pattern UTM_PATTERN = Pattern.compile("\\b(\\d{1,2})\\s?([A-Z])\\s(\\d{6})\\s(\\d{1,7})\\b");
	
	/**
	 * Detects: Dezimalgrad, Grad Minuten and Grad Minuten Sekunden
	 * First match is degree, second is minutes (might be empty), third is seconds (might be empty) and third is the cardinal point (N/S/E/W).
	 */
	public static final Pattern WGS84_PATTERN = Pattern.compile("\\b(-?\\d+(?:[\\.,‚’_-]\\d+)?)[°o](?:\\s*(\\d+(?:[\\.,‚’_-]\\d+)?)['‘’‚,`´\\|])?(?:\\s*(\\d+(?:[\\.,‚’_-]\\d+)?)(?:\"|''|“|”|„))?\\s*(N|S|W|E)\\b", Pattern.CASE_INSENSITIVE);
	
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
	
	public static Location union(Collection<Location> collection) {
		// ToDo: Testing
		Geometry env = null;
		for(Location l : collection) {
			Geometry geom = geometryFactory.toGeometry(l);
			if (env == null) {
				env = geom;
			}
			else {
				env.union(geom);
			}
		}
		if (env != null) {
			return new Location(env.getEnvelopeInternal());
		}
		return null;
	}
	
	/**
	 * 
	 * @param expected
	 * @param result
	 * @return 
	 * @see https://en.wikipedia.org/wiki/Jaccard_index
	 */
	public static double calcJaccardIndex(Location expected, Location result) {
		if ((expected == null || result == null) && expected != result) {
			return 0;
		}
		else if (expected == null && result == null) {
			return 1;
		}

		Geometry expectedGeom = geometryFactory.toGeometry(expected);
		Geometry resultGeom = geometryFactory.toGeometry(result);
		Geometry intersection = expectedGeom.intersection(resultGeom);
		Geometry union = expectedGeom.union(resultGeom);
		return intersection.getArea() / union.getArea();
	}
	
	/**
	 * Rounds to fourth decimal place (as specified in LAT_LON_PRECISION). Accuracy is 11m in this case.
	 * 
	 * @param deg
	 * @return 
	 * @see https://gis.stackexchange.com/questions/8650/measuring-accuracy-of-latitude-and-longitude/8674#8674
	 */
	public static double roundLatLon(double deg) {
		double pow = Math.pow(10, LAT_LON_PRECISION);
		return Math.round(deg * pow) / pow;
	}
	
}
