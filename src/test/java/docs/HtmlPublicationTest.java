package docs;

import de.lutana.geodataextractor.entity.Location;

public class HtmlPublicationTest extends BasePublicationTest {

	@org.junit.Test
	public void testDocument() {
		assertDocument(new Location(5.98865807458, 15.0169958839, 47.3024876979, 54.983104153), getDocument("germany.html"));
	}
	
}
