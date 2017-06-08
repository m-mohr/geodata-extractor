package de.lutana.geodataextractor.parser;

import de.lutana.geodataextractor.entity.FigureCollection;
import java.io.File;

/**
 * Parses figures from PDF files.
 * 
 * @author Matthias Mohr
 */
public class PdfParser implements Parser {

	/**
	 * Parses contents from PDF documents.
	 * 
	 * This parser uses the PDFFigures 2.0 package from Allenai.
	 * Some figures might not be detected correctly, which is limited by PDFFigures.
	 * 
	 * @param publication File referencing an PDF based publication
	 * @return
	 * @see http://pdffigures2.allenai.org/
	 */
	@Override
	public FigureCollection parse(File publication) {
		return null; // ToDo
	}

	/**
	 * Returns a list of file extensions that can be parsed by this parser.
	 * 
	 * Specify each extension without a leading dot.
	 * 
	 * @return List of valid file extensions
	 */
	@Override
	public String[] getExtensions() {
		return new String[] {".pdf"};
	}
	
}
