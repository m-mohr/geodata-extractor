package de.lutana.geodataextractor.recognizer.gazetteer;

import de.lutana.geodataextractor.entity.Location;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.slf4j.LoggerFactory;

public class OsmNamesReader implements Iterator<GeoName> {
	
	public static final File DATA_FILE = new File("./planet-latest_geonames.tsv.gz");

	private BufferedReader reader;
	private GeoName current;
	private Map<String, String> countryCodeIdMapping;
	private Map<String, Integer> countryCodeRankMapping;
	
	public OsmNamesReader() throws IOException {
		this(DATA_FILE, true);
	}
	
	public OsmNamesReader(File file, boolean gzipped) throws IOException {
		this(gzipped ? new GZIPInputStream(new FileInputStream(file)) : new FileInputStream(file));
	}
	
	public OsmNamesReader(InputStream is) throws IOException {
		this.reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
		this.reader.readLine(); // Skip header
		this.countryCodeIdMapping = new HashMap<>();
		this.countryCodeRankMapping = new HashMap<>();
	}
	
	public Map<String, String> getCountryCodeMapping() {
		return countryCodeIdMapping;
	}
	
	public void makeCountryCodeMapping(GeoName gn) {
		String cc = gn.getCountryCode();
		if (cc == null || cc.isEmpty()) {
			return;
		}
		cc = cc.toUpperCase();
		
		Integer smallestRank = Integer.MAX_VALUE;
		if (countryCodeRankMapping.containsKey(cc)) {
			smallestRank = countryCodeRankMapping.get(cc);
		}

		int rank = gn.getPlaceRank();
		String type = gn.getType();
		String osmType = gn.getOsmType();
		String featureClass = gn.getFeatureClass();
		String state = gn.getState();
		String county = gn.getCounty();
		String city = gn.getCity();
		if (rank > 0 && rank < smallestRank && 
				type != null && type.equalsIgnoreCase("administrative") &&
				osmType != null && osmType.equalsIgnoreCase("relation") &&
				featureClass != null && featureClass.equalsIgnoreCase("boundary") &&
				(state == null || state.isEmpty()) &&
				(county == null || county.isEmpty()) &&
				(city == null || city.isEmpty())) {
			countryCodeRankMapping.put(cc, gn.getPlaceRank());
			countryCodeIdMapping.put(cc, gn.getOsmId());
		}
	}

	@Override
	public boolean hasNext() {
		try {
			String currentLine;
			while ((currentLine = this.reader.readLine()) != null) {
				current = this.parseLine(currentLine);
				if (current != null) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public GeoName next() {
		return current;
	}
	
	protected GeoName parseLine(String line) {
		if (line == null) {
			return null;
		}
		String[] data = line.split("\\t");
		if (data.length < 21) {
			LoggerFactory.getLogger(getClass()).debug("Skipped line: " + line);
			return null;
		}
		
		GeoName gn = new GeoName();
		gn.setName(data[0]);
		String[] altNames = data[1].split(",");
		if (altNames.length > 0 && !altNames[0].isEmpty()) {
			gn.setAlternativeNames(altNames);
		}
		gn.setOsmType(data[2]);
		gn.setOsmId(data[2].charAt(0) + data[3]);
		gn.setFeatureClass(data[4]);
		gn.setType(data[5]);
		if (!data[8].isEmpty()) {
			try {
				gn.setPlaceRank(Integer.parseInt(data[8]));
			} catch(NumberFormatException e) {}
		}
		if (!data[9].isEmpty()) {
			try {
				gn.setImportance(Float.parseFloat(data[9]));
			} catch(NumberFormatException e) {}
		}
		gn.setCity(data[11]);
		gn.setCounty(data[12]);
		gn.setState(data[13]);
		gn.setCountry(data[14]);
		gn.setCountryCode(data[15]);
		gn.setDisplayName(data[16]);
		if (!data[17].isEmpty() && !data[18].isEmpty() && !data[19].isEmpty() && !data[20].isEmpty()) {
			try {
				double west = Double.parseDouble(data[17]);
				double south = Double.parseDouble(data[18]);
				double east = Double.parseDouble(data[19]);
				double north = Double.parseDouble(data[20]);
				Location l = new Location(west, east, south, north);
				gn.setLocation(l);
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}
		}
		
		this.fixOsmNames(gn);
		
		this.makeCountryCodeMapping(gn);
		
		return gn;
	}
	
	private void fixOsmNames(GeoName gn) {
		String osmId = gn.getOsmId();
		// Importance for Greece is too low in data set: https://github.com/OSMNames/OSMNames/issues/148
		if (osmId.equals("r192307")) {
			gn.setImportance(0.9f);
		}
		// Add pacific as alternative name to the pacific ocean
		else if (osmId.equals("n305640005")) {
			gn.addAlternativeName("Pacific");
		}
		// Add atlantic as alternative name to the atlantic ocean
		else if (osmId.equals("n305640306")) {
			gn.addAlternativeName("Atlantic");
		}
		// and so on...
		else if (osmId.equals("n305639726")) {
			gn.addAlternativeName("North Atlantic");
		}
		else if (osmId.equals("n305640288")) {
			gn.addAlternativeName("South Atlantic");
		}
		else if (osmId.equals("n305640027")) {
			gn.addAlternativeName("North Pacific");
		}
		else if (osmId.equals("n305640292")) {
			gn.addAlternativeName("South Pacific");
		}
		// River Amazon should also be Amazonas
		else if (osmId.equals("w27912146")) { 
			gn.addAlternativeName("Amazonas");
			gn.addAlternativeName("Amazon");
		}
		// Hamburg has an empty bbox
		else if (osmId.equals("n565666208")) { 
			gn.setLocation(new Location(8.105329, 10.32528, 53.395111, 54.027683));
		}
	}
	
	public void close() {
		if (this.reader != null) {
			try {
				this.reader.close();
			} catch (IOException ex) {}
		}
	}
	
}
