package docs;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.recognizer.GeoNamesGraphicRecognizer;
import de.lutana.geodataextractor.recognizer.gazetteer.LuceneIndex;
import java.util.Collection;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class GeoNamesGraphicRecognizerTest extends BaseRecognizerTest {

	private static LuceneIndex geoNamesIndex;
	private static GeoNamesGraphicRecognizer recognizer;

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		return getData(false);
    }

	@org.junit.Test
    public void testDocuments() {
		assertFigure(figureObj, recognizer);
    }
	
    @org.junit.BeforeClass
    public static void initTests() {
		geoNamesIndex = new LuceneIndex();
		geoNamesIndex.load();
		recognizer = new GeoNamesGraphicRecognizer(geoNamesIndex);
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
