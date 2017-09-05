package de.lutana.geodataextractor.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class SpatialOutlierDetector {

	/**
	 * Takes a list of spatial longitude or latitude values (WGS84) and tries to
	 * remove outliers from the specified list. The specified list itself is modified!
	 * 
	 * Outliers are detected by calculating distances to (0,0) using the
	 * Vincenty formula. This takes axes into account and is therefore more
	 * accurate than Haversine formula. Median +/- 2 * standard deviation is used
	 * on the distances to detect outliers.
	 * 
	 * Only executed when there are more than 2 values.
	 * 
	 * Removed outliers are returned.
	 *
	 * @param list
	 * @param isLongitude true if list contains longitude values, false if list contains latitude values.
	 * @return
	 */
	public List<Double> detectWgs84Outliers(List<Double> list, boolean isLongitude) {
		List<Double> removed = new ArrayList<>();
		if (list.size() < 3) {
			return removed;
		}

		// Calculate distance from each point to 0,0
		double[] distances = new double[list.size()];
		for(int i = 0; i < list.size(); i++) {
			Double value = list.get(i);
			if (isLongitude) {
				distances[i] = GeoTools.calcVincentyDistance(0, value, 0, 0) / 1000;
			}
			else {
				distances[i] = GeoTools.calcVincentyDistance(value, 0, 0, 0) / 1000;
			}
		}

		// Calculate standard deviation
		StandardDeviation sdCalculator = new StandardDeviation(false);
		double sd = sdCalculator.evaluate(distances);
		double median = StatUtils.percentile(distances, 50);
		double lowerBorder = median - 2 * sd;
		double upperBorder = median + 2 * sd;
		
		for(int i = 0; i < list.size(); i++) {
			double entry = distances[i];
			if (entry < lowerBorder  || entry > upperBorder) {
				removed.add(list.get(i));
			}
		}
		list.removeAll(removed);

		return removed;
	}

}
