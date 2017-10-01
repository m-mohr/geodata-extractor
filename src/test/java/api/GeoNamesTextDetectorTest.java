package api;

import de.lutana.geodataextractor.detector.GeoNamesTextDetector;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.util.GeoTools;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class GeoNamesTextDetectorTest {
	
    @org.junit.runners.Parameterized.Parameter(0)
    public String text;
    @org.junit.runners.Parameterized.Parameter(1)
    public Location expectedLocation;
	
	private GeoNamesTextDetector parser;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		Object[][] list = new Object[][] {
			{"I live in Austria.", new Location(9.5307487, 17.1607759, 46.372276, 49.0205263)}
		};
		return Arrays.asList(list);
    }
	
	@org.junit.Before
	public void setUp() throws IOException, ClassNotFoundException {
		parser = new GeoNamesTextDetector();
	}
	
	@org.junit.After
	public void tearDown() {
		parser.close();
	}

	@org.junit.Test
    public void testDetect() {
		LocationCollection lc = new LocationCollection();
		parser.detect(this.text, lc);
		Assert.assertFalse(lc.isEmpty());
		Location result = lc.get(0);
		double jackard = GeoTools.calcJaccardIndex(expectedLocation, result);
		Assert.assertTrue(jackard > 0.99);
    }

}
