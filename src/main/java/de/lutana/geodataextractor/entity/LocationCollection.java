package de.lutana.geodataextractor.entity;

import de.lutana.geodataextractor.util.GeoTools;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.util.Precision;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthias Mohr
 */
public class LocationCollection implements Located, Collection<Location> {

	private final List<Location> data;
	private double weight;

	public LocationCollection() {
		this.data = new ArrayList<>();
		this.resetWeight();
	}

	public LocationCollection(LocationCollection collection) {
		this.data = new ArrayList<>(collection.data);
		this.weight = collection.weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public final void resetWeight() {
		this.weight = 1;
	}
	
	@Override
	public Location getLocation() {
		Location union = GeoTools.union(this.data);
		if (union != null) {
			union.setWeight(1);
			double scoreSum = 0;
			for(Location l : this.data) {
				scoreSum += l.getScore();
			}
			union.setProbability(scoreSum / this.data.size());
		}
		return union;
	}
	
	public Location getMostLikelyLocation() {
		int count = this.data.size();
		switch (count) {
			case 0:
				return null;
			case 1:
				return this.data.get(0);
			case 2:
				Location l1 = this.data.get(0);
				double l1Score = l1.getScoreWithPenalty();
				Location l2 = this.data.get(1);
				double l2Score = l2.getScoreWithPenalty();
				if (Math.abs(l1Score - l2Score) > 0.2) {
					// Decide to use only the higher scored location if score is somehow far different
					return l1Score > l2Score ? l1 : l2;
				}
				else {
					// Return union - we can't decide
					return this.getLocation();
				}
		}

		// ToDo: Pretty slow O(n²) - is there a more elegant solution?
		double[] scores = new double[count];
		for(int i = 0; i < count; i++) {
			Location l = this.data.get(i);
			double otherScores = 0;
			for(Location l2 : this.data) {
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

		return this.data.get(maxIndex);
	}
	
	public Location get(int index) {
		return this.data.get(index);
	}

	@Override
	public int size() {
		return this.data.size();
	}

	@Override
	public boolean isEmpty() {
		return this.data.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return this.data.contains(o);
	}

	@Override
	public Iterator<Location> iterator() {
		return this.data.iterator();
	}

	@Override
	public Object[] toArray() {
		return this.data.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.data.toArray(a);
	}

	@Override
	public boolean add(Location o) {
		if (o == null) {
			throw new NullPointerException();
		}
		if (!o.isValid()) {
			LoggerFactory.getLogger(getClass()).debug("Ignored invalid location " + o);
			return false;
		}
		o.setWeight(this.weight);
		return this.data.add(o);
	}

	@Override
	public boolean remove(Object o) {
		return this.data.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.data.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Location> c) {
		boolean changed = false;
		Iterator<? extends Location> it = c.iterator();
		while (it.hasNext()) {
			if (this.add(it.next())) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.data.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return this.data.removeAll(c);
	}

	@Override
	public void clear() {
		this.data.clear();
	}

}
