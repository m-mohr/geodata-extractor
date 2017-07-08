package de.lutana.geodataextractor.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import de.lutana.geodataextractor.entity.Location;
import java.util.Collection;

/**
 *
 * @author Matthias Mohr
 */
public class GeoTools {
	
	private static GeometryFactory geometryFactory = new GeometryFactory();
	
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
	
}
