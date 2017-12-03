package docs;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.recognizer.cv.CvGraphic;
import de.lutana.geodataextractor.strategy.DefaultStrategy;
import static docs.BasePublicationTest.getStudyResultsForFigure;
import org.junit.Assume;

public class PdfPublicationFigureNoResolveTest extends BaseRecognizerTest {

	public static class TestAnalyzerStrategy extends DefaultStrategy {
		@Override
		protected void extractFigureLocations(Figure figure, LocationCollection documentLocations) {
			CvGraphic cvGraphic = new CvGraphic(figure);

			LocationCollection figureLocations = null;
			float mapConfidence = this.mapRecognizer.detect(figure, cvGraphic);
			if (mapConfidence >= 0.4) { // 10% tolerance
				figureLocations = this.getMapLocations(figure, cvGraphic, documentLocations);
			}
			StudyResults studyResults = getStudyResultsForFigure(figure);
			try {
				if (studyResults.isMap()) {
					this.resolveFigureLocation(figure, figureLocations);
				}
			} catch (InconsistencyException ex) {}
			cvGraphic.dispose();
		}
		
		@Override
		protected void resolveFigureLocation(Figure figure, LocationCollection locations) {
			StudyResults studyResults = getStudyResultsForFigure(figure);
			Location expected = null;
			try {
				figure.setLocation(studyResults.getLocation());
				assertLocations(figure, locations);
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
