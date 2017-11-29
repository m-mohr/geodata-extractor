package docs;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.recognizer.GeoNamesTextRecognizer;
import de.lutana.geodataextractor.recognizer.gazetteer.LuceneIndex;
import java.io.IOException;
import java.util.Collection;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class GeoNamesTextRecognizerTest extends BaseRecognizerTest {

	private static LuceneIndex geoNamesIndex;
	private static GeoNamesTextRecognizer recognizer;

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
		try {
			recognizer = new GeoNamesTextRecognizer(geoNamesIndex);
		} catch (IOException | ClassNotFoundException ex) {
			ex.printStackTrace();
		}
        resetTestEnv();
    }
	
    @org.junit.AfterClass
    public static void finalizeTests() {
		if (geoNamesIndex != null) {
			geoNamesIndex.close();
		}
        System.out.println(getTestResults());
    }
	
}
