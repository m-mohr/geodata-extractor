package de.lutana.geodataextractor.detector.coordinates;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.util.GeoTools;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

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
		List<Double> longitude = new ArrayList<>();
		List<Double> latitude = new ArrayList<>();
		double sum = 0;
		for(CoordinateFromText c : this) {
			Double lat = c.getLatitude();
			if (lat != null) {
				latitude.add(lat);
			}
			Double lon = c.getLongitude();
			if (lon != null) {
				longitude.add(lon);
			}
			sum += c.getProbability();
		}
		
		if (!longitude.isEmpty() && !latitude.isEmpty()) {
			Collections.sort(longitude);
			Collections.sort(latitude);

			Double minLon = longitude.get(0);
			Double maxLon = longitude.get(longitude.size() - 1);
			Double minLat = latitude.get(0);
			Double maxLat = latitude.get(latitude.size() - 1);
			Location l = new Location(minLon, maxLon, minLat, maxLat);
			l.setProbability(sum / this.size());
			return l;
		} else {
			return null;
		}
	}
	
	public void removeOutliers() {
		removeOutliers(true);
		removeOutliers(false);
	}

	/**
	 * Takes this list of spatial longitude or latitude values (WGS84) and tries to
	 * remove outliers from it.
	 * 
	 * Outliers are detected by calculating distances to (0,0) using the
	 * Vincenty formula. This takes axes into account and is therefore more
	 * accurate than Haversine formula. Median +/- 2 * standard deviation is used
	 * on the distances to detect outliers.
	 * 
	 * Only executed when there are more than 2 distinct values.
	 *
	 * @param isLongitude
	 */
	protected void removeOutliers(boolean isLongitude) {		
		// "Remove" duplicates
		TreeMap<Double, List<CoordinateFromText>> map = new TreeMap<>();
		for(CoordinateFromText coord : this) {
			Double value;
			if (isLongitude) {
				value = coord.getLongitude();
			}
			else {
				value = coord.getLatitude();
			}
			if (value == null) {
				continue;
			}
			List<CoordinateFromText> list = map.get(value);
			if (list == null) {
				list = new ArrayList<>();
				map.put(value, list);
			}
			list.add(coord);
		}
		if (map.size() < 3) {
			return;
		}

		// Calculate distance from each point to 0,0
		double[] distances = new double[map.size()];
		int i = 0;
		Set<Double> keys = map.keySet();
		for(Double value : keys) {
			if (isLongitude) {
				distances[i] = GeoTools.calcVincentyDistance(0, value, 0, 0) / 1000;
			}
			else {
				distances[i] = GeoTools.calcVincentyDistance(value, 0, 0, 0) / 1000;
			}
			i++;
		}

		// Calculate standard deviation
		StandardDeviation sdCalculator = new StandardDeviation(false);
		double sd = sdCalculator.evaluate(distances);
		double median = StatUtils.percentile(distances, 50);
		double lowerBorder = median - 2 * sd;
		double upperBorder = median + 2 * sd;

		i = 0;
		Collection<List<CoordinateFromText>> coordLists = map.values();
		for(List<CoordinateFromText> cList : coordLists) {
			double distance = distances[i];
			if (distance < lowerBorder  || distance > upperBorder) {
				for(CoordinateFromText c : cList) {
					if (isLongitude) {
						c.setLongitude(null);
					}
					else {
						c.setLatitude(null);
					}
					if (c.isEmpty()) {
						this.remove(c);
					}
				}
			}
			i++;
		}
	}

}
