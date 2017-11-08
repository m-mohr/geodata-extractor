package de.lutana.geodataextractor.recognizer.coordinates;

import de.lutana.geodataextractor.util.GeoTools;
import java.security.InvalidParameterException;
import java.util.Objects;
import uk.me.jstott.jcoord.LatLng;

public class CoordinateFromText {
	
	private Double latitude;
	private Double longitude;

	private final String text;
	private final int beginMatch;
	private final int endMatch;
	
	private double probability;
	
	public CoordinateFromText(CoordinateFromText c) {
		this(c.latitude, c.longitude, c.text, c.beginMatch, c.endMatch, c.probability);
	}
	
	public CoordinateFromText(LatLng ll, String text, int beginMatch, int endMatch, double probability) {
		this(ll.getLatitude(), ll.getLongitude(), text, beginMatch, endMatch, probability);
	}
	
	public CoordinateFromText(Double lat, Double lon, String text, int beginMatch, int endMatch, double probability) {
		this.latitude = lat;
		this.longitude = lon;
		this.text = text;
		this.beginMatch = beginMatch;
		this.endMatch = endMatch;
		this.setProbability(probability);
	}
	
	public CoordinateFromText(Double lat, Double lon) {
		this.latitude = lat;
		this.longitude = lon;
		this.text = null;
		this.beginMatch = -1;
		this.endMatch = -1;
		this.probability = 0;
	}
	
	public com.vividsolutions.jts.geom.Coordinate toJtsCoordinate() {
		return new com.vividsolutions.jts.geom.Coordinate(this.longitude, this.latitude);
	}
	
	public boolean isEmpty() {
		return (latitude == null && longitude == null);
	}

	/**
	 * @return the value
	 */
	public Double getLatitude() {
		return this.latitude;
	}

	/**
	 * @return the value
	 */
	public Double getLongitude() {
		return this.longitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Start position in the text where the coordinate has been found.
	 * 
	 * Returns -1 if no position is available.
	 * 
	 * @return the beginMatch
	 */
	public int getBeginMatch() {
		return beginMatch;
	}

	/**
	 * End position in the text where the coordinate has been found.
	 * 
	 * Returns -1 if no position is available.
	 * 
	 * @return the endMatch
	 */
	public int getEndMatch() {
		return endMatch;
	}

	/**
	 * @return the probability
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * @param probability the probability to set
	 */
	public final void setProbability(double probability) {
		if (probability < 0 || probability > 1.0) {
			throw new InvalidParameterException("Probability needs to be between 0 and 1");
		}
		this.probability = probability;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof CoordinateFromText)) {
			return false;
		}
		CoordinateFromText oc = (CoordinateFromText) other;
		return ((oc.longitude == null && this.longitude == null) || (this.longitude != null && GeoTools.roundLatLon(oc.longitude) == GeoTools.roundLatLon(this.longitude)))
				&& ((oc.latitude == null && this.latitude == null) || (this.latitude != null && GeoTools.roundLatLon(oc.latitude) == GeoTools.roundLatLon(this.latitude)));
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 19 * hash + Objects.hashCode(this.latitude);
		hash = 19 * hash + Objects.hashCode(this.longitude);
		return hash;
	}

	@Override
	public String toString() {
		return "("+ (this.latitude == null ? "?" : this.latitude)  +"," + (this.longitude == null ? "?" : this.longitude) + ")";
	}
	
	public static class UnknownOrientation extends CoordinateFromText {
	
		public UnknownOrientation(Double coordinate, String text, int beginMatch, int endMatch) {
			super(coordinate, coordinate, text, beginMatch, endMatch, 0.01);
		}
		
	}

}
