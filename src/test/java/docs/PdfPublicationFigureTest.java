package docs;

import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.util.FileExtension;
import static docs.BasePublicationTest.DOC_FOLDER;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class PdfPublicationFigureTest extends BasePublicationTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		Collection<Object[]> list = new ArrayList<>();
		File[] files = DOC_FOLDER.listFiles(new FileExtension.Filter("pdf"));
		for (File file : files) {
			Document document = getDocument(file.getName());
			FigureCollection figures = document.getFigures();
			for (Figure figure : figures) {
				list.add(new Object[]{figure});
			}
		}
		return list;
    }

	@org.junit.Test
    public void testDocuments() {
		runFigureTest(this.figureObj);
    }
	
}
