package de.lutana.geodataextractor.parser;

import de.lutana.geodataextractor.entity.FigureCollection;
import java.io.File;

/**
 *
 * @author Matthias Mohr
 */
public interface Parser {
	
	public String[] getExtensions();
	
	public FigureCollection parse(File publication);
	
}
