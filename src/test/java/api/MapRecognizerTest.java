package api;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.locator.NullStrategy;
import de.lutana.geodataextractor.recognizor.MapRecognizer;
import docs.BasePublicationTest;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import org.junit.Assert;


@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class MapRecognizerTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		return BasePublicationTest.getAllFiguresWithStrategy(new NullStrategy());
    }

	@org.junit.Test
    public void testFigures() throws IOException, URISyntaxException {
		MapRecognizer m = new MapRecognizer(false);
		float result = m.recognize(figureObj);
		Boolean isMap = (result >= 0.5);

		Boolean expected = null;
		File mapMetaFile = BasePublicationTest.getFigureMetaFile(figureObj);
		if (mapMetaFile.exists()) {
			Location expectedLocation = BasePublicationTest.getExpectedLocationForFigure(figureObj);
			expected = (expectedLocation != null);
		}

		System.out.println((isMap.equals(expected) ? "" : "!! ") + figureObj.getDocument().getFile().getName() + "#" + figureObj.toString() + ": " + (expected ? "MAP" : "NOT a map") + " == " + (isMap ? "MAP" : "NOT a map") + "(" + Math.round(result * 100) + "%)");
		Assert.assertEquals(expected, isMap);
    }
	
}