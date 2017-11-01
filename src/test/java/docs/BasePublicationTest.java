package docs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Assume;

public abstract class BasePublicationTest {

	public static final Double JACCARD_INDEX_THRESHOLD = 0.001;
	public static final File DOC_FOLDER = new File("./test-docs/");
	
	private static int testCount = 0;
	private static long testTimeSum = 0;

	private static GeodataExtractor instance;

	protected void runDocumentTest(String documentFile) {
		this.testDocument(getDocument(documentFile));
	}

	protected void runDocumentTest(String documentFile, Location expectedLocation) {
		this.assertDocument(expectedLocation, getDocument(documentFile));
	}

	protected void testDocument(Document document) {
		this.assertDocument(getExpectedLocationForDocument(document), document);
	}

	protected void testFigure(Figure figure) {
		Document document = figure.getDocument();
		StudyResults studyResults = getStudyResultsForFigure(figure);
		this.assertFigure(document.getFile().getName() + "#" + figure.getGraphicFile().getName(), studyResults, figure.getLocation(), figure.getGraphicFile());
	}

	protected void assertDocument(Location expected, Document document) {
		this.assertLocation(document.getFile().getName(), (expected != null), expected, document.getLocation(), document.getFile());
	}

	protected void assertFigure(String testName, StudyResults studyResults, Location result, File contextFile) {
		Boolean isMap = null;
		Location expected = null;
		try {
			Assume.assumeTrue(studyResults.isFigure()); // Ignores tests when it's not a valid figure
			isMap = studyResults.isMap();
			expected = studyResults.getLocation();
		} catch (InconsistencyException ex) {
			Assert.assertNotNull(ex.getMessage(), isMap);
		}
		
		this.assertLocation(testName, isMap, expected, result, contextFile);
	}
	
	protected void assertLocation(String testName, boolean isMap, Location expected, Location result, File contextFile) {
		Double jaccardIndex = GeoTools.calcJaccardIndex(expected, result);
		String info = " - " + testName + ": Expected " + expected + "; Found " + result;
		if (!isMap) {
			System.out.println("NOMAP" + info);
		}
		else if (expected == null) {
			System.out.println("NOLOC" + info);
			Assert.assertNull("Found location for a non-map", result);
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
			StudyResults studyResults = getStudyResultsForFigure(figure);
			Location expectedLocation = null;
			try {
				expectedLocation = studyResults.getLocation();
				if (expectedLocation != null) {
					locations.add(expectedLocation);
				}
			} catch (InconsistencyException ex) {
				Assert.assertNotNull(ex.getMessage(), expectedLocation);
			}
		}
		return locations.getMostLikelyLocation();
	}

	public static StudyResults getStudyResultsForFigure(Figure figure) {
		File metaFile = getFigureMetaFile(figure);
		if (!metaFile.exists()) {
			return null;
		}

		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(metaFile, StudyResults.class);
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
		long timeStart = System.currentTimeMillis();
		Document doc = instance.runSingle(documentFile, page);
		testCount++;
		testTimeSum += System.currentTimeMillis() - timeStart;
		return doc;
	}
	
	public static void resetBenchmark() {
		testCount = 0;
		testTimeSum = 0;
	}
	
	public static String getBenchmark() {
		if (testCount == 0) {
			return "No tests benchmarked.";
		}
		return "Number of tests: " + testCount + System.lineSeparator() +
				"Runtime: " + (testTimeSum / 1000) + " seconds" + System.lineSeparator() + 
				"Avg. runtime per test: " + (testTimeSum / (testCount * 1000)) + "seconds";
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
		GeodataExtractor extractor = new GeodataExtractor(strategy);
		extractor.enableCaching(true);
		Collection<Object[]> list = new ArrayList<>();
		File[] files = BasePublicationTest.DOC_FOLDER.listFiles(new FileExtension.Filter("pdf"));
		for (File file : files) {
			Document document = extractor.runSingle(file);
			FigureCollection figures = document.getFigures();
			for (Figure figure : figures) {
				list.add(new Object[]{figure});
			}
		}
		return list;
	}
	
	public static class InconsistencyException extends Exception {
		
		private final StudyResults container;
		
		public InconsistencyException(StudyResults container, String message) {
			super(message);
			this.container = container;
		}
		
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class StudyResults {
		public String document;
		public String graphic;
		public List<StudyItem> study;

		public StudyResults() {}
		
		public boolean isMap() throws InconsistencyException {
			Boolean value = null;
			for(StudyItem item : study) {
				if (value == null) {
					value = item.isMap();
				}
				else if(value != item.isMap()) {
					throw new InconsistencyException(this, "Inconsistency in isMap");
				}
			}
			return value;
		}
		
		public boolean isFigure() throws InconsistencyException {
			Boolean value = null;
			for(StudyItem item : study) {
				if (value == null) {
					value = item.isFigure();
				}
				else if(value != item.isFigure()) {
					throw new InconsistencyException(this, "Inconsistency in isValidFigure");
				}
			}
			return value;
		}

		public Location getLocation() throws InconsistencyException {
			LocationCollection collection = new LocationCollection();
			if (study != null) {
				for(StudyItem item : study) {
					Location location = item.getLocation();
					if (location != null) {
						collection.add(location);
					}
				}
			}
			// ToDo: Consistency check
			return collection.getMostLikelyLocation();
		}
	}
	
	public static class StudyItem {
		public String isMap;
		public String isFigure;
		public Double minlon;
		public Double maxlon;
		public Double minlat;
		public Double maxlat;

		public StudyItem() {}

		public boolean isMap() {
			return isMap.equals("1");
		}

		public boolean isFigure() {
			return isFigure.equals("1");
		}

		public Location getLocation() {
			if (minlon == null || maxlon == null || minlat == null || maxlat == null) {
				return null;
			}
			return new Location(minlon, maxlon, minlat, maxlat);
		}
	}

}
