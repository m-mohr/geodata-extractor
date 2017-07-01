package de.lutana.geodataextractor;

import de.lutana.geodataextractor.fileparser.HtmlParser;
import de.lutana.geodataextractor.fileparser.Parser;
import de.lutana.geodataextractor.fileparser.PdfParser;
import java.io.File;

/**
 *
 * @author Matthias Mohr
 */
public class Config {
	
	public static File getTempFolder(String subFolderName) {
		File folder = new File("./temp/" + subFolderName);
		if (!folder.exists()) {
			folder.mkdirs();
			folder.deleteOnExit();
		}
		return folder;
	}
	
	public static Parser[] getParsers() {
		return new Parser[] {
			new HtmlParser(),
			new PdfParser()
		};
	}
	
}
