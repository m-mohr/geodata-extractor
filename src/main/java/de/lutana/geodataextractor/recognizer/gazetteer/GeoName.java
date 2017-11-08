package de.lutana.geodataextractor.recognizer.gazetteer;

import com.vividsolutions.jts.geom.Coordinate;
import de.lutana.geodataextractor.entity.Location;

public class GeoName {

	/**
	 * The name of the feature (default language is en, others available are de,
	 * es, fr, ru, zh)
	 */
	private String name;
	/**
	 * The display name of the feature representing the hierarchy, if available
	 * in English
	 */
	private String displayName;
	/**
	 * All other available and distinct names
	 */
	private String[] alternativeNames;
	/**
	 * The OSM ID of the feature (mostly for identifying test results)
	 */
	private String osmId;
	/**
	 * The OSM type of the feature (node, way, relation)
	 */
	private String osmType;
	/**
	 * (class) The class of the feature e.g. boundary
	 */
	private String featureClass;
	/**
	 * The type of the feature e.g. administrative
	 */
	private String type;
	/**
	 * Rank from 1-30 ascending, 1 being the highest. Calculated with the type
	 * and class of the feature.
	 */
	private int placeRank;
	/**
	 * Importance of the feature, ranging [0.0-1.0], 1.0 being the most
	 * important.
	 */
	private float importance;
	/**
	 * The name of the city of the feature, if it has one
	 */
	private String city;
	/**
	 * The name of the county of the feature, if it has one
	 */
	private String county;
	/**
	 * The name of the state of the feature, it it has one
	 */
	private String state;
	/**
	 * The name of the country of the feature
	 */
	private String country;
	/**
	 * The ISO-3166 2-letter country code of the feature
	 */
	private String countryCode;
	/**
	 * The bounding box (WGS84) of the feature (built from west, south, east,
	 * north)
	 */
	private Location location;
	/**
	 * Score from the index.
	 */
	private double resultScore;

	public GeoName() {
		this.alternativeNames = new String[] {};
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the alternativeNames
	 */
	public String[] getAlternativeNames() {
		return alternativeNames;
	}

	/**
	 * @param alternativeNames the alternativeNames to set
	 */
	public void setAlternativeNames(String[] alternativeNames) {
		this.alternativeNames = alternativeNames;
	}

	/**
	 * @return the osmId
	 */
	public String getOsmId() {
		return osmId;
	}

	/**
	 * @param osmId the osmId to set
	 */
	public void setOsmId(String osmId) {
		this.osmId = osmId;
	}

	/**
	 * @return the osmType
	 */
	public String getOsmType() {
		if (osmType == null && osmId != null) {
			char c = osmId.charAt(0);
			switch(c) {
				case 'n':
					return "node";
				case 'w':
					return "way";
				case 'r':
					return "relation";
				case 'c':
					return "custom";
			}
		}
		return osmType;
	}

	/**
	 * @param osmType the osmType to set
	 */
	public void setOsmType(String osmType) {
		this.osmType = osmType;
	}

	/**
	 * @return the featureClass
	 */
	public String getFeatureClass() {
		return featureClass;
	}

	/**
	 * @param featureClass the featureClass to set
	 */
	public void setFeatureClass(String featureClass) {
		this.featureClass = featureClass;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the placeRank
	 */
	public int getPlaceRank() {
		return placeRank;
	}

	/**
	 * @param placeRank the placeRank to set
	 */
	public void setPlaceRank(int placeRank) {
		this.placeRank = placeRank;
	}

	/**
	 * @return the importance
	 */
	public float getImportance() {
		return importance;
	}

	/**
	 * @param importance the importance to set
	 */
	public void setImportance(float importance) {
		this.importance = importance;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the county
	 */
	public String getCounty() {
		return county;
	}

	/**
	 * @param county the county to set
	 */
	public void setCounty(String county) {
		this.county = county;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the countryCode
	 */
	public String getCountryCode() {
		return countryCode;
	}

	/**
	 * @param countryCode the countryCode to set
	 */
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public Coordinate getCentroid() {
		return this.location.centre();
	}

	/**
	 * @return the resultScore
	 */
	public double getResultScore() {
		return resultScore;
	}

	/**
	 * @param resultScore the resultScore to set
	 */
	public void setResultScore(double resultScore) {
		this.resultScore = resultScore;
	}

	@Override
	public String toString() {
		String str = "[" + osmId + "] ";
		str += (displayName == null || displayName.isEmpty()) ? name : displayName;
		if (location != null) {
			str += " @ ";
			str += location.toString();
		}
		return str;
	}
	
}
