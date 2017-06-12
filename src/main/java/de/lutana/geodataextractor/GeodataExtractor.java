package de.lutana.geodataextractor;

import de.lutana.geodataextractor.detector.DefaultStrategy;
import de.lutana.geodataextractor.detector.Strategy;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.parser.Parser;
import de.lutana.geodataextractor.parser.ParserFactory;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Main class that controlls the location extraction from files.
 * 
 * @author Matthias Mohr
 */
public class GeodataExtractor {
	
	private Map<File, LocationCollection> files;
	private Strategy strategy;
	private ParserFactory parserFactory;
	
	/**
	 * Creates an instance using the DefaultStrategy.
	 * 
	 * @see de.lutana.geodataextractor.detector.DefaultStrategy
	 */
	public GeodataExtractor() {
		this(new DefaultStrategy());
	}

	/**
	 * Creates an instance using the specified strategy.
	 * 
	 * @param strategy
	 */
	public GeodataExtractor(Strategy strategy) {
		this.files = new HashMap<>();
		this.strategy = strategy;
		this.parserFactory = new ParserFactory();
	}
	
	/**
	 * Executes the GeodataExtractor and detects the locations for the specified files.
	 * 
	 * @return Map containing files and their locations on success, null on failure (e.g. no strategy specified).
	 */
	public Map<File, LocationCollection> run() {
		if (this.strategy == null) {
			return null;
		}
		
		for(File file : this.files.keySet()) {
			// ToDo: Put this in a thread
			try {
				Parser parser = this.parserFactory.getParser(file);
				FigureCollection figures = parser.parse(file);
				if (figures != null) {
					LocationCollection locations = this.strategy.execute(figures);
					this.files.replace(file, locations);
				}
			} catch (Exception e) {
				e.printStackTrace(); // ToDo: Better logging
			}
		}

		return this.files;
	}
	
	/**
	 * Returns a list of files.
	 * 
	 * @return the files
	 */
	public Map<File, LocationCollection> getFileLocations() {
		return this.files;
	}
	
	/**
	 * Returns a list of files.
	 * 
	 * @return the files
	 */
	public Set<File> getFiles() {
		return this.files.keySet();
	}
	
	/**
	 * Adds a file if a parser exists for it.
	 * 
	 * @param file file to add
	 * @return true if added, false if not
	 * @see de.lutana.geodataextractor.parser.ParserFactory
	 */
	public boolean addFile(File file) {
		if (this.parserFactory.hasParser(file)) {
			this.files.put(file, null);
			return true;
		}
		return false;
	}

	/**
	 * @param files the files to set
	 * @return number of added files
	 */
	public int setFiles(Collection<File> files) {
		int added = 0;
		for(File file : files) {
			if (this.addFile(file)) {
				added++;
			}
		}
		return added;
	}
	
	/**
	 * Adds all files from a folder (non-recursive) that can be parsed.
	 * 
	 * @param file
	 * @return 
	 * @see de.lutana.geodataextractor.parser.ParserFactory
	 */
	public int setFolder(File file) {
		if (!file.isDirectory()) {
			return 0;
		}
		
		return this.setFiles(Arrays.asList(file.listFiles()));
	}

	/**
	 * Returns the Strategy to be used for location detection.
	 * 
	 * @return the strategy
	 */
	public Strategy getStrategy() {
		return this.strategy;
	}

	/**
	 * Sets the Strategy to be used for location detection.
	 * 
	 * @param strategy the strategy to set
	 */
	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}
	
	/**
	 * Returns the ParserFactory used.
	 * 
	 * @return 
	 */
	public ParserFactory getParserFactory() {
		return this.parserFactory;
	}
	
}
