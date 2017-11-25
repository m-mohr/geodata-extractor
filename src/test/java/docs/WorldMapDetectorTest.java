package docs;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.strategy.NullStrategy;
import de.lutana.geodataextractor.detector.TensorFlowMapDetector;
import de.lutana.geodataextractor.detector.TensorFlowWorldMapDetector;
import de.lutana.geodataextractor.detector.WorldMapDetector;
import de.lutana.geodataextractor.util.GeoTools;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Assume;


@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class WorldMapDetectorTest extends BasePublicationTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;
	
	private static WorldMapDetector detector;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() throws URISyntaxException, IOException {
		TensorFlowMapDetector.getInstance().preload();
		TensorFlowWorldMapDetector.getInstance().preload();
		return BasePublicationTest.getAllFiguresWithStrategy(new NullStrategy());
    }
	
	public static Boolean getExpectedResult(Figure figure) {
		BasePublicationTest.StudyResults studyResults = BasePublicationTest.getStudyResultsForFigure(figure);
		Boolean expected = null;
		try {
			Assume.assumeTrue(studyResults.isFigure()); // Ignores tests when it's not a valid figure
			Location location = studyResults.getLocation();
			expected = GeoTools.calcJaccardIndex(new Location(-180, 180, -90, 90), location) > 0.9;
		} catch (BasePublicationTest.InconsistencyException ex) {
			Assert.assertNotNull(ex.getMessage(), expected);
		}
		return expected;
	}

	@org.junit.Test
    public void testFigures() throws IOException, URISyntaxException {
		Boolean expected = getExpectedResult(figureObj);
		Assert.assertNotNull(expected);

		long timeStart = System.currentTimeMillis();
		float result = detector.detect(figureObj);
		addBenchmark(System.currentTimeMillis() - timeStart);
		Boolean isWorldMap = (result >= 0.5);

		System.out.println((isWorldMap.equals(expected) ? "" : "!! ") + figureObj.getDocument().getFile().getName() + "#" + figureObj.toString() + ": " + (expected == true ? "WORLD" : "OTHER") + " == " + (isWorldMap == true ? "WORLD" : "OTHER") + " (" + Math.round(result * 100) + "%)");
		addTestResults(expected, isWorldMap);
		Assert.assertEquals(expected, isWorldMap);
    }
	
    @org.junit.BeforeClass
    public static void initTests() {
		detector = new WorldMapDetector();
        BasePublicationTest.resetTestEnv();
    }
	
    @org.junit.AfterClass
    public static void finalizeTests() {
        System.out.println(BasePublicationTest.getTestResults());
    }
	
}