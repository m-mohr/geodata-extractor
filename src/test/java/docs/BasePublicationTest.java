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
import de.lutana.geodataextractor.entity.locationresolver.JackardIndexResolver;
import de.lutana.geodataextractor.strategy.NullStrategy;
import de.lutana.geodataextractor.strategy.Strategy;
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
import org.slf4j.LoggerFactory;

public abstract class BasePublicationTest {

	public static final Double JACCARD_INDEX_THRESHOLD = 0.001;
	public static final File DOC_FOLDER = new File("./test-docs/");
	
	private static int benchmarkCount = 0;
	private static long benchmarkTimeSum = 0;
	private static int testCount = 0;
	private static int testsTruePos = 0;
	private static int testsTrueNeg = 0;
	private static int testsFalsePos = 0;
	private static int testsFalseNeg = 0;
	private static List<Double> results;

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
			System.out.println("NOMAP - " + testName);
		}
		else if (expected == null) {
			System.out.println("NOLOC" + info);
			addTestResult(0);
			Assert.assertNull("No location available for the map", result);
		}
		else {
			boolean success = (jaccardIndex > JACCARD_INDEX_THRESHOLD);
			Double formatted = Math.round(jaccardIndex * 100d) / 100d;
			addTestResult(jaccardIndex);
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
		return locations.resolveLocation(new JackardIndexResolver());
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
	
	public static void resetTestEnv() {
		benchmarkCount = 0;
		benchmarkTimeSum = 0;
		testCount = 0;
		testsTruePos = 0;
		testsTrueNeg = 0;
		testsFalsePos = 0;
		testsFalseNeg = 0;
		results = new ArrayList<>();
	}
	
	public static void addBenchmark(double time) {
		benchmarkCount++;
		benchmarkTimeSum += time;
	}
	
	public static void addTestResult(double result) {
		if (results == null) {
			LoggerFactory.getLogger("BasePublicationTest").warn("Test env. not initialized. Test results can't be calculated.");
			return;
		}
		results.add(result);
	}
	
	public static void addTestResults(boolean expected, boolean result) {
		testCount++;
		if (expected) {
			if (result) {
				testsTruePos++;
			}
			else {
				testsFalsePos++;
			}
		}
		else {
			if (result) {
				testsFalseNeg++;
			}
			else {
				testsTrueNeg++;
			}
		}
	}

	public static String getTestResults() {
		if (testCount == 0 && benchmarkCount == 0 && results.isEmpty()) {
			return "No tests evaluated." + System.lineSeparator();
		}
		String data = "";
		if (testCount > 0) {
			double precision = (double) testsTruePos / (double) (testsTruePos + testsFalsePos);
			double recall = (double) testsTruePos / (double) (testsTruePos + testsFalseNeg);
			double f1 = 2d * ( precision * recall ) / (precision + recall);
			double accuracy = (double) (testsTruePos + testsTrueNeg) / (double) testCount;
			double tnr = (double) testsTrueNeg / (double) (testsTrueNeg + testsFalsePos);
			data += "Number of tests: " + testCount + " (TP: " + testsTruePos + ", TN: " + testsTrueNeg + ", FP: " + testsFalsePos + ", FN: " + testsFalseNeg + ")" + System.lineSeparator() +
				"Accuracy: " + accuracy + System.lineSeparator() +
				"True negative rate: " + tnr + System.lineSeparator() +
				"Precision: " + precision + System.lineSeparator() +
				"Recall: " + recall + System.lineSeparator() +
				"F1-Score: " + f1 + System.lineSeparator();
		}
		if (!results.isEmpty()) {
			int excellent = 0;
			int good = 0;
			int fair = 0;
			int poor = 0;
			int wrong = 0;
			
			double sum1 = 0;
			double sum2 = 0;
			for(Double v : results) {
				long v2 = Math.round(v * 100);
				sum1 += v;
				if (v2 == 0) {
					wrong++;
				}
				else if (v2 <= 25) {
					poor++;
					sum2 += v;
				}
				else if (v2 <= 50) {
					fair++;
					sum2 += v;
				}
				else if (v2 <= 75) {
					good++;
					sum2 += v;
				}
				else if (v2 <= 100) {
					excellent++;
					sum2 += v;
				}
				else {
					throw new IndexOutOfBoundsException();
				}
			}
			double avgJI1 = sum1 / results.size();
			double avgJI2 = sum2 / (results.size() - wrong);
			
			data += "Number of tests: " + results.size() + System.lineSeparator() +
				"Wrong    : " + wrong + System.lineSeparator() +
				"Poor     : " + poor + System.lineSeparator() +
				"Fair     : " + fair + System.lineSeparator() +
				"Good     : " + good + System.lineSeparator() +
				"Excellent: " + excellent + System.lineSeparator() +
				"Avg. JI (all)       : " + avgJI1 + System.lineSeparator() +
				"Avg. JI (w/o wrong) : " + avgJI2 + System.lineSeparator();
		}
		if (benchmarkCount > 0) {
			data += "Number of runs: " + benchmarkCount + System.lineSeparator() +
				"Total runtime: " + ((double) benchmarkTimeSum / 1000) + " seconds" + System.lineSeparator() + 
				"Avg. runtime per test: " + ((double) benchmarkTimeSum / (double) (benchmarkCount * 1000)) + " seconds" + System.lineSeparator();
		}
		return data;
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
	
	public static Collection<Object[]> getInconsistencies() {
		Collection<Object[]> figures = getAllFiguresWithStrategy(new NullStrategy());
		Collection<Object[]> list = new ArrayList<>();
		for (Object[] data : figures) {
			Figure figure = (Figure) data[0];
			StudyResults sr = BasePublicationTest.getStudyResultsForFigure(figure);
			List<String> where = new ArrayList<>();
			if (sr.getParticipants() < 2) {
				where.add("# Participants");
			}
			try {
				sr.isMap();
			} catch(InconsistencyException e) {
				where.add(e.toString());
			}
			try {
				sr.isFigure();
			} catch(InconsistencyException e) {
				where.add(e.toString());
			}
			try {
				sr.getLocation();
			} catch(InconsistencyException e) {
				where.add(e.toString());
			}
			list.add(new Object[]{figure, where});
		}
		return list;
	}

	public static Document getDocument(String documentPath) {
		return getDocument(getDocumentFile(documentPath), null, null);
	}
	
	public static Document getDocument(String documentPath, Integer page) {
		return getDocument(getDocumentFile(documentPath), null, page);
	}
	
	public static Document getDocument(File documentFile, Strategy strategy, Integer page) {
		if (strategy == null) {
			strategy = Config.getStrategy();
		}
		GeodataExtractor instance = new GeodataExtractor(strategy);
		instance.enableCaching(true);
		long timeStart = System.currentTimeMillis();
		Document doc = instance.runSingle(documentFile, page);
		addBenchmark(System.currentTimeMillis() - timeStart);
		return doc;
	}
	
	public static Collection<Object[]> getAllDocumentsWithStrategy(Strategy strategy) {
		Collection<Object[]> list = new ArrayList<>();
		File[] files = BasePublicationTest.DOC_FOLDER.listFiles(new FileExtension.Filter("pdf"));
		for (File file : files) {
			Document document = getDocument(file, strategy, null);
			list.add(new Object[]{document});
		}
		return list;
	}
	
	public static Collection<Object[]> getAllFiguresWithStrategy(Strategy strategy) {
		Collection<Object[]> list = new ArrayList<>();
		File[] files = BasePublicationTest.DOC_FOLDER.listFiles(new FileExtension.Filter("pdf"));
		for (File file : files) {
			Document document = getDocument(file, strategy, null);
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
		
		@Override
		public String toString() {
			return super.getMessage() + "[id: " + container.fid + ", file: " + container.graphic + "]";
		}
		
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class StudyResults {
		public Integer fid;
		public String document;
		public String graphic;
		public List<StudyItem> study;
		public Boolean coordinates;

		public StudyResults() {}
		
		public Integer getFigureId() {
			return this.fid;
		}
		
		public Boolean hasCoordinates() {
			return this.coordinates;
		}
		
		public int getParticipants() {
			return this.study.size();
		}
		
		public boolean isMap() throws InconsistencyException {
			int trueCount = 0;
			int falseCount = 0;
			for(StudyItem item : study) {
				if (item.isMap()) {
					trueCount++;
				}
				else {
					falseCount++;
				}
			}
			Boolean value = this.getDominantBoolean(trueCount, falseCount);
			if(value == null) {
				throw new InconsistencyException(this, "Inconsistency in isMap");
			}
			return value;
		}
		
		public boolean isFigure() throws InconsistencyException {
			int trueCount = 0;
			int falseCount = 0;
			for(StudyItem item : study) {
				if (item.isFigure()) {
					trueCount++;
				}
				else {
					falseCount++;
				}
			}
			Boolean value = this.getDominantBoolean(trueCount, falseCount);
			if(value == null) {
				throw new InconsistencyException(this, "Inconsistency in isValidFigure");
			}
			return value;
		}
		
		protected Boolean getDominantBoolean(int trueCount, int falseCount) {
			double parts = 100d / (trueCount + falseCount);
			double truePercent = parts * trueCount;
			double falsePercent = parts * falseCount;
			if(Math.max(truePercent, falsePercent) > 66) {
				return (truePercent > falsePercent);
			}
			return null;
		}

		public Location getLocation() throws InconsistencyException {
			if (study == null) {
				return null;
			}
			LocationCollection collection = new LocationCollection();
			int[] inconsistenciesSub = new int[study.size()];
			int inconsistencies = 0;
			int allowedInconsistencies = (int) (0.66 * study.size());
			for(int i = 0; i < study.size(); i++) {
				StudyItem item = study.get(i);
				Location location = item.getLocation();
				if (location == null) {
					continue;
				}
				for(StudyItem item2 : study) {
					if (item == item2) {
						continue;
					}
					Location location2 = item2.getLocation();
					if (location2 == null) {
						continue;
					}

					double jackard = GeoTools.calcJaccardIndex(location, location2);
					if (jackard < 0.5) {
						inconsistenciesSub[i]++;
					}
				}

				if (inconsistenciesSub[i] > allowedInconsistencies) {
					inconsistencies++;
				}
				else {
					collection.add(location);
				}
			}
			if (inconsistencies > allowedInconsistencies) {
				throw new InconsistencyException(this, "Inconsistency in getLocation");
			}
			return collection.resolveLocation(new JackardIndexResolver());
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
