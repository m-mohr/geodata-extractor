package docs;

import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.locationresolver.UnionResolver;
import de.lutana.geodataextractor.strategy.DefaultStrategy;
import java.util.Collection;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class PdfPublicationUnionTest extends BasePublicationTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public Document document;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		return getAllDocumentsWithStrategy(new DefaultStrategy(new UnionResolver()));
    }

	@org.junit.Test
    public void testDocuments() {
		testDocument(this.document);
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
