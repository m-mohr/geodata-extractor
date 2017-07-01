package de.lutana.geodataextractor.entity;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Represents a bounding box.
 * 
 * @author Matthias Mohr
 */
public class Location extends Envelope {
	
	private double probability = 1;
	private double weight = 1;
	
	public Location(double x1, double x2, double y1, double y2) {
		super(x1, x2, y1, y2);
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
	
}
