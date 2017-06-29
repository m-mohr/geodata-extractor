
package de.lutana.geodataextractor;

import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Location;
import java.io.File;
import static org.junit.Assert.assertEquals;

public class PublicationTest {

	private GeodataExtractor instance;
	protected String folder = "./test-docs/";

	public PublicationTest() {
		this.instance = new GeodataExtractor();
	}
	
	protected void runTest(String file, Location location) {
		File documentFile = new File(this.folder, file);
		Document result = this.instance.runSingle(documentFile);
		// ToDo: How to check for similarity of bboxes?
		assertEquals(location, result.getLocation());
	}

	@org.junit.Test
	public void testPublications() {
		runTest("germany.html", new Location(5.98865807458, 47.3024876979, 15.0169958839, 54.983104153));
	}
	
}
