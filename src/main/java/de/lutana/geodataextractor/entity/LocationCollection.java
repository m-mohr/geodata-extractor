package de.lutana.geodataextractor.entity;

import de.lutana.geodataextractor.entity.locationresolver.HeatmapResolver;
import de.lutana.geodataextractor.entity.locationresolver.JackardIndexResolver;
import de.lutana.geodataextractor.entity.locationresolver.LocationResolver;
import de.lutana.geodataextractor.entity.locationresolver.UnionResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthias Mohr
 */
public class LocationCollection implements Located, Collection<Location> {

	private final List<Location> data;

	public LocationCollection() {
		this.data = new ArrayList<>();
	}

	public LocationCollection(LocationCollection collection) {
		this.data = new ArrayList<>(collection.data);
	}
	
	@Override
	public Location getLocation() {
		return this.getMostLikelyLocation();
	}
	
	public Location getUnifiedLocation() {
		return this.resolveLocation(new UnionResolver());
	}
	
	public Location getMostLikelyLocation() {
		return this.getMostLikelyLocationUsingJackardIndex();
	}

	public Location getMostLikelyLocationUsingHeatmap() {
		return this.resolveLocation(new HeatmapResolver());
	}
	
	public Location getMostLikelyLocationUsingJackardIndex() {
		return this.resolveLocation(new JackardIndexResolver());
	}
	
	public Location resolveLocation(LocationResolver resolver) {
		return resolver.resolve(this);
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
