package de.lutana.geodataextractor.entity.locationresolver;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.util.GeoTools;

public class UnionResolver implements LocationResolver {

	@Override
	public Location resolve(LocationCollection locations) {
		Location union = GeoTools.union(locations);
		if (union != null) {
			union.setWeight(1);
			double scoreSum = 0;
			for(Location l : locations) {
				scoreSum += l.getScore();
			}
			union.setProbability(scoreSum / locations.size());
		}
		return union;
	}
	
	
	
}
