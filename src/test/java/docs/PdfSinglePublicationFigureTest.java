package docs;

import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class PdfSinglePublicationFigureTest extends BasePublicationTest {

	private static final File file = new File("Ganguli and Ganguly - 2016 - Space-time trends in U.S. meteorological droughts.pdf");
	
    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		Collection<Object[]> list = new ArrayList<>();
		Document document = getDocument(file.getName());
		FigureCollection figures = document.getFigures();
		for (Figure figure : figures) {
			list.add(new Object[]{figure});
		}
		return list;
    }
	
	@org.junit.Test
    public void testDocument() {
		runFigureTest(this.figureObj);
    }
	
}
