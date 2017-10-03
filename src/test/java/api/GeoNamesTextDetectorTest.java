package api;

import de.lutana.geodataextractor.detector.GeoNamesTextDetector;
import de.lutana.geodataextractor.detector.gazetteer.LuceneIndex;
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
	private LuceneIndex index;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		Object[][] list = new Object[][] {
			{"I live in Austria.", new Location(9.5307487, 17.1607759, 46.372276, 49.0205263)},
			{"Photo taken in York.", new Location(-1.0815361, -1.0815361, 53.9590554, 53.9590554)}, // York in the UK
			{"Photo taken in York, US.", new Location(-76.7627, -76.6994481, 39.942753, 39.991176)}, // York in Pennsylvania, US
//			{"Photo taken in York, WA.", new Location(116.76915, 116.76915, -31.888903, -31.888903)} // York in Western Australia (not in dataset 2.0.1?)
		};
		return Arrays.asList(list);
    }
	
	@org.junit.Before
	public void setUp() throws IOException, ClassNotFoundException {
		index = new LuceneIndex();
		index.load();
		parser = new GeoNamesTextDetector(index);
	}
	
	@org.junit.After
	public void tearDown() {
		if (index != null) {
			index.close();
		}
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
