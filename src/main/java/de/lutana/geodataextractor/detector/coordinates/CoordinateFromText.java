package de.lutana.geodataextractor.detector.coordinates;

import de.lutana.geodataextractor.util.GeoTools;
import java.util.Objects;
import uk.me.jstott.jcoord.LatLng;

public class CoordinateFromText {
	
	private final Double latitude;
	private final Double longitude;

	private final String text;
	private final int beginMatch;
	private final int endMatch;
	
	public CoordinateFromText(CoordinateFromText c) {
		this(c.latitude, c.longitude, c.text, c.beginMatch, c.endMatch);
	}
	
	public CoordinateFromText(LatLng ll, String text, int beginMatch, int endMatch) {
		this(ll.getLatitude(), ll.getLongitude(), text, beginMatch, endMatch);
	}
	
	public CoordinateFromText(Double lat, Double lon, String text, int beginMatch, int endMatch) {
		this.latitude = lat;
		this.longitude = lon;
		this.text = text;
		this.beginMatch = beginMatch;
		this.endMatch = endMatch;
	}
	
	public CoordinateFromText(Double lat, Double lon) {
		this.latitude = lat;
		this.longitude = lon;
		this.text = null;
		this.beginMatch = -1;
		this.endMatch = -1;
	}

	/**
	 * @return the value
	 */
	public Double getLatitude() {
		return this.latitude;
	}
	
	public com.vividsolutions.jts.geom.Coordinate toJtsCoordinate() {
		return new com.vividsolutions.jts.geom.Coordinate(this.longitude, this.latitude);
	}

	/**
	 * @return the value
	 */
	public Double getLongitude() {
		return this.longitude;
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

}
