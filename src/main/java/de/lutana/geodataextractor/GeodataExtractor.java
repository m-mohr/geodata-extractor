package de.lutana.geodataextractor;

import de.lutana.geodataextractor.detector.DefaultStrategy;
import de.lutana.geodataextractor.detector.Strategy;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.fileparser.Parser;
import de.lutana.geodataextractor.fileparser.ParserFactory;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Main class that controlls the location extraction from documents.
 * 
 * @author Matthias Mohr
 */
public class GeodataExtractor {
	
	private Set<Document> documents;
	private Strategy strategy;
	private ParserFactory parserFactory;
	private boolean saveFigures;
	
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
		this.documents = new HashSet<>();
		this.strategy = strategy;
		this.parserFactory = new ParserFactory();
		this.saveFigures = false;
	}
	
	/**
	 * Executes the GeodataExtractor and detects the locations for the specified documents.
	 * 
	 * @return Map containing documents and their locations on success, null on failure (e.g. no strategy specified).
	 */
	public Set<Document> run() {
		if (this.strategy == null) {
			return null;
		}
		
		for(Document doc : this.documents) {
			// ToDo: Put this in a thread?
			this.runDocument(doc);
		}

		return this.documents;
	}
	
	public Document runSingle(File file) {
		if (this.strategy == null) {
			return null;
		}

		if (this.parserFactory.hasParser(file)) {
			Document doc = new Document(file);
			if (this.runDocument(doc)) {
				return doc;
			}
		}
		return null;
	}
	
	protected boolean runDocument(Document doc) {
		try {
			Parser parser = this.parserFactory.getParser(doc.getFile());
			parser.parse(doc);
			if (canSaveFigures()) {
				doc.save();
			}
			return this.strategy.execute(doc);
		} catch (Exception e) {
			e.printStackTrace(); // ToDo: Better logging
			return false;
		}
	}
	
	/**
	 * Returns a list of documents.
	 * 
	 * @return the documents
	 */
	public Set<Document> getDocuments() {
		return this.documents;
	}
	
	/**
	 * Adds a file if a parser exists for it.
	 * 
	 * @param file file to add
	 * @return Newly created Document object if added, null if not
	 * @see de.lutana.geodataextractor.fileparser.ParserFactory
	 */
	public Document addDocument(File file) {
		if (this.parserFactory.hasParser(file)) {
			Document doc = new Document(file);
			this.documents.add(doc);
			return doc;
		}
		return null;
	}

	/**
	 * @param files the documents to set
	 * @return number of added documents
	 */
	public int setFiles(Collection<File> files) {
		int added = 0;
		for(File file : files) {
			if (this.addDocument(file) != null) {
				added++;
			}
		}
		return added;
	}
	
	/**
	 * Adds all documents from a folder (non-recursive) that can be parsed.
	 * 
	 * @param file
	 * @return 
	 * @see de.lutana.geodataextractor.fileparser.ParserFactory
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

	/**
	 * @return the save
	 */
	public boolean canSaveFigures() {
		return saveFigures;
	}

	/**
	 * @param saveFigures the save to set
	 */
	public void enableSaveFigures(boolean saveFigures) {
		this.saveFigures = saveFigures;
	}
	
}
