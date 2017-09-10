package de.lutana.geodataextractor.detector;

import com.bericotech.clavin.ClavinException;
import com.bericotech.clavin.GeoParser;
import com.bericotech.clavin.GeoParserFactory;
import com.bericotech.clavin.gazetteer.GeoName;
import com.bericotech.clavin.resolver.ResolvedLocation;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.util.ClavinIndexDirectoryBuilder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClavinTextDetector implements TextDetector {

	private static GeoParser geoParser = null;
	private static final String[] DEFAULT_GAZETTEER_FILES = new String[]{"./allCountries.txt"};
	private static final String DEFAULT_INDEX_DIRECTORY = "./IndexDirectory";

	public static GeoParser getGeoParser() throws ClavinException {
		if (geoParser == null) {
			createIndex();
			geoParser = GeoParserFactory.getDefault(DEFAULT_INDEX_DIRECTORY);
		}
		return geoParser;
	}

	public static void createIndex() {
		File idir = new File(DEFAULT_INDEX_DIRECTORY);
		if (idir.exists()) {
			return;
		}

		List<File> gazetteerFiles = new ArrayList<File>();
		for (String gp : DEFAULT_GAZETTEER_FILES) {
			File gf = new File(gp);
			if (gf.isFile() && gf.canRead()) {
				gazetteerFiles.add(gf);
			}
		}
		if (gazetteerFiles.isEmpty()) {
			return;
		}

		try {
			new ClavinIndexDirectoryBuilder(false).buildIndex(idir, gazetteerFiles, null);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void detect(String text, LocationCollection locations) {
		try {
			List<ResolvedLocation> resolvedLocations = getGeoParser().parse(text);

			// Display the ResolvedLocations found for the location names
			for (ResolvedLocation resolvedLocation : resolvedLocations) {
				GeoName geoname = resolvedLocation.getGeoname();
				Location l = new Location(geoname.getLongitude(), geoname.getLongitude(), geoname.getLatitude(), geoname.getLatitude());
				l.setProbability(resolvedLocation.getConfidence());
				locations.add(l);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
