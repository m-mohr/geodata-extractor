package de.lutana.geodataextractor.entity;

import java.io.File;

/**
 * Defines a figure, containing an optional caption, a graphic and an optional document context.
 * 
 * @author Matthias Mohr
 */
public class Figure {
	
	private String caption;
	private File graphic;
	private File document;
	private String documentContext;
	
	/**
	 * An empty figure.
	 */
	public Figure() {
		this(null, "");
	}
	
	/**
	 * An empty figure relating to a document.
	 * 
	 * @param document 
	 */
	public Figure(File document) {
		this(document, "");
	}
	
	/**
	 * An empty figure relating more spcifically to a document.
	 * 
	 * @param document 
	 * @param context
	 * @see Figure.setDocumentContext()
	 */
	public Figure(File document, String context) {
		this.document = document;
		this.documentContext = context;
		this.caption = "";
	}

	/**
	 * Returns the caption.
	 * 
	 * @return the caption
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * Sets the caption.
	 * 
	 * This should be plain text, all markup should be removed.
	 * As we don't exactly know where this comes from, the program doesn't try to
	 * make it plain text.
	 * 
	 * @param caption the caption to set
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * Returns the graphic file.
	 * 
	 * @return the graphic
	 * @see Figure.setGraphic()
	 */
	public File getGraphic() {
		return graphic;
	}

	/**
	 * Sets the graphic file.
	 * 
	 * Could be an jpeg image or an svg file for examle.
	 * 
	 * This is usually a file extracted from the original document in a 
	 * temporary folder. Don't expect it to be available forever.
	 * 
	 * @param graphic the graphic to set
	 */
	public void setGraphic(File graphic) {
		this.graphic = graphic;
	}

	/**
	 * Returns the file where the figure has been taken from, e.g. the PDF file.
	 * 
	 * @return the document
	 */
	public File getDocument() {
		return document;
	}

	/**
	 * Sets the file where the figure has been taken from, e.g. the PDF file.
	 * 
	 * @param document the document to set
	 */
	public void setDocument(File document) {
		this.document = document;
	}

	/**
	 * Sets a string containing more information about the figures "context"
	 * in the document, e.g. the page number, line number or similar.
	 * 
	 * @return the documentContext
	 */
	public String getDocumentContext() {
		return documentContext;
	}

	/**
	 * Returns a string containing more information about the figures "context"
	 * in the document, e.g. the page number, line number or similar.
	 * 
	 * @param documentContext the documentContext to set
	 */
	public void setDocumentContext(String documentContext) {
		this.documentContext = documentContext;
	}
	
}
