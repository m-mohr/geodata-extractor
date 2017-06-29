package de.lutana.geodataextractor.entity;

import com.vividsolutions.jts.geom.Envelope;

/**
 *
 * @author Matthias Mohr
 */
public class Location extends Envelope {
	
	public Location(double x1, double x2, double y1, double y2) {
		super(x1, x2, y1, y2);
	}
	
	public Location(Envelope e) {
		super(e);
	}
	
}
