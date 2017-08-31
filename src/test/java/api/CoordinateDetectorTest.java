package api;

import de.lutana.geodataextractor.detector.CoordinateDetector;
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
	
	private final CoordinateDetector detector = new CoordinateDetector();

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		Object[][] list = new Object[][] {
			// WGS84 (D, DM, DMS)
			{"12° N, 12°S, 90° 30' W, 1°30'40.5\"E", new Location(-90.5, 1.51125, -12d, 12d)},
			{"Pick me up at 52°N 1°30'40.5\"E.", new Location(1.51125, 52d)},
			// Universal Transverse Mercator (UTM)
			{"17T 630084 4833438", new Location(-79.387139, 43.642567)},
			{"17N 630084 4833438", new Location(-79.387139, 43.642567)},
			{"31N 166021 0", new Location(0.0, 0.0)},
			{"30 N 808084 14385", new Location(-0.2324, 0.13)},
			{"34 G 683473 4942631", new Location(23.3545, -45.6456)},
			{"25 L 404859 8588690", new Location(-33.8765, -12.765)},
			{"02 C 506346 1057742", new Location(-170.654, -80.5434)},
			{"08 Q 453580 2594272", new Location(-135.4545, 23.4578)},
			{"You can find it at UTM 57 X 450793 8586116.", new Location(156.9876, 77.3450)},
			// OS (Ordnance Survey)
			{"NY 9545 9776", new Location(-2.07162170, 55.27392830)},
			{"NU1918 2813", new Location(-1.69597330, 55.54650170)},
			{"NU19182813", new Location(-1.69597330, 55.54650170)},
			// Military Grid Reference System (MGRS)
			{"4QFJ 12345 67890", new Location(-157.9160812, 21.4097967)},
			{"4QFJ1234567890", new Location(-157.9160812, 21.4097967)},
			{"15SUD0370514711", new Location(-95.2654824, 38.9593911)}
		};
		return Arrays.asList(list);
    }

	@org.junit.Test
    public void testDetect() {
		LocationCollection result = new LocationCollection();
		detector.detect(this.pattern, result);
		
		LocationCollection expected = new LocationCollection();
		expected.add(this.location);
		
		Assert.assertEquals(expected.first(), result.first());
    }
	
}
