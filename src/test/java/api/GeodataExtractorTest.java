package api;

import de.lutana.geodataextractor.GeodataExtractor;
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
		File documentFile = new File("test-docs/germany.html");

		Set<Document> expResult = new HashSet<>();
		Document doc = new Document(documentFile);
		Figure figure = doc.addFigure(null, null);
		figure.setLocation(new Location(5.98865807458, 47.3024876979, 15.0169958839, 54.983104153));
		expResult.add(doc);

		GeodataExtractor instance = new GeodataExtractor();
		instance.addDocument(documentFile);
		Set<Document> result = instance.run();

		assertEquals(expResult, result);
	}
	
}
