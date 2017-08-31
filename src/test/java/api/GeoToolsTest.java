package api;

import de.lutana.geodataextractor.util.GeoTools;
import java.io.IOException;
import static org.junit.Assert.assertEquals;

public class GeoToolsTest {

	@org.junit.Test
	public void testRun() throws IOException {
		double expected = 66.6667;
		double rounded = GeoTools.roundLatLon(66.6666666666);
		assertEquals(expected, rounded, 0.00009);
	}
	
	
}
