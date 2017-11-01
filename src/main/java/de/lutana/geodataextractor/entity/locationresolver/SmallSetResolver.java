package de.lutana.geodataextractor.entity.locationresolver;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;

public abstract class SmallSetResolver implements LocationResolver {

	@Override
	public Location resolve(LocationCollection locations) {
		if (locations.isEmpty()) {
			return null;
		}

		int count = locations.size();
		switch (count) {
			case 0:
				return null;
			case 1:
				return locations.get(0);
			case 2:
				Location l1 = locations.get(0);
				double l1Score = l1.getScoreWithPenalty();
				Location l2 = locations.get(1);
				double l2Score = l2.getScoreWithPenalty();
				if (Math.abs(l1Score - l2Score) > 0.2) {
					// Decide to use only the higher scored location if score is somehow far different
					return l1Score > l2Score ? l1 : l2;
				}
				else if (l1.intersects(l2)) {
					// Both rectangles intersect, calculate an "average" rectangle.
					// ToDo: Check whether this works correct in all edge cases
					double minLon = this.avg(l1.getMinX(), l2.getMinX());
					double maxLon = this.avg(l1.getMaxX(), l2.getMaxX());
					double minLat = this.avg(l1.getMinY(), l2.getMinY());
					double maxLat = this.avg(l1.getMaxY(), l2.getMaxY());
					return new Location(minLon, maxLon, minLat, maxLat);
				}
				else {
					// We can't decide: return union
					UnionResolver r = new UnionResolver();
					return r.resolve(locations);
				}
			default:
				return null;
		}
	}
	
	private double avg(double m, double n) {
		return (m+n)/2;
	}
	
}
