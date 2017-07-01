/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lutana.geodataextractor;

import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

/**
 *
 * @author Matthias
 */
public class GeodataExtractorTest {

	/**
	 * Test of run method, of class GeodataExtractor.
	 * @throws java.io.IOException
	 */
	@org.junit.Test
	public void testRun() throws IOException {
		File folder = new File("./test-docs/");
		GeodataExtractor instance = new GeodataExtractor();
		instance.enableSaveFigures(true);
		instance.setFolder(folder);
		System.out.println("Using the following documents from folder " + folder.getCanonicalPath() + ":");
		for(Document doc : instance.getDocuments()) {
			System.out.println(doc.getFile().getCanonicalPath());
		}

		Set<Document> expResult = new HashSet<>();
		// ToDo: This path works on Windows only
		// Improve this tests a lot!
		Document doc = new Document(new File("test-docs/germany.html"));
		Figure figure = doc.addFigure(null, null);
		figure.setLocation(new Location(5.98865807458, 47.3024876979, 15.0169958839, 54.983104153));
		expResult.add(doc);
		
		Set<Document> result = instance.run();
		assertEquals(expResult, result);
	}
	
}
