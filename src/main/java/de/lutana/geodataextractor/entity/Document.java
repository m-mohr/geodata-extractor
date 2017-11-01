package de.lutana.geodataextractor.entity;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a document.
 *
 * @author Matthias Mohr
 */
public class Document implements Located {

	protected File file;
	protected Map<String, String> texts;
	protected FigureCollection figures;

	public Document(File file) {
		this.file = file;
		this.texts = new HashMap<>();
		this.figures = new FigureCollection();
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @param type
	 * @return the title
	 */
	public String getText(String type) {
		return this.texts.getOrDefault(type, "");
	}

	/**
	 * @param type
	 * @param text the text to set
	 * @return 
	 */
	public boolean setText(String type, String text) {
		if (text == null) {
			this.texts.remove(type);
			return false;
		}

		this.texts.put(type, text.trim());
		return true;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return this.texts.get("title");
	}

	/**
	 * @param title the title to set
	 * @return 
	 */
	public boolean setTitle(String title) {
		if (title != null) {
			title = title.trim();
			if (!title.matches("\\S+\\b\\s+\\b.+")) {
				return false;
			}
		}
		
		return this.setText("title", title);
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.getText("description");
	}

	/**
	 * @param description the description to set
	 * @return 
	 */
	public boolean setDescription(String description) {
		if (description != null) {
			description = description.trim();
			if (description.equalsIgnoreCase("abstract")) {
				return false;
			}
		}
		return this.setText("description", description);
	}

	/**
	 * @return the figures
	 */
	public FigureCollection getFigures() {
		return figures;
	}

	/**
	 * Adds a new figure.
	 *
	 * @param graphic
	 * @param index
	 * @return
	 */
	public Figure addFigure(File graphic, String index) {
		Figure figure = new Figure(this, graphic, index);
		this.figures.add(figure);
		return figure;
	}

	/**
	 * Adds a new figure with document context.
	 *
	 * @param graphic
	 * @param index
	 * @param pageNo
	 * @return
	 */
	public Figure addFigure(File graphic, String index, Integer pageNo) {
		Figure figure = this.addFigure(graphic, index);
		figure.setPage(pageNo);
		return figure;
	}

	/**
	 * @param figures the figures to set
	 */
	public void setFigures(FigureCollection figures) {
		this.figures = figures;
	}

	/**
	 * Loads all figure data from disk.
	 * 
	 * @return
	 */
	public boolean load() {
		File folder = new File(this.file.getAbsolutePath() + "-figures");
		if (!folder.exists()) {
			return false;
		}
		
		try {
			File jsonFile = new File(folder, "document.json");
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(jsonFile);
			while(!parser.isClosed()){
				JsonToken jsonToken = parser.nextToken();
				if(JsonToken.FIELD_NAME.equals(jsonToken)){
					String fieldName = parser.getCurrentName();
					jsonToken = parser.nextToken();
					if(fieldName.equals("title")){
						this.setTitle(parser.getValueAsString());
					}
					else if (fieldName.equals("abstract")){
						this.setDescription(parser.getValueAsString());
					}
				}
			}
			
			return figures.load(folder, this);
		} catch(IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	/**
	 * Saves all figure data to disk.
	 */
	public void save() {
		File folder = new File(this.file.getAbsolutePath() + "-figures");
		if (folder.exists()) {
			return;
		}

		folder.mkdirs();
		
		try {
			File destMetadata = new File(folder, "document.json");
			JsonFactory factory = new JsonFactory();
			try (JsonGenerator g = factory.createGenerator(destMetadata, JsonEncoding.UTF8)) {
				g.writeStartObject();
				g.writeStringField("document", this.file.getName());
				g.writeStringField("title", this.getTitle());
				g.writeStringField("abstract", this.getDescription());
				g.writeEndObject();
			}
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		
		figures.save(folder);
	}

	@Override
	public Location getLocation() {
		return this.figures.getLocation();
	}

	private String getPath() {
		String path;
		try {
			path = this.file.getCanonicalPath();
		} catch(IOException e) {
			path = this.file.getPath();
		}
		return path;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof Document) {
			return (this.getPath().equals(((Document) other).getPath()));
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 23 * hash + Objects.hashCode(this.getPath());
		return hash;
	}
	
	@Override
	public String toString() {
		return this.file.getName();
	}

}
