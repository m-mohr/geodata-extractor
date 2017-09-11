package de.lutana.geodataextractor.entity;

import com.vividsolutions.jts.geom.Envelope;
import de.lutana.geodataextractor.util.GeoTools;

/**
 * Represents a bounding box.
 *
 * @author Matthias Mohr
 */
public class Location extends Envelope {

	protected double probability = 1;
	protected double weight = 1;

	public Location(double minLon, double maxLon, double minLat, double maxLat) {
		super(minLon, maxLon, minLat, maxLat);
	}

	public Location(double lon, double lat) {
		super(lon, lon, lat, lat);
	}

	public Location(Envelope e) {
		super(e);
	}
	
	public void setX(double min, double max) {
		this.init(min, max, this.getMinY(), this.getMaxY());
	}
	
	public void setY(double min, double max) {
		this.init(this.getMinX(), this.getMaxX(), min, max);
	}

	public double getProbability() {
		return this.probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public double getWeight() {
		return this.weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getScore() {
		return this.weight * this.probability;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Envelope)) {
			return false;
		}
		Envelope otherEnvelope = (Envelope) other;
		if (isNull()) {
			return otherEnvelope.isNull();
		}
		return GeoTools.roundLatLon(getMaxX()) == GeoTools.roundLatLon(otherEnvelope.getMaxX())
				&& GeoTools.roundLatLon(getMaxY()) == GeoTools.roundLatLon(otherEnvelope.getMaxY())
				&& GeoTools.roundLatLon(getMinX()) == GeoTools.roundLatLon(otherEnvelope.getMinX())
				&& GeoTools.roundLatLon(getMinY()) == GeoTools.roundLatLon(otherEnvelope.getMinY());
	}

	@Override
	public int hashCode() {
		int hash = 3;
		double v1 = GeoTools.roundLatLon(getMaxX());
		hash = 43 * hash + (int) (Double.doubleToLongBits(v1) ^ (Double.doubleToLongBits(v1) >>> 32));
		double v2 = GeoTools.roundLatLon(getMaxY());
		hash = 43 * hash + (int) (Double.doubleToLongBits(v2) ^ (Double.doubleToLongBits(v2) >>> 32));
		double v3 = GeoTools.roundLatLon(getMinX());
		hash = 43 * hash + (int) (Double.doubleToLongBits(v3) ^ (Double.doubleToLongBits(v3) >>> 32));
		double v4 = GeoTools.roundLatLon(getMinY());
		hash = 43 * hash + (int) (Double.doubleToLongBits(v4) ^ (Double.doubleToLongBits(v4) >>> 32));
		return hash;
	}

}
