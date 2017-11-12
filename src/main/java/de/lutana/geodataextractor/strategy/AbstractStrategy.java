package de.lutana.geodataextractor.strategy;

import de.lutana.geodataextractor.entity.Document;

public abstract class AbstractStrategy implements Strategy {

	@Override
	public abstract boolean execute(Document document, Integer page);

	@Override
	public void shutdown() {}
	
}
