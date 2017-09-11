package de.lutana.geodataextractor.entity;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Represents a document.
 *
 * @author Matthias Mohr
 */
public class Document implements Located {

	protected File file;
	protected String title;
	protected String description;
	protected FigureCollection figures;

	public Document(File file) {
		this.file = file;
		this.title = "";
		this.description = "";
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
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 * @return 
	 */
	public boolean setTitle(String title) {
		if (title == null) {
			return false;
		}

		title = title.trim();
		if (title.matches("\\S+\\b\\s+\\b.+")) {
			this.title = title;
			return true;
		}
		return false;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 * @return 
	 */
	public boolean setDescription(String description) {
		if (description == null) {
			return false;
		}

		description = description.trim();
		if (!description.isEmpty() && !description.equalsIgnoreCase("abstract")) {
			this.description = description;
			return true;
		}
		return false;
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
						this.title = parser.getValueAsString();
					}
					else if (fieldName.equals("abstract")){
						this.description = parser.getValueAsString();
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
				g.writeStringField("title", this.title);
				g.writeStringField("abstract", this.description);
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
