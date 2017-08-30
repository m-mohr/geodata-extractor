package api;

import de.lutana.geodataextractor.GeodataExtractor;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.util.GeoTools;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;

public class GeoToolsTest {

	@org.junit.Test
	public void testRun() throws IOException {
		double expected = 66.6667;
		double rounded = GeoTools.roundLatLon(66.6666666666);
		assertEquals(expected, rounded, 0.00009);
	}
	
	
}
