package de.lutana.geodataextractor.entity.locationresolver;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class HeatmapResolver extends SmallSetResolver {

	@Override
	public Location resolve(LocationCollection locations) {
		Location l = super.resolve(locations);
		if (l != null) {
			return l;
		}

		TreeMap<Double, EnvScorer> treeX = new TreeMap<>();
		TreeMap<Double, EnvScorer> treeY = new TreeMap<>();
		
		for(Location location : locations) {
			treeX.put(location.getMinX(), new EnvScorer());
			treeX.put(location.getMaxX(), new EnvScorer());
			treeY.put(location.getMinY(), new EnvScorer());
			treeY.put(location.getMaxY(), new EnvScorer());
		}
		
		double maxScore = -Double.MAX_VALUE;
		for(Location location : locations) {
			double score = location.getScore();
			if (score > maxScore) {
				maxScore = score;
			}
			SortedMap<Double, EnvScorer> subX = treeX.subMap(location.getMinX(), true, location.getMaxX(), true);
			for(EnvScorer e : subX.values()) {
				e.sum += score;
				e.count++;
			}
			SortedMap<Double, EnvScorer> subY = treeY.subMap(location.getMinY(), true, location.getMaxY(), true);
			for(EnvScorer e : subY.values()) {
				e.sum += score;
				e.count++;
			}
		}
		
		double[] boundsX = this.getMinMaxValuesFromTree(treeX, maxScore);
		double[] boundsY = this.getMinMaxValuesFromTree(treeY, maxScore);
		l = new Location(boundsX[0], boundsX[1], boundsY[0], boundsY[1]);
		l.setProbability(1);
		l.setWeight(1);
		return l;
	}
	
	private double[] getMinMaxValuesFromTree(TreeMap<Double, EnvScorer> tree, double threshold) {
		Double min = Double.MAX_VALUE;
		Double max = -Double.MAX_VALUE;
		for(Map.Entry<Double, EnvScorer> e : tree.entrySet()) {
			EnvScorer env = e.getValue();
			if (env.sum >= threshold || env.count > 1) {
				double value = e.getKey();
				if (value > max) {
					max = value;
				}
				if (value < min) {
					min = value;
				}
			}
		}
		
		return new double[] {min, max};
	}

	private class EnvScorer {
		public double sum = 0;
		public int count = 0;
	}
	
}
