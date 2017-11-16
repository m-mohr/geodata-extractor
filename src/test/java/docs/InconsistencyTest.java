package docs;

import de.lutana.geodataextractor.entity.Figure;
import java.util.Collection;
import static org.junit.Assert.assertTrue;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class InconsistencyTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;

    @org.junit.runners.Parameterized.Parameter(1)
    public Collection<String> where;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		return BasePublicationTest.getInconsistencies();
    }

	@org.junit.Test
    public void testDocuments() {
		assertTrue("Inconsistency " + where + " for figure " + figureObj.getGraphicFile().getAbsoluteFile(), where.isEmpty());
    }
	
}
