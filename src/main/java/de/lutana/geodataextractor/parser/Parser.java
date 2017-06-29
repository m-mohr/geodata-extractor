package de.lutana.geodataextractor.parser;

import de.lutana.geodataextractor.entity.Document;

/**
 * Interface for new document parsers that extract figures from documents.
 * 
 * New Parsers can be added to the default parsers in Config.getParsers().
 * 
 * @author Matthias Mohr
 * @see de.lutana.geodataextractor.Config.getParsers()
 */
public interface Parser {
	
	/**
	 * Returns a list of file extensions that can be parsed by this parser.
	 * 
	 * Specify each extension without a leading dot.
	 * 
	 * @return List of valid file extensions
	 */
	public String[] getExtensions();
	
	/**
	 * Parses figures from documents.
	 * 
	 * @param document
	 * @throws de.lutana.geodataextractor.parser.ParserException
	 */
	public void parse(Document document) throws ParserException;
	
}
