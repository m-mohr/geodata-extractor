package de.lutana.geodataextractor.entity;

import de.lutana.geodataextractor.util.GeoTools;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Matthias Mohr
 */
public class LocationCollection implements Located, Collection<Location>, Comparator<Location> {

	private final List<Location> data;
	private double weight;

	public LocationCollection() {
		this.data = new ArrayList<>();
		this.resetWeight();
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public final void resetWeight() {
		this.weight = 1;
	}

	@Override
	public int compare(Location o1, Location o2) {
		Double w1 = o1.getScore();
		return w1.compareTo(o2.getScore());
	}
	
	@Override
	public Location getLocation() {
		return GeoTools.union(this.data);
	}
	
	public Location getMostLikelyLocation() {
		if (this.isEmpty()) {
			return null;
		}
		List<Location> cloned = new ArrayList<>(this.data);
		Collections.sort(cloned);
		// ToDo: Merge bboxes and calculate the most probable place based on all locations (remember: they might intersect!)
		return cloned.get(0);
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
