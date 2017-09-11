package de.lutana.geodataextractor.detector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import de.lutana.geodataextractor.Config;
import de.lutana.geodataextractor.detector.coordinates.CoordinateFromText;
import de.lutana.geodataextractor.entity.Graphic;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.detector.coordinates.CoordinateList;
import de.lutana.geodataextractor.detector.coordinates.CoordinateParser;
import de.lutana.geodataextractor.detector.cv.LineParser;
import de.lutana.geodataextractor.detector.cv.OpenCV;
import de.lutana.geodataextractor.util.TesseractOCR;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Word;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.LoggerFactory;

public class CoordinateGraphicDetector implements GraphicDetector {

	private final CoordinateParser parser = new CoordinateParser();
	private static final float MIN_CONFIDENCE = 25f; // in percent
	private static final int MIN_WORD_LENGTH = 3;

	public CoordinateGraphicDetector() {
		TesseractOCR instance = TesseractOCR.getInstance();
		// Sparse is more accurate for randomly located text parts than automatic detection as it assumes bigger text paragraphs.
		instance.setPageSegMode(TessAPI.TessPageSegMode.PSM_SPARSE_TEXT_OSD);
		// Set the OCR mode (slow and more accurate = Cube and Tesseract / fast and more inaccurate = Tesseract only)
		instance.setOcrEngineMode(Config.isOcrFastModeEnabled() ? TessAPI.TessOcrEngineMode.OEM_TESSERACT_ONLY : TessAPI.TessOcrEngineMode.OEM_TESSERACT_CUBE_COMBINED);
		// Avoid word list/dictionaries as geonaames and coordinates are not in those lists
		instance.setTessVariable("load_system_dawg", "false");
		instance.setTessVariable("load_freq_dawg", "false");
		// Limit characters to the ones used for coordinates, especially to avoid confusion between - and _, dot and comma, ° and o etc.
		instance.setTessVariable("tessedit_char_whitelist", "-.°'\"1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	}

	@Override
	public void detect(Graphic graphic, LocationCollection locations) {
		try {
			BufferedImage img = graphic.getBufferedImage();
			List<Word> words = TesseractOCR.getInstance().getWords(img, TessPageIteratorLevel.RIL_WORD);

			// Add a text with a directory of words
			Text textBuilder = new Text();
			for (Word word : words) {
				String text = word.getText();
				if (text.length() < MIN_WORD_LENGTH || word.getConfidence() < MIN_CONFIDENCE) {
					continue;
				}
				textBuilder.add(word);
			}
			LoggerFactory.getLogger(this.getClass()).debug(textBuilder.getText());

			// Combine coordinates with OCR rectangles
			CoordinateList coords = parser.parse(textBuilder.getText());
			for (int i = 0; i < coords.size(); i++) {
				CoordinateFromText coord = coords.get(i);
				List<Word> textWords = textBuilder.getWordsBetween(coord.getBeginMatch(), coord.getEndMatch());

				// Combine bounding boxes if a coordinate was built from multiple words
				Rectangle rectangle = null;
				for (Word word : textWords) {
					if (rectangle == null) {
						rectangle = word.getBoundingBox();
					} else {
						rectangle.add(word.getBoundingBox());
					}
				}

				CoordinateFromOcr newCoord = new CoordinateFromOcr(coord, rectangle);
				coords.set(i, newCoord);
				LoggerFactory.getLogger(this.getClass()).debug(newCoord.toString());
			}

			// Remove obvious outliers
			coords.removeOutliers();
			// Try to get location using axes and labels
			Location location = coords.getLocation();
			if (location != null) {
				this.improveLocationUsingAxes(img, coords, location);
				locations.add(location);
			}

		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			System.out.println("Tess4J not installed correctly, please visit http://tess4j.sourceforge.net/usage.html for instructions.");
		}
	}
	
	public void improveLocationUsingAxes(BufferedImage img, CoordinateList coords, Location baseLocation) {
		// Do countour finding to get the axes
		LineParser lp = OpenCV.getInstance().createLineParser(img);
		List<LineSegment> lines = lp.parse();
		if (lines.isEmpty()) {
			return;
		}
		
		List<Axis> axes = new ArrayList<>();
		for(LineSegment line : lines) {
			axes.add(new Axis(line));
		}

		for (CoordinateFromText coord : coords) {
			CoordinateFromOcr ocrCoord = (CoordinateFromOcr) coord;
			if (ocrCoord == null) {
				continue;
			}

			Coordinate c = ocrCoord.getBoundingBoxCenter();

			Axis nearestAxis = null;
			double minDistanceFromNearestAxis = Double.MAX_VALUE;
			for(Axis axis : axes) {
				LineSegment l = axis.getLine();
				// Check whether the text is not inside the line bounds => reject it
				if (l.isHorizontal() && (c.x < l.minX() || c.x > l.maxX())) {
					continue;
				}
				else if (l.isVertical() && (c.y < l.minY() || c.y > l.maxY())) {
					continue;
				}
				
				Coordinate snappedPoint = axis.getLine().closestPoint(c);
				double distance = snappedPoint.distance(c);
				if (minDistanceFromNearestAxis > distance) {
					nearestAxis = axis;
					minDistanceFromNearestAxis = distance;
				}
			}

			ocrCoord.setNearestAxis(nearestAxis);
		}
		
		for (CoordinateFromText coord : coords) {
			CoordinateFromOcr ocrCoord = (CoordinateFromOcr) coord;
			if (ocrCoord == null) {
				continue;
			}
			Axis axis = ocrCoord.getNearestAxis();
			if (axis != null) {
				axis.addCoordinate(ocrCoord);
			}
		}
		
		double xMin = Double.NaN;
		double xMax = Double.NaN;
		double yMin = Double.NaN;
		double yMax = Double.NaN;
		for(Axis axis : axes) {
			if (!axis.hasCoordinates()) {
				continue;
			}
			LoggerFactory.getLogger(this.getClass()).debug(axis.toString());
			double length = axis.getLine().getLength();
			if (axis.getLine().isHorizontal()) {
				Double xMinPred = axis.predictValue(0);
				if (Double.isNaN(xMin) || xMin > xMinPred) {
					xMin = xMinPred;
				}
				Double xMaxPred = axis.predictValue(length);
				if (Double.isNaN(xMax) || xMax < xMaxPred) {
					xMax = xMaxPred;
				}
			}
			else {
				Double yMinPred = axis.predictValue(0);
				if (Double.isNaN(yMin) || yMin > yMinPred) {
					yMin = yMinPred;
				}
				Double yMaxPred = axis.predictValue(length);
				if (Double.isNaN(yMax) || yMax < yMaxPred) {
					yMax = yMaxPred;
				}
			}
		}
		
		if (!Double.isNaN(xMin) && !Double.isNaN(xMax)) {
			baseLocation.setX(xMin, xMax);
		}
		if (!Double.isNaN(yMin) && !Double.isNaN(yMax)) {
			baseLocation.setY(yMin, yMax);
		}
	}
	
	public class Axis {
		
		private List<CoordinateFromOcr> data;
		private SimpleRegression regression;
		private LineSegment line;
		
		public Axis(LineSegment line) {
			this.line = line;
			this.data = new ArrayList<>();
			this.regression = new SimpleRegression();
		}
		
		public LineSegment getLine() {
			return this.line;
		}
		
		public boolean hasCoordinates() {
			return (this.data.size() >= 2);
		}
		
		public void addCoordinate(CoordinateFromOcr coord) {
			if (this.line.isHorizontal() && coord.getLongitude() != null) {
				this.data.add(coord);
				double x = coord.getBoundingBoxCenter().x - this.line.minX();
				this.regression.addData(x, coord.getLongitude());
			}
			else if (this.line.isVertical() && coord.getLatitude() != null) {
				this.data.add(coord);
				double x = coord.getBoundingBoxCenter().y - this.line.minY();
				this.regression.addData(x, coord.getLatitude());
			}
		}
		
		public double predictValue(double x) {
			double significance = this.regression.getSignificance();
			if (!Double.isNaN(this.regression.getSlope()) && (Double.isNaN(significance) || significance < 0.1)) {
				return this.regression.predict(x);
			}
			return Double.NaN;
		}
		
		@Override
		public String toString() {
			return "["+this.line+" with "+this.data+"]";
		}
		
	}

	public class CoordinateFromOcr extends CoordinateFromText {

		private Envelope env;
		private Axis nearestAxis;

		public CoordinateFromOcr(CoordinateFromText coord, Rectangle rect) {
			this(coord, new Envelope(rect.getMinX(), rect.getMaxX(), rect.getMinY(), rect.getMaxY()));
		}

		public CoordinateFromOcr(CoordinateFromText coord, Envelope env) {
			super(coord);
			this.env = env;
		}

		public Envelope getBoundingBox() {
			return this.env;
		}

		private Coordinate getBoundingBoxCenter() {
			return env.centre();
		}

		public Axis getNearestAxis() {
			return nearestAxis;
		}

		public void setNearestAxis(Axis nearest) {
			this.nearestAxis = nearest;
		}

	}

	public class Text {

		private final HashMap<Integer, Word> words;
		private final StringBuilder text;

		public Text() {
			this.words = new HashMap<>();
			this.text = new StringBuilder();
		}

		public void add(Word word) {
			// This order is important: First get current length of text then append word to text.
			this.words.put(this.text.length(), word);
			this.text.append(word.getText());
			this.text.append(" ");
		}

		public List<Word> getWordsBetween(int begin, int end) {
			List<Word> list = new ArrayList<>();
			Iterator<Entry<Integer, Word>> it = this.words.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, Word> entry = it.next();
				if (entry.getKey() >= begin && entry.getKey() <= end) {
					list.add(entry.getValue());
				}
			}
			return list;
		}

		public String getText() {
			return text.toString();
		}

		@Override
		public String toString() {
			return text.toString();
		}

	}

}
