package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Graphic;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Matthias
 */
public class CoordinateDetector implements TextDetector {

	private static final Pattern wgsPattern = Pattern.compile("(-?\\d+(?:[\\.,‚’_-]\\d+)?)[°o](?:\\s*(\\d+(?:[\\.,‚’_-]\\d+)?)['‘’‚,`´\\|])?(?:\\s*(\\d+(?:[\\.,‚’_-]\\d+)?)(?:\"|''|“|”|„))?\\s*(N|S|W|E)", Pattern.CASE_INSENSITIVE);

	@Override
	public void detect(String text, LocationCollection locations) {
		CoordinatePairs cp = this.parseWgsCoordinates(text);
		Location l = cp.getLocation();
		if (l != null) {
			locations.add(l);
		}
	}
	
	public CoordinatePairs parseWgsCoordinates(String text) {
		CoordinatePairs cp = new CoordinatePairs();
		Matcher m = wgsPattern.matcher(text);
		while (m.find()) {
			String degStr = m.group(1).replace(',', '.').replace('‚', '.').replace('’', '.').replace('-', '.').replace('_', '.');
			Double deg = Double.parseDouble(degStr);
			
			String minStr = m.group(2);
			Double min = 0d;
			if (minStr != null && !minStr.isEmpty()) {
				minStr = minStr.replace(',', '.').replace('‚', '.').replace('’', '.').replace('-', '.').replace('_', '.');
				min = Double.parseDouble(minStr);
			}
			
			String secStr = m.group(3);
			Double sec = 0d;
			if (secStr != null && !secStr.isEmpty()) {
				secStr = secStr.replace(',', '.').replace('‚', '.').replace('’', '.').replace('-', '.').replace('_', '.');
				sec = Double.parseDouble(secStr);
			}
			
			Double coord = deg + (min / 60d) + (sec / 3600d);
			
			String sigStr = m.group(4);
			if (sigStr.equalsIgnoreCase("S") || sigStr.equalsIgnoreCase("W")) {
				coord = -1 * coord;
			}
			
			// ToDO: Remove outliers
			if (sigStr.equalsIgnoreCase("S") || sigStr.equalsIgnoreCase("N")) {
				cp.latitude().add(coord);
			}
			else {
				cp.longitude().add(coord);
			}
		}
		return cp;
	}
	
	public class CoordinatePairs {
		
		private final List<Double> longitude = new ArrayList<>();
		private final List<Double> latitude = new ArrayList<>();
		
		public List<Double> longitude() {
			return this.longitude;
		}
		
		public List<Double> latitude() {
			return this.latitude;
		}
		
		public Location getLocation() {
			if (longitude.size() > 0 && latitude.size() > 0) {
				Double minLon = Collections.min(longitude);
				Double maxLon = Collections.max(longitude);
				Double minLat = Collections.min(latitude);
				Double maxLat = Collections.max(latitude);
				return new Location(minLon, maxLon, minLat, maxLat);
			}
			else {
				return null;
			}
		}
		
	}

}
