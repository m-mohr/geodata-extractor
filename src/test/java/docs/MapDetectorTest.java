package docs;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.strategy.NullStrategy;
import de.lutana.geodataextractor.detector.MapDetector;
import docs.BasePublicationTest.StudyResults;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Assume;


@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class MapDetectorTest extends BasePublicationTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;
	
	private static MapDetector detector;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		return BasePublicationTest.getAllFiguresWithStrategy(new NullStrategy());
    }

	@org.junit.Test
    public void testFigures() throws IOException, URISyntaxException {
		long timeStart = System.currentTimeMillis();
		float result = detector.detect(figureObj);
		addBenchmark(System.currentTimeMillis() - timeStart);
		Boolean isMap = (result >= 0.5);

		StudyResults studyResults = BasePublicationTest.getStudyResultsForFigure(figureObj);
		Boolean expected = null;
		try {
			Assume.assumeTrue(studyResults.isFigure()); // Ignores tests when it's not a valid figure
			expected = studyResults.isMap();
		} catch (BasePublicationTest.InconsistencyException ex) {
			Assert.assertNotNull(ex.getMessage(), expected);
		}

		System.out.println((isMap.equals(expected) ? "" : "!! ") + figureObj.getDocument().getFile().getName() + "#" + figureObj.toString() + ": " + (expected ? "MAP" : "NOT a map") + " == " + (isMap ? "MAP" : "NOT a map") + "(" + Math.round(result * 100) + "%)");
		addTestResults(expected, isMap);
		Assert.assertEquals(expected, isMap);
    }
	
    @org.junit.BeforeClass
    public static void initTests() {
		detector = new MapDetector(false);
        BasePublicationTest.resetTestEnv();
    }
	
    @org.junit.AfterClass
    public static void finalizeTests() {
        System.out.println(BasePublicationTest.getTestResults());
    }
	
}