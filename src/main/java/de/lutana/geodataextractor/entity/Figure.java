package de.lutana.geodataextractor.entity;

import java.io.File;

/**
 *
 * @author Matthias Mohr
 */
public class Figure {
	
	private String caption;
	private File graphic;
	private String reference;
	
	public Figure() {
		
	}

	/**
	 * @return the caption
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * @param caption the caption to set
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * @return the graphic
	 */
	public File getGraphic() {
		return graphic;
	}

	/**
	 * @param graphic the graphic to set
	 */
	public void setGraphic(File graphic) {
		this.graphic = graphic;
	}

	/**
	 * @return the reference
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * @param reference the reference to set
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}
	
}
