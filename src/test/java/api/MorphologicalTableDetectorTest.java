package api;

import de.lutana.geodataextractor.detector.cv.CvGraphic;
import de.lutana.geodataextractor.detector.cv.MorphologicalTableDetector;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.locator.NullStrategy;
import de.lutana.geodataextractor.util.GeoTools;
import docs.BasePublicationTest;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.opencv.core.Rect;

// This test makes sure the table detector doesn't detect maps as tables
@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class MorphologicalTableDetectorTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		List<Object[]> onlyMaps = new ArrayList<>();
		Collection<Object[]> data = BasePublicationTest.getAllFiguresWithStrategy(new NullStrategy());
		for(Object[] obj : data) {
			Figure figure = (Figure) obj[0];
			File mapMetaFile = BasePublicationTest.getFigureMetaFile(figure);
			if (mapMetaFile.exists()) {
				Location expectedLocation = BasePublicationTest.getExpectedLocationForFigure(figure);
				if(expectedLocation != null) {
					onlyMaps.add(obj);
				}
			}
		}
		return onlyMaps;
    }

	@org.junit.Test
    public void testFigures() throws IOException, URISyntaxException {
		CvGraphic g = new CvGraphic(figureObj.getGraphic());
		MorphologicalTableDetector m = new MorphologicalTableDetector(g);
		List<Rect> rectangles = m.detect();
		Boolean noTableFound = true;
		Location imageLocation = new Location(0, g.getWidth(), 0, g.getHeight());
		for(Rect rect : rectangles) {
			// make sure it's not just a legend or a really small table.
			Location tableLocation = new Location(rect.x, rect.x + rect.width, rect.y, rect.y + rect.y + rect.height); // Well, somehow they are locations ;)
			if (GeoTools.calcJaccardIndex(tableLocation, imageLocation) > 0.5) {
				noTableFound = false;
				break;
			}
		}

		if (!noTableFound) {
			System.out.println("Map detected as table: " + figureObj.getDocument().getFile().getName() + "#" + figureObj.toString());
		}
		Assert.assertTrue(noTableFound);
    }
	
}