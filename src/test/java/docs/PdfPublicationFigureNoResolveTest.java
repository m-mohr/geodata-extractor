package docs;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.strategy.DefaultStrategy;
import static docs.BasePublicationTest.getStudyResultsForFigure;
import org.junit.Assume;

public class PdfPublicationFigureNoResolveTest extends BaseRecognizerTest {

	public static class TestAnalyzerStrategy extends DefaultStrategy {
		@Override
		protected void resolveFigureLocation(Figure figure, LocationCollection locations) {
			StudyResults studyResults = getStudyResultsForFigure(figure);
			Location expected = null;
			try {
				if (studyResults.isFigure() && studyResults.isMap()) {
					expected = studyResults.getLocation();
					if (expected == null) {
						addTestResult(0);
					}
					else {
						figure.setLocation(expected);
						assertLocations(figure, locations);
					}
				}
			} catch (InconsistencyException ex) {
				ex.printStackTrace();
				Assume.assumeNotNull(expected);
			}
		}
	}

	@org.junit.Test
    public void testDocuments() {
		getAllFiguresWithStrategy(new TestAnalyzerStrategy());
    }
	
    @org.junit.BeforeClass
    public static void initTests() {
		THRESHOLD = 0;
        BasePublicationTest.resetTestEnv();
    }

    @org.junit.AfterClass
    public static void finalizeTests() {
        System.out.println(BasePublicationTest.getTestResults());
    }
	
}
