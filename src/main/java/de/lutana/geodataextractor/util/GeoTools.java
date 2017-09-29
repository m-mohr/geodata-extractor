package de.lutana.geodataextractor.util;

import com.vividsolutions.jts.geom.Envelope;
import de.lutana.geodataextractor.entity.Location;
import java.util.Collection;
import org.apache.commons.math3.util.Precision;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.opencv.core.Rect;

public class GeoTools {

	/**
	 * @see GeoTools::roundLatLon()
	 */
	private static final int LAT_LON_PRECISION = 4;
	/**
	 * Earth radius in kilometers, for haversine calculation.
	 */
    public static final double EARTH_R = 6372.8;
	
	public static Location union(Collection<Location> collection) {
		Location env = null;
		for(Location l : collection) {
			if (env == null) {
				env = l;
			}
			else {
				env.expandToInclude(l);
			}
		}
		if (env != null) {
			return new Location(env);
		}
		return null;
	}

	/**
	 * Returns the distance between two WGS84 based coordinates in meters.
	 * 
	 * Claculated using Haversine formulae.
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return 
	 */
    public static double calcHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_R * c;
    }
	
	/**
	 * Returns the distance between two WGS84 based coordinates in meters.
	 * 
	 * Claculated using Vincenty’s formulae.
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return 
	 */
	public static double calcVincentyDistance(double lat1, double lon1, double lat2, double lon2) {
		GeodeticCalculator geoCalc = new GeodeticCalculator();
		GlobalCoordinates point1 = new GlobalCoordinates(lat1, lon1);
		GlobalCoordinates point2 = new GlobalCoordinates(lat2, lon2);
		GeodeticCurve geoCurve = geoCalc.calculateGeodeticCurve(Ellipsoid.WGS84, point1, point2);
		return geoCurve.getEllipsoidalDistance();
	}
	
	/**
	 * Calculates the Jackard Index (also known as Intersection over Union).
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
		

		Envelope intersection = expected.intersection(result);

		Envelope union = new Envelope(expected);
		union.expandToInclude(result);

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
		return Precision.round(deg, LAT_LON_PRECISION);
	}
	
	public static Rect addMargin(Rect r, int lrMargin, int tbMargin, int maxWidth, int maxHeight) {
		int x = r.x - lrMargin;
		int y = r.y - tbMargin;
		int w = r.width + 2 * lrMargin;
		int h = r.height + 2 * tbMargin;
		x = x < 0 ? 0 : x;
		y = y < 0 ? 0 : y;
		w = (x + w) > maxWidth ? (maxWidth - x) : w;
		h = (y + h) > maxHeight ? (maxHeight - y) : h;
		return new Rect(x, y, w, h);
	}

	public static boolean isMostlyHorizontal(double rad, boolean includeDiagonal) {
		double deg = normalizeAngle(rad);
		double range = (includeDiagonal ? 45 : 30);
		return (deg >= 0 && deg < range);
	}
	
	public static boolean isMostlyDiagonal(double rad) {
		double deg = normalizeAngle(rad);
		return (deg >= 30 && deg < 60);
	}
	
	public static boolean isMostlyVertical(double rad, boolean includeDiagonal) {
		double deg = normalizeAngle(rad);
		double range = (includeDiagonal ? 45 : 30);
		return (deg >= range && deg <= 90);
	}
	
	private static double normalizeAngle(double rad) {
		double deg = Math.toDegrees(rad) % 180;
		if (deg < 0) {
			deg += 180;
		}
		if (deg > 90) {
			deg -= 90;
		}
		return deg;
	}
	
}
