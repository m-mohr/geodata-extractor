package de.lutana.geodataextractor.detector.coordinates;

import de.lutana.geodataextractor.util.GeoTools;
import java.util.Objects;
import uk.me.jstott.jcoord.LatLng;

public class Coordinate {
	
	private final Double latitude;
	private final Double longitude;
	private final int beginMatch;
	private final int endMatch;
	
	public Coordinate(LatLng ll, int beginMatch, int endMatch) {
		this(ll.getLatitude(), ll.getLongitude(), beginMatch, endMatch);
	}
	
	public Coordinate(Double lat, Double lon, int beginMatch, int endMatch) {
		this.latitude = lat;
		this.longitude = lon;
		this.beginMatch = beginMatch;
		this.endMatch = endMatch;
	}
	
	public Coordinate(Double lat, Double lon) {
		this.latitude = lat;
		this.longitude = lon;
		this.beginMatch = -1;
		this.endMatch = -1;
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
		if (!(other instanceof Coordinate)) {
			return false;
		}
		Coordinate oc = (Coordinate) other;
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
