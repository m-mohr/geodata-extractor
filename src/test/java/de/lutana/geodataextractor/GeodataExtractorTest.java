/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lutana.geodataextractor;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 *
 * @author Matthias
 */
public class GeodataExtractorTest {
	
	public GeodataExtractorTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}

	/**
	 * Test of run method, of class GeodataExtractor.
	 * @throws java.io.IOException
	 */
	@org.junit.Test
	public void testRun() throws IOException {
		File folder = new File("./test-docs/");
		GeodataExtractor instance = new GeodataExtractor();
		instance.setFolder(folder);
		System.out.println("Using the following documents from folder " + folder.getCanonicalPath() + ":");
		for(File file : instance.getFiles()) {
			System.out.println(file.getCanonicalPath());
		}

		LocationCollection lc = new LocationCollection();
		lc.add(new Location(5.98865807458, 47.3024876979, 15.0169958839, 54.983104153));
		Map<File, LocationCollection> expResult = new HashMap<>();
		// ToDo: This path works on Windows only
		// Improve this tests a lot!
		File file = new File("test-docs/germany.html");
		expResult.put(file.getCanonicalFile(), lc);
		
		Map<File, LocationCollection> result = instance.run();
		assertEquals(expResult, result);
	}
	
}
