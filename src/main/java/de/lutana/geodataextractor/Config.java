package de.lutana.geodataextractor;

import de.lutana.geodataextractor.parser.HtmlParser;
import de.lutana.geodataextractor.parser.Parser;
import de.lutana.geodataextractor.parser.PdfParser;
import java.io.File;

/**
 *
 * @author Matthias Mohr
 */
public class Config {
	
	public static File getTempFolder() {
		File folder = new File("./temp/");
		if (!folder.exists()) {
			folder.mkdirs();
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
