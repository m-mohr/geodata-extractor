package de.lutana.geodataextractor.parser;

import de.lutana.geodataextractor.Config;
import java.io.File;
import java.util.HashMap;

/**
 * Manages the parsers.
 *
 * @author Matthias Mohr
 */
public class ParserFactory {

	private HashMap<String, Parser> parser;

	/**
	 * Creates a new instance of the parser factory and adds the default parsers.
	 *
	 * @see ParserFactory.addDefaultParsers()
	 */
	public ParserFactory() {
		this.parser = new HashMap<>();
		this.addDefaultParsers();
	}
	
	/**
	 * Adds the default parsers from Config.getParsers().
	 * 
	 * @see de.lutana.geodataextractor.Config.getParsers()
	 */
	private void addDefaultParsers() {
		for (Parser p : Config.getParsers()) {
			this.addParser(p);
		}
	}

	/**
	 * Adds a parsers.
	 * 
	 * If there is already a parser registered covering the type of file
	 * spefied by the new parser the old parser is overridden.
	 * 
	 * @param p Parser to add
	 */
	public void addParser(Parser p) {
		String[] extensions = p.getExtensions();
		for (String extension : extensions) {
			this.parser.put(extension.toLowerCase(), p);
		}
	}

	/**
	 * Returns the suitable parser for the specified file or null if no parser
	 * could be found.
	 * 
	 * @param file
	 * @return 
	 */
	public Parser getParser(File file) {
		return this.parser.get(ParserFactory.getFileExtension(file));
	}

	/**
	 * Checks whether a suitable parser for the specified file exists or not.
	 * 
	 * @param file
	 * @return true if a suitable parser has been found, false if not.
	 */
	public boolean hasParser(File file) {
		return this.parser.containsKey(ParserFactory.getFileExtension(file));
	}

	/**
	 * Returns the file extension in lower case and without leading dot.
	 * 
	 * If no dot (= no file extension) is found an empty string is returned.
	 * 
	 * @param path
	 * @return 
	 */
	public static String getFileExtension(String path) {
		return ParserFactory.getFileExtension(new File(path));
	}

	/**
	 * Returns the file extension in lower case and without leading dot.
	 * 
	 * If no dot (= no file extension) is found an empty string is returned.
	 * 
	 * @param file
	 * @return 
	 */
	public static String getFileExtension(File file) {
		String fileName = file.getName();
		int i = fileName.lastIndexOf('.');
		if (i >= 0) {
			return fileName.substring(i + 1).toLowerCase();
		}
		return "";
	}

}
