package de.lutana.geodataextractor.entity.locationresolver;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;

public class ScoredUnionResolver extends UnionResolver {

	@Override
	public Location resolve(LocationCollection locations) {
		if (locations.isEmpty()) {
			return null;
		}
		else if (locations.size() == 1) {
			return locations.get(0);
		}
		
		double sum = 0;
		for(Location l : locations) {
			sum += l.getScore();
		}
		double avgScore = sum / locations.size();
		LocationCollection filteredLocations = new LocationCollection();
		for(Location l : locations) {
			if (l.getScore() > avgScore) {
				filteredLocations.add(l);
			}
		}
		return super.resolve(filteredLocations);
	}
	
	
	
}
