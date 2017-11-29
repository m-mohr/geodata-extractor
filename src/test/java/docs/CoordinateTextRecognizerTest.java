package docs;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.recognizer.CoordinateTextRecognizer;
import java.util.Collection;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class CoordinateTextRecognizerTest extends BaseRecognizerTest {

	private static CoordinateTextRecognizer recognizer;

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
		recognizer = new CoordinateTextRecognizer();
        BasePublicationTest.resetTestEnv();
    }
	
    @org.junit.AfterClass
    public static void finalizeTests() {
        System.out.println(BasePublicationTest.getTestResults());
    }
	
}
