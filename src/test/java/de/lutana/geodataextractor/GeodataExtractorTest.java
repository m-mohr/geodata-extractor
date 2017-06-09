/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lutana.geodataextractor;

import de.lutana.geodataextractor.detector.Strategy;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.parser.ParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
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
		System.out.println(folder.getCanonicalPath());
		Map<File, LocationCollection> expResult = null;
		Map<File, LocationCollection> result = instance.run();
		assertEquals(expResult, result);
	}
	
}
