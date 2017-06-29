package de.lutana.geodataextractor.entity;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import de.lutana.geodataextractor.util.FileExtension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Defines a figure, containing an optional caption, a graphic and an optional document context.
 * 
 * @author Matthias Mohr
 */
public class Figure {
	
	private String caption;
	private File graphic;
	private Document document;
	private Location location;
	private String index;
	private Integer pageNo;
	
	/**
	 * An empty figure relating to a document.
	 * 
	 * @param document 
	 * @param graphic
	 * @param index
	 */
	public Figure(Document document, File graphic, String index) {
		this.document = document;
		this.index = index;
		this.pageNo = 1;
		this.graphic = graphic;
		this.caption = "";
		this.location = null;
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
	public Document getDocument() {
		return document;
	}

	/**
	 * Sets the file where the figure has been taken from, e.g. the PDF file.
	 * 
	 * @param document the document to set
	 */
	public void setDocument(Document document) {
		this.document = document;
	}

	/**
	 * Returns the figure name, which is basically an unique index for the figure in it's document.
	 * 
	 * @return the index
	 */
	public String getIndex() {
		return index;
	}

	/**
	 * Sets the figure name, which is basically an unique index for the figure in it's document.
	 * 
	 * @param index the index to set
	 */
	public void setIndex(String index) {
		this.index = index;
	}

	/**
	 * Returns the page number containing the figure.
	 * 
	 * @return the page
	 */
	public Integer getPageNo() {
		return pageNo;
	}

	/**
	 * Sets the page number containing the figure. Defaults to 1.
	 * 
	 * @param page the page to set
	 */
	public void setPageNo(Integer page) {
		if (page == null) {
			page = 1;
		}
		this.pageNo = page;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}
	
	/**
	 * Saves all data to the specified folder.
	 * 
	 * Make sure the specified directory exists!
	 * 
	 * @param folder
	 * @throws IOException
	 */
	public void save(File folder) throws IOException {
		File destGraphic = new File(folder, graphic.getName());
		Files.copy(graphic.toPath(), destGraphic.toPath(), StandardCopyOption.REPLACE_EXISTING);

		File destMetadata = new File(folder, FileExtension.replace(graphic.getName(), "json"));
		JsonFactory factory = new JsonFactory();
		JsonGenerator g = factory.createGenerator(destMetadata, JsonEncoding.UTF8);
		g.writeStartObject();
		g.writeStringField("document", this.document.getFile().getName());
		g.writeStringField("page", this.pageNo.toString());
		g.writeStringField("index", this.index);
		g.writeStringField("caption", this.caption);
		g.writeStringField("graphic", this.graphic.getName());
		g.writeEndObject();
		g.close();
	}
	
}
