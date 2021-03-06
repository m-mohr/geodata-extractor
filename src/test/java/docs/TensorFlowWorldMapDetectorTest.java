package docs;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.strategy.NullStrategy;
import de.lutana.geodataextractor.detector.TensorFlowWorldMapDetector;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import org.junit.Assert;


@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class TensorFlowWorldMapDetectorTest extends BasePublicationTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() throws URISyntaxException, IOException {
		TensorFlowWorldMapDetector.getInstance().preload();
		return BasePublicationTest.getAllFiguresWithStrategy(new NullStrategy());
    }

	@org.junit.Test
	public void testFigures() throws IOException, URISyntaxException {
		Boolean expected = WorldMapDetectorTest.getExpectedResult(figureObj);
		Assert.assertNotNull(expected);

		long timeStart = System.currentTimeMillis();
		float result = TensorFlowWorldMapDetector.getInstance().detect(figureObj.getGraphic());
		addBenchmark(System.currentTimeMillis() - timeStart);

		Boolean isWorldMap = (result >= 0.5);
		System.out.println((isWorldMap.equals(expected) ? "" : "!! ") + figureObj.getDocument().getFile().getName() + "#" + figureObj.toString() + ": " + (expected ? "WORLD" : "OTHER") + " == " + (isWorldMap ? "WORLD" : "OTHER") + "(" + Math.round(result * 100) + "%)");
		addTestResults(expected, isWorldMap);
		Assert.assertEquals(expected, isWorldMap);
    }
	
    @org.junit.BeforeClass
    public static void initTests() {
        BasePublicationTest.resetTestEnv();
    }
	
    @org.junit.AfterClass
    public static void finalizeTests() {
        System.out.println(BasePublicationTest.getTestResults());
    }
	
}