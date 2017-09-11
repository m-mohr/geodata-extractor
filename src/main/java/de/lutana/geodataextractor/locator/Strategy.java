package de.lutana.geodataextractor.locator;

import de.lutana.geodataextractor.entity.Document;

/**
 * A strategy combines the detectors and the resulting location collections.
 * 
 * @author Matthias Mohr
 */
public interface Strategy {
	
	/**
	 * A strategy uses the parsed figures of a single file and detects locations
	 * for them with a certain strategy implemented here. It also combines all 
	 * locations collected to a single location collections.
	 * 
	 * If page is not null, only the selected page will be parsed and detected.
	 * 
	 * @param document
	 * @param page
	 * @return
	 */
	public boolean execute(Document document, Integer page);
	
}
