package docs;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.strategy.NullStrategy;
import de.lutana.geodataextractor.recognizor.TensorFlowWorldMapRecognizer;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import org.junit.Assert;


@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class TensorFlowWorldMapRecognizerTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		return BasePublicationTest.getAllFiguresWithStrategy(new NullStrategy());
    }

	@org.junit.Test
	public void testFigures() throws IOException, URISyntaxException {
		Boolean expected = WorldMapRecognizerTest.getExpectedResult(figureObj);
		Assert.assertNotNull(expected);

		float result = TensorFlowWorldMapRecognizer.getInstance().recognize(figureObj.getGraphic());
		Boolean isWorldMap = (result >= 0.5);
		System.out.println((isWorldMap.equals(expected) ? "" : "!! ") + figureObj.getDocument().getFile().getName() + "#" + figureObj.toString() + ": " + (expected ? "WORLD" : "OTHER") + " == " + (isWorldMap ? "WORLD" : "OTHER") + "(" + Math.round(result * 100) + "%)");
		Assert.assertEquals(expected, isWorldMap);
    }
	
}