package docs;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.strategy.NullStrategy;
import de.lutana.geodataextractor.detector.MapDetector;
import de.lutana.geodataextractor.detector.TensorFlowMapDetector;
import de.lutana.geodataextractor.detector.TensorFlowWorldMapDetector;
import de.lutana.geodataextractor.detector.WorldMapDetector;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import org.junit.Assert;


@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class WorldMapCombinedDetectorTest extends BasePublicationTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;
	
	private static WorldMapDetector wmd;
	private static MapDetector md;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() throws URISyntaxException, IOException {
		TensorFlowMapDetector.getInstance().preload();
		TensorFlowWorldMapDetector.getInstance().preload();
		return BasePublicationTest.getAllFiguresWithStrategy(new NullStrategy());
    }

	@org.junit.Test
    public void testFigures() throws IOException, URISyntaxException {
		Boolean expected = WorldMapDetectorTest.getExpectedResult(figureObj);
		Assert.assertNotNull(expected);

		long timeStart = System.currentTimeMillis();
		Boolean isWorldMap = false;
		Float result = null;
		float isMap = md.detect(figureObj);
		if (isMap >= 0.5) {
			result = wmd.detect(figureObj);
			isWorldMap = (result >= 0.5);
		}
		addBenchmark(System.currentTimeMillis() - timeStart);
		System.out.print((isWorldMap.equals(expected) ? "" : "!! ") + figureObj.getDocument().getFile().getName() + "#" + figureObj.toString() + ": " + (expected == true ? "WORLD" : "OTHER") + " == " + (isWorldMap == true ? "WORLD" : "OTHER"));
		if (result != null) {
			System.out.print("(" + Math.round(result * 100) + "%)");
		}
		System.out.println();
		addTestResults(expected, isWorldMap);
		Assert.assertEquals(expected, isWorldMap);
    }
	
    @org.junit.BeforeClass
    public static void initTests() {
		md = new MapDetector(false);
		wmd = new WorldMapDetector();
        BasePublicationTest.resetTestEnv();
    }
	
    @org.junit.AfterClass
    public static void finalizeTests() {
        System.out.println(BasePublicationTest.getTestResults());
    }
	
}