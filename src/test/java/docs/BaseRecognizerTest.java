package docs;

import de.lutana.geodataextractor.GeodataExtractor;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.recognizer.GraphicRecognizer;
import de.lutana.geodataextractor.recognizer.TextRecognizer;
import de.lutana.geodataextractor.recognizer.cv.CvGraphic;
import de.lutana.geodataextractor.strategy.NullStrategy;
import de.lutana.geodataextractor.util.FileExtension;
import de.lutana.geodataextractor.util.GeoTools;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;

public class BaseRecognizerTest extends BasePublicationTest {
	
	public static double THRESHOLD = 0.5d;

	public static Collection<Object[]> getData(boolean onlyWithCoordinates) {
		GeodataExtractor extractor = new GeodataExtractor(new NullStrategy());
		extractor.enableCaching(true);
		Collection<Object[]> list = new ArrayList<>();
		File[] files = BasePublicationTest.DOC_FOLDER.listFiles(new FileExtension.Filter("pdf"));
		for (File file : files) {
			Document document = extractor.runSingle(file);
			FigureCollection figures = document.getFigures();
			for (Figure figure : figures) {
				StudyResults sr = BasePublicationTest.getStudyResultsForFigure(figure);
				try {
					boolean isMap = sr.isMap();
					Boolean hasCoords = sr.hasCoordinates();
					if (!isMap) {
						continue;
					}
					else if (onlyWithCoordinates && (hasCoords == null || hasCoords == false)) {
						continue;
					}
					list.add(new Object[]{figure, sr.getLocation()});
				} catch(InconsistencyException e) {}
			}
		}
		return list;
	}
	
	public static void assertFigure(Figure expectedFigure, TextRecognizer recognizer) {
		LocationCollection locations = new LocationCollection();
		Document doc = expectedFigure.getDocument();
		Assert.assertNotNull("No parent document available", doc);
		if (doc.getTitle() != null) {
			recognizer.recognize(doc.getTitle(), locations, 1);
		}
		if (doc.getDescription() != null) {
			recognizer.recognize(doc.getDescription(), locations, 1);
		}
		if (expectedFigure.getCaption() != null) {
			recognizer.recognize(expectedFigure.getCaption(), locations, 1);
		}
		
		assertLocations(expectedFigure, locations);
	}
	
	public static void assertFigure(Figure expectedFigure, GraphicRecognizer recognizer) {
		LocationCollection locations = new LocationCollection();
		CvGraphic cvGraphic = new CvGraphic(expectedFigure);
		recognizer.recognize(cvGraphic, locations, 1);
		cvGraphic.dispose();
		
		assertLocations(expectedFigure, locations);
	}
	
	public static void assertLocations(Figure expectedFigure, LocationCollection locations) {
		double hightestJackaedIndex = 0;
		if (locations != null) {
			for(Location foundLocation : locations) {
				Double jaccardIndex = GeoTools.calcJaccardIndex(expectedFigure.getLocation(), foundLocation);
				if (hightestJackaedIndex < jaccardIndex) {
					hightestJackaedIndex = jaccardIndex;
				}
			}
		}

		Double formatted = Math.round(hightestJackaedIndex * 100d) / 100d;
		String info = formatted + " - " + expectedFigure.getGraphicFile().getPath();
		System.out.println(info);
		addTestResult(hightestJackaedIndex);
		assertTrue(info, hightestJackaedIndex >= THRESHOLD);
	}
	
}
