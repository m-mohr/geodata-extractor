package de.lutana.geodataextractor.entity.locationresolver;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;

public interface LocationResolver {
	
	public Location resolve(LocationCollection locations);
	
}
