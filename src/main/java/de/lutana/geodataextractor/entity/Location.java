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

	public Location(double x1, double x2, double y1, double y2) {
		super(x1, x2, y1, y2);
	}

	public Location(double x, double y) {
		super(x, x, y, y);
	}

	public Location(Envelope e) {
		super(e);
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

}
