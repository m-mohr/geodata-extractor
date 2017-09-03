package docs;

import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import java.io.File;

public class PdfSinglePublicationFigureTest extends BasePublicationTest {

	@org.junit.Test
    public void testDocuments() {
		File file = new File ("1279.full.pdf");
		Document document = getDocument(file.getName());
		FigureCollection figures = document.getFigures();
		for (Figure figure : figures) {
			runFigureTest(figure);
		}
    }
	
}
