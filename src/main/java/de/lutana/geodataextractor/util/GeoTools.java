package de.lutana.geodataextractor.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import de.lutana.geodataextractor.entity.Location;
import java.util.Collection;

/**
 *
 * @author Matthias
 */
public class GeoTools {
	
	public static Location union(Collection<Location> collection) {
		// ToDo: Testing
		Geometry env = null;
		GeometryFactory geometryFactory = new GeometryFactory();
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
	
}
