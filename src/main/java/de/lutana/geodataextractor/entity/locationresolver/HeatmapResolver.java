package de.lutana.geodataextractor.entity.locationresolver;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class HeatmapResolver extends SmallSetResolver {
	
	private Double threshold;
	private THRESHOLD type;
	
	public HeatmapResolver() {
		this.type = THRESHOLD.MAX_SCORE;
		this.threshold = null;
	}
	
	public HeatmapResolver(THRESHOLD type) {
		this.type = type;
		this.threshold = null;
	}
	
	public HeatmapResolver(double threshold) {
		this.type = THRESHOLD.CUSTOM;
		this.threshold = threshold;
	}

	@Override
	public Location resolve(LocationCollection locations) {
		Location l = super.resolve(locations);
		if (l != null || locations.isEmpty()) {
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

		if (this.type == THRESHOLD.MAX_SCORE) {
			threshold = -Double.MAX_VALUE;
		}
		else if (this.type == THRESHOLD.AVERAGE) {
			this.threshold = 0d;
		}
		for(Location location : locations) {
			double score = location.getScore();
			if (this.type == THRESHOLD.MAX_SCORE && score > this.threshold) {
				this.threshold = score;
			}
			else if (this.type == THRESHOLD.AVERAGE) {
				this.threshold += score;
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

		if (type == THRESHOLD.AVERAGE) {
			this.threshold = this.threshold / locations.size();
		}

		double[] boundsX = this.getMinMaxValuesFromTree(treeX, this.threshold);
		double[] boundsY = this.getMinMaxValuesFromTree(treeY, this.threshold);
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
			if (env.sum >= threshold) {
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
	
	public enum THRESHOLD {
		AVERAGE,
		MAX_SCORE,
		CUSTOM
	}
	
}
