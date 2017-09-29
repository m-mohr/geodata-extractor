package docs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lutana.geodataextractor.Config;
import de.lutana.geodataextractor.GeodataExtractor;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.locator.Strategy;
import de.lutana.geodataextractor.util.FileExtension;
import de.lutana.geodataextractor.util.GeoTools;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static org.junit.Assert.assertTrue;

public abstract class BasePublicationTest {

	public static final Double JACCARD_INDEX_THRESHOLD = 0.5;
	public static final File DOC_FOLDER = new File("./test-docs/");

	private static GeodataExtractor instance;

	protected void runDocumentTest(String documentFile) {
		this.testDocument(getDocument(documentFile));
	}

	protected void runDocumentTest(String documentFile, Location expectedLocation) {
		this.assertDocument(expectedLocation, getDocument(documentFile));
	}

	protected void testDocument(Document document) {
		this.assertDocument(this.getExpectedLocationForDocument(document), document);
	}

	protected void testFigure(Figure figure) {
		Document document = figure.getDocument();
		Location expected = this.getExpectedLocationForFigure(figure);
		this.assertLocation(document.getFile().getName() + "#" + figure.getGraphicFile().getName(), expected, figure.getLocation(), figure.getGraphicFile());
	}

	protected void assertDocument(Location expected, Document document) {
		this.assertLocation(document.getFile().getName(), expected, document.getLocation(), document.getFile());
	}

	protected void assertLocation(String testName, Location expected, Location result, File contextFile) {
		Double jaccardIndex = GeoTools.calcJaccardIndex(expected, result);
		String info = " - " + testName + ": Expected " + expected + "; Found " + result;
		if (expected == null) {
			System.out.println("NOMAP" + info);
			assertTrue("Not a map", true);
		}
		else {
			boolean success = (jaccardIndex > JACCARD_INDEX_THRESHOLD);
			Double formatted = Math.round(jaccardIndex * 100d) / 100d;
			System.out.println((success ? "FOUND" : "ERROR") + info + " - Jaccard Index: " + formatted);
			if (result != null) {
				try {
					System.out.println("        " + Config.getTestUrl(expected, result, contextFile));
				} catch (UnsupportedEncodingException ex) {}
			}
			assertTrue("Calculated Jaccard Index (" + formatted + ") should be greater than threshold (" + JACCARD_INDEX_THRESHOLD + ")", success);
		}
	}

	public static Location getExpectedLocationForDocument(Document document) {
		FigureCollection figures = document.getFigures();
		LocationCollection locations = new LocationCollection();
		for (Figure figure : figures) {
			Location location = getExpectedLocationForFigure(figure);
			if (location != null) {
				locations.add(location);
			}
		}
		// ToDo: Is the union of all locations really the wanted behaviour?
		return locations.getLocation();
	}

	public static Location getExpectedLocationForFigure(Figure figure) {
		File metaFile = getFigureMetaFile(figure);
		if (!metaFile.exists()) {
			// This is not a map that's why there is no meta data
			return null;
		}

		ObjectMapper mapper = new ObjectMapper();
		try {
			BBoxContainer c = mapper.readValue(metaFile, BBoxContainer.class);
			// ToDo: Is the union of all locations really the wanted behaviour?
			return c.toLocationCollection().getLocation();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Document getDocument(String documentPath) {
		return getDocument(documentPath, null);
	}
	
	public static Document getDocument(String documentPath, Integer page) {
		if (instance == null) {
			instance = new GeodataExtractor();
			instance.enableCaching(true);
		}
		File documentFile = getDocumentFile(documentPath);
		return instance.runSingle(documentFile, page);
	}

	public static File getDocumentFile(String documentFile) {
		return new File(DOC_FOLDER, documentFile);
	}

	public static File getFigureMetaFolder(String documentFile) {
		File docFileObj = getDocumentFile(documentFile);
		return new File(docFileObj.getAbsolutePath() + "-figures");
	}

	public static File getFigureMetaFile(Figure figure) {
		File metaFolder = getFigureMetaFolder(figure.getDocument().getFile().getName());
		return new File(metaFolder, FileExtension.replace(figure.getGraphicFile().getName(), "json"));
	}
	
	public static Collection<Object[]> getAllFiguresWithStrategy(Strategy strategy) {
		GeodataExtractor instance = new GeodataExtractor(strategy);
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
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class BBoxContainer {
		public List<BBox> locations;
		public BBoxContainer() {}
		public LocationCollection toLocationCollection() {
			LocationCollection collection = new LocationCollection();
			if (locations != null) {
				for(BBox location : locations) {
					collection.add(location.toLocation());
				}
			}
			return collection;
		}
	}
	
	public static class BBox {
		public Double minlon;
		public Double maxlon;
		public Double minlat;
		public Double maxlat;
		public BBox() {}
		public Location toLocation() {
			return new Location(minlon, maxlon, minlat, maxlat);
		}
	}

}
