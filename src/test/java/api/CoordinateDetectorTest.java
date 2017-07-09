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
			{"12° N, 12°S, 90° 30' W, 1°30'40.5\"E", new Location(-90.5, 1.51125, -12d, 12d)}
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
