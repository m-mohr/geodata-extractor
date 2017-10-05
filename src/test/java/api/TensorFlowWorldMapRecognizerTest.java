package api;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.locator.NullStrategy;
import de.lutana.geodataextractor.recognizor.MapRecognizer;
import de.lutana.geodataextractor.recognizor.TensorFlowWorldMapRecognizer;
import docs.BasePublicationTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Assume;


@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class TensorFlowWorldMapRecognizerTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		return BasePublicationTest.getAllFiguresWithStrategy(new NullStrategy());
    }
	
	private Boolean getExpectedResult(Figure figure) {
		BasePublicationTest.StudyResults studyResults = BasePublicationTest.getStudyResultsForFigure(figure);
		Boolean expected = null;
		try {
			Assume.assumeTrue(studyResults.isFigure()); // Ignores tests when it's not a valid figure
			Location location = studyResults.getLocation();
			if (location != null) {
				expected = (location.getMinX() == -180 && location.getMaxX() == 180 && location.getMinY() == -90 && location.getMaxY() == 90);
			}
			else {
				expected = false;
			}
		} catch (BasePublicationTest.InconsistencyException ex) {
			Assert.assertNotNull(ex.getMessage(), expected);
		}
		return expected;
	}

	@org.junit.Test
	public void testFiguresWorldOnly() throws IOException, URISyntaxException {
		Boolean expected = this.getExpectedResult(figureObj);
		Assert.assertNotNull(expected);

		float result = TensorFlowWorldMapRecognizer.getInstance().recognize(figureObj.getGraphic());
		Boolean isWorldMap = (result >= 0.5);
		System.out.println((isWorldMap.equals(expected) ? "" : "!! ") + figureObj.getDocument().getFile().getName() + "#" + figureObj.toString() + ": " + (expected ? "WORLD" : "OTHER") + " == " + (isWorldMap ? "WORLD" : "OTHER") + "(" + Math.round(result * 100) + "%)");
		Assert.assertEquals(expected, isWorldMap);
    }

	@org.junit.Test
    public void testFiguresWorldWithMapRecognition() throws IOException, URISyntaxException {
		Boolean expected = this.getExpectedResult(figureObj);
		Assert.assertNotNull(expected);

		Boolean isWorldMap = false;
		Float result = null;
		MapRecognizer mr = new MapRecognizer(false);
		float isMap = mr.recognize(figureObj);
		if (isMap >= 0.4) {
			result = TensorFlowWorldMapRecognizer.getInstance().recognize(figureObj.getGraphic());
			isWorldMap = (result >= 0.5);
		}
		System.out.print((isWorldMap.equals(expected) ? "" : "!! ") + figureObj.getDocument().getFile().getName() + "#" + figureObj.toString() + ": " + (expected == true ? "WORLD" : "OTHER") + " == " + (isWorldMap == true ? "WORLD" : "OTHER"));
		if (result != null) {
			System.out.print("(" + Math.round(result * 100) + "%)");
		}
		System.out.println();
		Assert.assertEquals(expected, isWorldMap);
    }
	
}