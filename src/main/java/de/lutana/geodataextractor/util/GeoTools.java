package de.lutana.geodataextractor.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import de.lutana.geodataextractor.entity.Location;
import java.util.Collection;
import org.apache.commons.math3.util.Precision;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;

public class GeoTools {
	
	private static GeometryFactory geometryFactory = new GeometryFactory();
	private static final int LAT_LON_PRECISION = 4;
    public static final double EARTH_R = 6372.8; // In kilometers, for haversine calculation
	
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
		return Precision.round(deg, LAT_LON_PRECISION);
	}
	
}
