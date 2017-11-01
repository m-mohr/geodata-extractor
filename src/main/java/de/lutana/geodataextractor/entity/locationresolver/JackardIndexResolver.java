package de.lutana.geodataextractor.entity.locationresolver;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.util.GeoTools;
import org.apache.commons.math3.util.Precision;
import org.slf4j.LoggerFactory;

public class JackardIndexResolver extends SmallSetResolver {

	@Override
	public Location resolve(LocationCollection locations) {
		Location location = super.resolve(locations);
		if (location != null) {
			return location;
		}

		int count = locations.size();
		// ToDo: Pretty slow O(n²) - is there a more elegant solution?
		// ToDo: Would it be a better solution to have a heatmap and get the location by a threshold?
		double[] scores = new double[count];
		for(int i = 0; i < count; i++) {
			Location l = locations.get(i);
			double otherScores = 0;
			for(Location l2 : locations) {
				if (l == l2) {
					continue;
				}

				// Points/Lines are calculated differntly as jackard always returns someting near to 0 for them.
				// If they are contained in the area slightly increase the score otherwirse decrease it.
				double l2Weight;
				if (l2.isPoint() || l2.isLine()) {
					l2Weight = l.contains(l2) ? 0.2 : 0.0;
				}
				else {
					// The more the rectangles have in common the better - "commonness" based on jackard
					l2Weight = GeoTools.calcJaccardIndex(l, l2);
				}
				l2Weight /= count - 1;
				otherScores += l2Weight * l2.getScore();
			}
			// simple points or lines get a penalty
			scores[i] = (l.getScoreWithPenalty() + otherScores) / 2;

			LoggerFactory.getLogger(getClass()).debug("[score: " + Precision.round(scores[i], 2) + "/" + Precision.round(otherScores, 2) + "] " + l);
		}
		
		double maxValue = -1;
		int maxIndex = -1;
		for(int i = 0; i < scores.length; i++) {
			if (scores[i] > maxValue) {
				maxValue = scores[i];
				maxIndex = i;
			}
		}
		
		if (maxIndex == -1) {
			return null;
		}

		return locations.get(maxIndex);
	}
	
}
