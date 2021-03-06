package de.lutana.geodataextractor;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.fileparser.HtmlParser;
import de.lutana.geodataextractor.fileparser.Parser;
import de.lutana.geodataextractor.fileparser.ParserFactory;
import de.lutana.geodataextractor.fileparser.PdfParser;
import de.lutana.geodataextractor.strategy.DefaultStrategy;
import de.lutana.geodataextractor.strategy.Strategy;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * @author Matthias Mohr
 */
public class Config {
	
	private static boolean fastOcrModeEnabled = true;
	
	public static boolean isOcrFastModeEnabled() {
		return fastOcrModeEnabled;
	}
	
	public static void enableFastOcrMode(boolean enableFastOcrMode) {
		fastOcrModeEnabled = enableFastOcrMode;
	}
	
	public static File getTempFolder(String subFolderName) {
		File folder = new File("./temp/" + subFolderName);
		if (!folder.exists()) {
			folder.mkdirs();
			folder.deleteOnExit();
		}
		return folder;
	}
	
	public static Strategy getStrategy() {
		return new DefaultStrategy();
	}
	
	public static Parser[] getParsers() {
		return new Parser[] {
			new HtmlParser(),
			new PdfParser()
		};
	}
	
	public static String getTestUrl(Location expected, Location result, File file) throws UnsupportedEncodingException {
		String url = "http://giv-project8.uni-muenster.de/study-app/test.php?" + 
				"&eminlon=" + expected.getMinX() + "&emaxlon=" + expected.getMaxX() + "&eminlat=" + expected.getMinY() + "&emaxlat=" + expected.getMaxY() + 
				"&rminlon=" + result.getMinX() + "&rmaxlon=" + result.getMaxX() + "&rminlat=" + result.getMinY() + "&rmaxlat=" + result.getMaxY();
		if (file != null) {
			String path = file.getName();
			ParserFactory pf = new ParserFactory();
			if (!pf.hasParser(file)) {
				path = file.getParentFile().getName() + File.separator + file.getName();
			}
			url += "&file=" + URLEncoder.encode(path, "UTF-8");
		}
		return url;
	}
	
}
