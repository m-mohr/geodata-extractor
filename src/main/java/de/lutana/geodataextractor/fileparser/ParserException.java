package de.lutana.geodataextractor.fileparser;

/**
 * Exception thrown when problems with Figure parsing occur.
 * 
 * @author Matthias
 */
public class ParserException extends Exception {

	/**
	 * Creates a new instance of <code>ParserException</code> with a detail message.
	 * 
	 * @param message
	 */
	public ParserException(String message) {
		super(message);
	}
	
	/**
	 * Creates a new instance of <code>ParserException</code> from another Exception.
	 * 
	 * @param e
	 */
	public ParserException(Exception e) {
		super(e);
	}

}
