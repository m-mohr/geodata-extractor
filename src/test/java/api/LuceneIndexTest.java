package api;

import de.lutana.geodataextractor.recognizer.gazetteer.GeoName;
import de.lutana.geodataextractor.recognizer.gazetteer.LuceneIndex;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class LuceneIndexTest {
	
    @org.junit.runners.Parameterized.Parameter(0)
    public String text;
    @org.junit.runners.Parameterized.Parameter(1)
    public String osmId;
    @org.junit.runners.Parameterized.Parameter(2)
    public boolean fuzzyIfNoResultsMode;
	
	private LuceneIndex index;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		Object[][] list = new Object[][] {
			{"France", "r2202162", false}, // France / name
			{"Frankreich", "r2202162", false}, // France / alternativeNames
			{"Frankreich", "r2202162", true}, // France / alternativeNames / fuzzy
			{"Francex", "r2202162", true}, // France / name / fuzzy
			{"St Gallenn", "r1683941", true}, // St. Gallen / name / fuzzy
			{"St Gallenn", null, false}, // St. Gallen / name
			{"Austria", "r16239", true}, // Austria / name / fuzzy
		};
		return Arrays.asList(list);
    }
	
	@org.junit.Before
	public void setUp() throws IOException, ClassNotFoundException {
		index = new LuceneIndex();
		index.load();
	}
	
	@org.junit.After
	public void tearDown() {
		index.close();
	}

	@org.junit.Test
    public void testFind() {
		List<GeoName> result = index.find(this.text, fuzzyIfNoResultsMode, 1);
		if (this.osmId != null) {
			Assert.assertFalse(result.isEmpty());
			Assert.assertEquals(this.osmId, result.get(0).getOsmId());
		}
		else {
			Assert.assertTrue(result.isEmpty());
		}
	}

}
