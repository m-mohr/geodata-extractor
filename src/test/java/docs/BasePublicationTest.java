package docs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.lutana.geodataextractor.GeodataExtractor;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.util.FileExtension;
import de.lutana.geodataextractor.util.GeoTools;
import java.io.File;
import java.io.IOException;
import java.util.List;
import static org.junit.Assert.assertTrue;

public abstract class BasePublicationTest {

	private static final Double JACCARD_INDEX_THRESHOLD = 0.5;
	protected static final File DOC_FOLDER = new File("./test-docs/");
	protected static final File META_FOLDER = new File("./parsed-maps/");

	private static GeodataExtractor instance = new GeodataExtractor();

	protected void runDocumentTest(String documentFile) {
		Document document = getDocument(documentFile);
		Location expected = this.getExpectedLocationForDocument(document);
		this.assertLocation(documentFile, expected, document.getLocation());
	}

	protected void runFigureTest(Figure figure) {
		Document document = figure.getDocument();
		Location expected = this.getExpectedLocationForFigure(figure);
		this.assertLocation(document.getFile().getName() + "#" + figure.getGraphic().getName(), expected, figure.getLocation());
	}

	protected void runTest(String documentFile, Location location) {
		this.assertDocument(location, this.getDocument(documentFile));
	}

	protected void assertDocument(Location expected, Document document) {
		this.assertLocation(document.getFile().getName(), expected, document.getLocation());
	}

	protected void assertLocation(String testName, Location expected, Location result) {
		Double jaccardIndex = GeoTools.calcJaccardIndex(expected, result);
		System.out.print(testName + ": Expected " + expected + "; Found " + result);
		if (expected == null) {
			System.out.println(" - Not a map");
			assertTrue("Not a map", true);
		}
		else {
			System.out.println(" - Jaccard Index: " + jaccardIndex);
			assertTrue(
					"Calculated Jaccard Index (" + jaccardIndex + ") should be greater than threshold (" + JACCARD_INDEX_THRESHOLD + ")",
					jaccardIndex > JACCARD_INDEX_THRESHOLD
			);
		}
	}

	private Location getExpectedLocationForDocument(Document document) {
		FigureCollection figures = document.getFigures();
		LocationCollection locations = new LocationCollection();
		for (Figure figure : figures) {
			Location location = this.getExpectedLocationForFigure(figure);
			if (location != null) {
				locations.add(location);
			}
		}
		// ToDo: Is the union of all locations really the wanted behaviour?
		return locations.getLocation();
	}

	private Location getExpectedLocationForFigure(Figure figure) {
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

	public static Document getDocument(String documentFile) {
		return instance.runSingle(getDocumentFile(documentFile));
	}

	public static File getDocumentFile(String documentFile) {
		return new File(DOC_FOLDER, documentFile);
	}

	public static File getFigureMetaFolder(String documentFile) {
		File docFileObj = new File(META_FOLDER, documentFile);
		return new File(docFileObj.getAbsolutePath() + "-figures");
	}

	public static File getFigureMetaFile(Figure figure) {
		File metaFolder = getFigureMetaFolder(figure.getDocument().getFile().getName());
		return new File(metaFolder, FileExtension.replace(figure.getGraphic().getName(), "json"));
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class BBoxContainer {
		public List<BBox> locations;
		public BBoxContainer() {}
		public LocationCollection toLocationCollection() {
			LocationCollection collection = new LocationCollection();
			for(BBox location : locations) {
				collection.add(location.toLocation());
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
