package de.lutana.geodataextractor.entity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Matthias Mohr
 */
public class FigureCollection extends ArrayList<Figure> {
	
	public void save(File folder) {
		Iterator<Figure> it = this.iterator();
		while (it.hasNext()) {
			try {
				it.next().save(folder);
			} catch(IOException ex) {
				// ToDo: Better logging
				ex.printStackTrace();
			}
		}
	}
	
}
