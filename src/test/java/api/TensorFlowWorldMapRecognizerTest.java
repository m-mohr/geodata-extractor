package api;

import de.lutana.geodataextractor.GeodataExtractor;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.locator.NullStrategy;
import de.lutana.geodataextractor.util.FileExtension;
import de.lutana.geodataextractor.recognizor.TensorFlowWorldMapRecognizer;
import docs.BasePublicationTest;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Assert;


@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class TensorFlowWorldMapRecognizerTest {

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
		float result = TensorFlowWorldMapRecognizer.getInstance().recognize(figureObj.getGraphic());
		Boolean isWorldMap = (result >= 0.5);
		File mapMetaFile = BasePublicationTest.getFigureMetaFile(figureObj);
		if (mapMetaFile.exists()) {
			Location expLoc = BasePublicationTest.getExpectedLocationForFigure(figureObj);
			if (expLoc != null) {
				expected = (expLoc.getMinX() == -180 && expLoc.getMaxX() == 180 && expLoc.getMinY() == -90 && expLoc.getMaxY() == 90);
			}
			else {
				expected = false;
			}
		}
		System.out.println((isWorldMap.equals(expected) ? "" : "!! ") + figureObj.getDocument().getFile().getName() + "#" + figureObj.toString() + ": " + (expected ? "WORLD" : "OTHER") + " == " + (isWorldMap ? "WORLD" : "OTHER") + "(" + Math.round(result * 100) + "%)");
		Assert.assertEquals(expected, isWorldMap);
    }
	
}