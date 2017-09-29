package api;

import de.lutana.geodataextractor.GeodataExtractor;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.locator.NullStrategy;
import de.lutana.geodataextractor.util.FileExtension;
import de.lutana.geodataextractor.recognizor.TensorFlowMapRecognizer;
import docs.BasePublicationTest;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Assert;


@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class TensorFlowMapRecognizerTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public Figure figureObj;

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() {
		GeodataExtractor instance = new GeodataExtractor(new NullStrategy());
		instance.enableCaching(true);
		Collection<Object[]> list = new ArrayList<>();
		File[] files = BasePublicationTest.DOC_FOLDER.listFiles(new FileExtension.Filter("pdf"));
		for (File file : files) {
			Document document = instance.runSingle(file);
			FigureCollection figures = document.getFigures();
			for (Figure figure : figures) {
				list.add(new Object[]{figure});
			}
		}
		return list;
    }

	@org.junit.Test
    public void testDocuments() throws IOException, URISyntaxException {
		Boolean expected = null;
		float result = TensorFlowMapRecognizer.getInstance().recognize(figureObj.getGraphic());
		Boolean isMap = (result >= 0.5);
		File mapMetaFile = BasePublicationTest.getFigureMetaFile(figureObj);
		if (mapMetaFile.exists()) {
			Location expectedLocation = BasePublicationTest.getExpectedLocationForFigure(figureObj);
			expected = (expectedLocation != null);
		}
		System.out.println((isMap.equals(expected) ? "" : "!! ") + figureObj.getDocument().getFile().getName() + "#" + figureObj.toString() + ": " + (expected ? "MAP" : "NOT a map") + " == " + (isMap ? "MAP" : "NOT a map") + "(" + Math.round(result * 100) + "%)");
		Assert.assertEquals(expected, isMap);
    }
	
}