package de.lutana.geodataextractor.parser;

import de.lutana.geodataextractor.entity.FigureCollection;
import java.io.File;

/**
 * Parses figures from HTML documents.
 * 
 * @author Matthias Mohr
 */
public class HtmlParser implements Parser {

	/**
	 * Parses contents from figure tags in HTML5 documents.
	 * 
	 * Other tags and (X)HTML blocks are currently not supported.
	 * 
	 * @param publication File referencing an HTML based publication
	 * @return
	 * @see http://wiki.selfhtml.org/wiki/HTML/Textstrukturierung/figure
	 */
	@Override
	public FigureCollection parse(File publication) {
		return null;
	}

	@Override
	public String[] getExtensions() {
		return new String[] {".htm", ".html"};
	}
	
}
