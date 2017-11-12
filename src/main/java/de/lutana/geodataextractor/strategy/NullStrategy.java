package de.lutana.geodataextractor.strategy;

import de.lutana.geodataextractor.entity.Document;

/**
 * Doesn't perform any task.
 * 
 * Could be useful to only parse the document, but avoid location detection.
 * 
 * @author Matthias
 */
public class NullStrategy extends AbstractStrategy {

	/**
	 * Doesn't perform any task.
	 * 
	 * @param document
	 * @param page
	 * @return 
	 */
	@Override
	public boolean execute(Document document, Integer page) {
		return true;
	}
	
}
