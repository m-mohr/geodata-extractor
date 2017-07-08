package de.lutana.geodataextractor.entity;

import de.lutana.geodataextractor.util.FileExtension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Matthias Mohr
 */
public class FigureCollection extends ArrayList<Figure> implements Located {

	/**
	 * Loads all figure data from disk.
	 * 
	 * @param folder
	 * @param document
	 * @return
	 */
	public boolean load(File folder, Document document) {
		boolean success = true;
		File[] files = folder.listFiles(new FileExtension.Filter("png"));
		for (File graphicFile : files) {
			Figure figure = new Figure(document, graphicFile, null);
			if (figure.load()) {
				this.add(figure);
			}
			else {
				success = false;
			}
		}
		return success;
	}
	
	public void save(File folder) {
		Iterator<Figure> it = this.iterator();
		while (it.hasNext()) {
			try {
				it.next().save(folder);
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public LocationCollection getFigureLocations() {
		LocationCollection collection = new LocationCollection();
		for(Figure f : this) {
			Location l = f.getLocation();
			if (l != null) {
				collection.add(l);
			}
		}
		return collection;
	}
	
	@Override
	public Location getLocation() {
		return this.getFigureLocations().getLocation();
	}
	
}
