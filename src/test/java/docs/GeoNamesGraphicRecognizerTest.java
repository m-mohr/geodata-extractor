package docs;

import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.recognizer.GeoNamesGraphicRecognizer;
import de.lutana.geodataextractor.recognizer.cv.CvGraphic;
import de.lutana.geodataextractor.recognizer.gazetteer.LuceneIndex;
import de.lutana.geodataextractor.strategy.DefaultStrategy;
import static docs.BaseRecognizerTest.assertLocations;
import java.util.Collection;
import java.util.HashMap;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class GeoNamesGraphicRecognizerTest extends BaseRecognizerTest {

	private static LuceneIndex geoNamesIndex;
	private static GeoNamesGraphicRecognizer recognizer;
	private static DefaultStrategy strategy;
	private static HashMap<String, LocationCollection> cache;

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;
    @org.junit.runners.Parameterized.Parameter(1)
    public Location expectedLocation;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		return getData(false);
    }

	@org.junit.Test
    public void testDocuments() {
		Document doc = figureObj.getDocument();
		String docPath = doc.getFile().getAbsolutePath();
		LocationCollection locations;
		if (!cache.containsKey(docPath)) {
			locations = strategy.getDocumentLocations(doc);
			FigureCollection figures = doc.getFigures();
			for(Figure figure : figures) {
				strategy.getMapLocations(figure, new CvGraphic(figure), locations);
			}
			cache.put(docPath, locations);
		}
		else {
			locations = cache.get(docPath);
		}

		int prevSize = locations.size();
		CvGraphic cvGraphic = new CvGraphic(figureObj);
		recognizer.recognize(cvGraphic, locations, 1);
		cvGraphic.dispose();
		LocationCollection newLocations = new LocationCollection();
		if (prevSize < locations.size()) {
			newLocations.add(locations.get(prevSize));
		}

		Figure expected = new Figure(figureObj);
		expected.setLocation(expectedLocation);
		assertLocations(expected, newLocations);
    }
	
    @org.junit.BeforeClass
    public static void initTests() {
		geoNamesIndex = new LuceneIndex();
		geoNamesIndex.load();
		recognizer = new GeoNamesGraphicRecognizer(geoNamesIndex);
		strategy = new DefaultStrategy(null, geoNamesIndex);
		strategy.disableGeoNamesGraphicRecognizer();
		cache = new HashMap<>();
        BasePublicationTest.resetTestEnv();
    }
	
    @org.junit.AfterClass
    public static void finalizeTests() {
		if (geoNamesIndex != null) {
			geoNamesIndex.close();
		}
        System.out.println(BasePublicationTest.getTestResults());
    }
	
}
