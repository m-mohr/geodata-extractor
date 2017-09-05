package de.lutana.geodataextractor.util;

import de.lutana.geodataextractor.entity.Location;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.me.jstott.jcoord.LatLng;

public class CoordinatePairs {

	private final List<Double> longitude = new ArrayList<>();
	private final List<Double> latitude = new ArrayList<>();

	public void addLatLng(LatLng ll) {
		if (ll == null) {
			return;
		}
		latitude.add(ll.getLatitude());
		longitude.add(ll.getLongitude());
	}

	public List<Double> longitude() {
		return this.longitude;
	}

	public List<Double> latitude() {
		return this.latitude;
	}

	public boolean hasIncompleteLocation() {
		return (longitude.size() > 0 || latitude.size() > 0);
	}

	public boolean hasLocation() {
		return (longitude.size() > 0 && latitude.size() > 0);
	}

	public Location getLocation() {
		return this.getLocation(false);
	}

	public Location getLocation(boolean removeOutliers) {
		if (this.hasLocation()) {
			if (removeOutliers) {
				SpatialOutlierDetector sod = new SpatialOutlierDetector();
				sod.detectWgs84Outliers(longitude, true);
				sod.detectWgs84Outliers(latitude, false);
			}

			Double minLon = Collections.min(longitude);
			Double maxLon = Collections.max(longitude);
			Double minLat = Collections.min(latitude);
			Double maxLat = Collections.max(latitude);
			return new Location(minLon, maxLon, minLat, maxLat);
		} else {
			return null;
		}
	}

}
