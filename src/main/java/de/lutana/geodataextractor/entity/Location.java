package de.lutana.geodataextractor.entity;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import de.lutana.geodataextractor.util.GeoTools;
import java.security.InvalidParameterException;
import java.util.Comparator;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.util.Precision;

/**
 * Represents a bounding box.
 *
 * @author Matthias Mohr
 */
public class Location extends Envelope implements Clusterable {

	protected double probability = 0;
	protected double weight = 0;

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

	@Override
	public double[] getPoint() {
		Coordinate c = this.centre();
		return new double[] {c.x, c.y};
	}

	public double getProbability() {
		return this.probability;
	}

	public void setProbability(double probability) {
		if (probability < 0 || probability > 1.0) {
			throw new InvalidParameterException("Probability (" + probability + ") needs to be between 0 and 1");
		}
		this.probability = probability;
	}

	public double getWeight() {
		return this.weight;
	}

	public void setWeight(double weight) {
		if (weight < 0 || weight > 1.0) {
			throw new InvalidParameterException("Probability needs to be between 0 and 1");
		}
		this.weight = weight;
	}

	public double getScore() {
		return this.weight * this.probability;
	}
	
	public boolean isPoint() {
		return (GeoTools.roundLatLon(getWidth()) == 0 && GeoTools.roundLatLon(getHeight()) == 0);
	}
	
	public boolean isLine() {
		return (GeoTools.roundLatLon(getWidth()) == 0 ^ GeoTools.roundLatLon(getHeight()) == 0); // ^ = xor
	}
	
	public double getScoreWithPenalty() {
		double score = this.getScore();

		// Points and Lines get a penalty
		if (isPoint()) {
			score *= 0.5;
		}
		else if (isLine()) {
			score *= 0.75;
		}
		
		return score;
	}
	
	public void makeValid() {
		if (!this.isValid()) {
			this.setX(Math.max(-180, this.getMinX()), Math.min(180, this.getMaxX()));
			this.setY(Math.max(-90, this.getMinY()), Math.min(90, this.getMaxY()));
		}
	}
	
	public boolean isValid() {
		return (this.getMinX() >= -180 && this.getMaxX() <= 180 && this.getMinY() >= -90 && this.getMaxY() <= 90);
	}
	
	public static ScoreComparator getScoreComparator() {
		return new ScoreComparator();
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
	public String toString() {
		return super.toString() + "{" + Precision.round(this.getScoreWithPenalty(), 2) + "/" + Precision.round(this.getProbability(), 2) + "}";
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
	
	public static class ScoreComparator implements Comparator<Location> {
		@Override
		public int compare(Location o1, Location o2) {
			Double w1 = o1.getScore();
			return w1.compareTo(o2.getScore());
		}
	}

}
