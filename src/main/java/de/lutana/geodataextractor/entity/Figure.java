package de.lutana.geodataextractor.entity;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
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
public class Figure extends Locatable {
	
	protected String caption;
	protected Graphic graphic;
	protected Document document;
	protected String index;
	protected Integer page;
	
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
		this.page = 1;
		this.caption = "";
		this.setGraphicFile(graphic);
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
	 * Returns the graphic.
	 * 
	 * @return the graphic
	 */
	public Graphic getGraphic() {
		return this.graphic;
	}

	/**
	 * Returns the graphic file.
	 * 
	 * @return the graphic
	 * @see Figure.setGraphic()
	 */
	public File getGraphicFile() {
		return this.graphic.getFile();
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
	public final void setGraphicFile(File graphic) {
		this.graphic = new Graphic(graphic);
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
	public Integer getPage() {
		return page;
	}

	/**
	 * Sets the page number containing the figure. Defaults to 1.
	 * 
	 * @param page the page to set
	 */
	public void setPage(Integer page) {
		if (page == null) {
			page = 1;
		}
		this.page = page;
	}

	/**
	 * Loads all figure data from disk.
	 * 
	 * @return
	 */
	public boolean load() {
		File jsonFile = new File(FileExtension.replace(getGraphicFile().getAbsolutePath(), "json"));
		if (!jsonFile.exists()) {
			return false;
		}
		
		try {
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(jsonFile);
			while(!parser.isClosed()){
				JsonToken jsonToken = parser.nextToken();
				if(JsonToken.FIELD_NAME.equals(jsonToken)){
					String fieldName = parser.getCurrentName();
					jsonToken = parser.nextToken();
					if(fieldName.equals("page")){
						this.page = parser.getValueAsInt();
					}
					else if (fieldName.equals("index")){
						this.index = parser.getValueAsString();
					}
					else if (fieldName.equals("caption")){
						this.caption = parser.getValueAsString();
					}
				}
			}
			return true;
		} catch(IOException ex) {
			ex.printStackTrace();
			return false;
		}
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
		File graphicFile = getGraphicFile();
		File destGraphic = new File(folder, graphicFile.getName());
		if (graphicFile.exists()) {
			Files.copy(graphicFile.toPath(), destGraphic.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		File destMetadata = new File(folder, FileExtension.replace(graphicFile.getName(), "json"));
		if (destMetadata.exists()) {
			return;
		}
		JsonFactory factory = new JsonFactory();
		try (JsonGenerator g = factory.createGenerator(destMetadata, JsonEncoding.UTF8)) {
			g.writeStartObject();
			g.writeStringField("document", this.document.getFile().getName());
			g.writeStringField("page", this.page.toString());
			g.writeStringField("index", this.index);
			g.writeStringField("caption", this.caption);
			g.writeStringField("graphic", graphicFile.getName());
			g.writeEndObject();
		}
	}
	
	@Override
	public String toString() {
		return "Figure " + this.index + " (page " + this.page.toString() + ")";
	}
	
}
