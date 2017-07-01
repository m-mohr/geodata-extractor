package de.lutana.geodataextractor.entity;

import de.lutana.geodataextractor.util.GeoTools;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Matthias Mohr
 */
public class LocationCollection implements Located, SortedSet<Location>, Comparator<Location> {

	private final SortedSet<Location> data;
	private double weight;

	public LocationCollection() {
		this.data = new TreeSet<>((Comparator<Location>) this);
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
		// ToDo: Merge bboxes and calculate the most probable place based on all locations (remember: they might intersect!)
		return this.first();
	}

	@Override
	public Comparator<? super Location> comparator() {
		return this.data.comparator();
	}

	@Override
	public SortedSet<Location> subSet(Location fromElement, Location toElement) {
		return this.data.subSet(fromElement, toElement);
	}

	@Override
	public SortedSet<Location> headSet(Location toElement) {
		return this.data.headSet(toElement);
	}

	@Override
	public SortedSet<Location> tailSet(Location fromElement) {
		return this.data.tailSet(fromElement);
	}

	@Override
	public Location first() {
		return this.data.first();
	}

	@Override
	public Location last() {
		return this.data.last();
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
