package de.lutana.geodataextractor.detector.coordinates;

import de.lutana.geodataextractor.entity.Location;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoordinateList extends ArrayList<CoordinateFromText> {

	public boolean hasLocation() {
		boolean hasLon = false;
		boolean hasLat = false;
		for(CoordinateFromText c : this) {
			if (c.getLatitude() != null) {
				hasLat = true;
			}
			if (c.getLongitude() != null) {
				hasLon = true;
			}
			if (hasLon && hasLat) {
				return true;
			}
		}
		return false;
	}

	public Location getLocation() {
		return this.getLocation(false);
	}

	public Location getLocation(boolean removeOutliers) {
		List<Double> longitude = new ArrayList<>();
		List<Double> latitude = new ArrayList<>();
		for(CoordinateFromText c : this) {
			Double lat = c.getLatitude();
			if (lat != null) {
				latitude.add(lat);
			}
			Double lon = c.getLongitude();
			if (lon != null) {
				longitude.add(lon);
			}
		}
		
		if (!longitude.isEmpty() && !latitude.isEmpty()) {
			if (removeOutliers) {
				SpatialOutlierDetector sod = new SpatialOutlierDetector();
				sod.detectWgs84Outliers(longitude, true);
				sod.detectWgs84Outliers(latitude, false);
			}

			Collections.sort(longitude);
			Collections.sort(latitude);

			Double minLon = longitude.get(0);
			Double maxLon = longitude.get(longitude.size() - 1);
			Double minLat = latitude.get(0);
			Double maxLat = latitude.get(latitude.size() - 1);
			return new Location(minLon, maxLon, minLat, maxLat);
		} else {
			return null;
		}
	}

}
