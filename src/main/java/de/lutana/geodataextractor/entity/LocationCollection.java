package de.lutana.geodataextractor.entity;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import de.lutana.geodataextractor.util.GeoTools;
import java.util.ArrayList;

/**
 *
 * @author Matthias Mohr
 */
public class LocationCollection extends ArrayList<Location> {
	
	public Location union() {
		return GeoTools.union(this);
	}

}
