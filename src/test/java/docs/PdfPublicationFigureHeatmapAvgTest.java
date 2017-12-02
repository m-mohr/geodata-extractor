package docs;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.locationresolver.HeatmapResolver;
import de.lutana.geodataextractor.strategy.DefaultStrategy;
import java.util.Collection;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class PdfPublicationFigureHeatmapAvgTest extends BasePublicationTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		return getAllFiguresWithStrategy(new DefaultStrategy(new HeatmapResolver(HeatmapResolver.THRESHOLD.AVERAGE)));
    }

	@org.junit.Test
    public void testDocuments() {
		testFigure(this.figureObj);
    }
	
    @org.junit.BeforeClass
    public static void initTests() {
        BasePublicationTest.resetTestEnv();
    }
	
    @org.junit.AfterClass
    public static void finalizeTests() {
        System.out.println(BasePublicationTest.getTestResults());
    }
	
}
