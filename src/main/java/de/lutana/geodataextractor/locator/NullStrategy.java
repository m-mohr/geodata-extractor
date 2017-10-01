package de.lutana.geodataextractor.locator;

import de.lutana.geodataextractor.entity.Document;

public class NullStrategy implements Strategy {

	/**
	 * Doesn't perform any task.
	 * 
	 * Could be useful to only parse the document, but avoid location detection.
	 * 
	 * @param document
	 * @param page
	 * @return 
	 */
	@Override
	public boolean execute(Document document, Integer page) {
		return true;
	}

	@Override
	public void shutdown() {}
	
}
