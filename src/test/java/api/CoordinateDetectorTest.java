package api;

import de.lutana.geodataextractor.recognizer.CoordinateTextRecognizer;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class CoordinateDetectorTest {
	
    @org.junit.runners.Parameterized.Parameter(0)
    public String pattern;
    @org.junit.runners.Parameterized.Parameter(1)
    public Location location;
	
	private final CoordinateTextRecognizer detector = new CoordinateTextRecognizer();

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		Object[][] list = new Object[][] {
			{"This is a dumb test for coordinates like 12°N.", null},
			// WGS84 (D, DM, DMS)
			{"12° N, 12°S, 90° 30' W, 1°30'40.5\"E", new Location(-90.5, 1.51125, -12d, 12d)},
			{"Pick me up at 52°N 1°30'40.5\"E.", new Location(1.51125, 52d)},
			// Universal Transverse Mercator (UTM)
			{"17T 630084 4833438", new Location(-79.387139, 43.642567)},
			{"You can find it at UTM 57 X 450793 8586116.", new Location(156.9876, 77.3450)},
			// OS (Ordnance Survey)
			{"NY 9545 9776", new Location(-2.07162170, 55.27392830)},
			// Military Grid Reference System (MGRS)
			{"4QFJ 12345 67890", new Location(-157.9160812, 21.4097967)},
		};
		return Arrays.asList(list);
    }

	@org.junit.Test
    public void testDetect() {
		LocationCollection result = new LocationCollection();
		detector.recognize(this.pattern, result, 1);

		if (result.isEmpty()) {
			Assert.assertNull(this.location);
		}
		else {
			Assert.assertEquals(1, result.size());
			Assert.assertEquals(this.location, result.get(0));
		}
    }
	
}
