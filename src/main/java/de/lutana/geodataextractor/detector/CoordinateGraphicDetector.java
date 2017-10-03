package de.lutana.geodataextractor.detector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import de.lutana.geodataextractor.detector.coordinates.CoordinateFromText;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.detector.coordinates.CoordinateList;
import de.lutana.geodataextractor.detector.coordinates.CoordinateParser;
import de.lutana.geodataextractor.detector.cv.CvException;
import de.lutana.geodataextractor.detector.cv.CvGraphic;
import de.lutana.geodataextractor.detector.cv.CvLineDetector;
import de.lutana.geodataextractor.detector.cv.MapAxesLineDetector;
import de.lutana.geodataextractor.util.GeoTools;
import de.lutana.geodataextractor.detector.cv.TesseractOCR;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;
import net.sourceforge.tess4j.Word;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.opencv.core.Rect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoordinateGraphicDetector implements GraphicDetector {

	private final CoordinateParser parser = new CoordinateParser();
	private static final float MIN_CONFIDENCE = 25f; // in percent
	private static final int MIN_WORD_LENGTH = 3;

	@Override
	public void detect(CvGraphic graphic, LocationCollection locations) {
		Logger logger = LoggerFactory.getLogger(this.getClass());
	
		// ToDo: Settings are affected globally, this might have side-effects when used in threads, ...
		TesseractOCR.getInstance().optimizeForCoordinates();
		
		BufferedImage img = graphic.getBufferedImage();
		int width = img.getWidth();
		int height = img.getHeight();

		List<Rect> rects = graphic.getTextBoxes();
		if (rects.size() > 100) {
			return; // TODO
		}

		List<Word> words = new ArrayList<>();
		try {
			for(Rect rect : rects) {
				int margin = Math.round(rect.height / 4);
				rect = GeoTools.addMargin(rect, margin, margin, width, height);
				BufferedImage subImg = img.getSubimage(rect.x, rect.y, rect.width, rect.height);
				List<Word> subWords = TesseractOCR.getInstance().getWords(subImg, TessPageIteratorLevel.RIL_WORD);
				// The bbox from Tesseract relates to the sub image(!).
				// To get the bbox for the whole image add the offset/position of the sub image to the detected bbox.
				for(Word w : subWords) {
					Rectangle r = w.getBoundingBox();
					r.x = rect.x + r.x;
					r.y = rect.y + r.y;
					words.add(new Word(w.getText(), w.getConfidence(), r));
				}
			}
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			logger.error("Tess4J not installed correctly, please visit http://tess4j.sourceforge.net/usage.html for instructions.");
			return;
		}

		// Add a text with a directory of words
		Text textBuilder = new Text();
		for (Word word : words) {
			String text = word.getText();
			if (text.length() < MIN_WORD_LENGTH || word.getConfidence() < MIN_CONFIDENCE) {
				continue;
			}
			textBuilder.add(word);
		}
		logger.debug(textBuilder.getText());

		// Combine coordinates with OCR rectangles
		CoordinateList coords = parser.parse(textBuilder.getText(), true);
		for (int i = 0; i < coords.size(); i++) {
			CoordinateFromText coord = coords.get(i);
			List<Word> textWords = textBuilder.getWordsBetween(coord.getBeginMatch(), coord.getEndMatch());

			// Combine bounding boxes if a coordinate was built from multiple words
			double confidenceSum = 0;
			Rectangle rectangle = null;
			for (Word word : textWords) {
				if (rectangle == null) {
					rectangle = word.getBoundingBox();
				} else {
					rectangle.add(word.getBoundingBox());
				}
				confidenceSum += word.getConfidence() / 100;
			}
			if (rectangle == null) {
				logger.debug("Combine coordinates with OCR rectangles failed. No words found.");
				continue;
			}
			double avgConfidence = confidenceSum / textWords.size();
			coord.setProbability((coord.getProbability() + avgConfidence) / 2);

			CoordinateFromOcr newCoord;
			if (coord instanceof CoordinateFromText.UnknownOrientation) {
				newCoord = new CoordinateFromOcrWithUnknownOrientation((CoordinateFromText.UnknownOrientation) coord, rectangle);
			}
			else {
				newCoord = new CoordinateFromOcr(coord, rectangle);
			}
			coords.set(i, newCoord);
		}

		// Remove obvious outliers
		coords.removeOutliers();
		// Try to get location using axes and labels
		Location location = coords.getLocation();
		if (location != null) {
			boolean improved = false;
			try {
				improved = this.improveLocationUsingAxes(graphic, coords, location);
			} catch (CvException e) {
				e.printStackTrace();
			}
			logger.debug("Parsed location " + location + " from graphical coordinates." + (improved ? " Using CV imrprovements." : ""));
			locations.add(location);
		}
	}
	
	public boolean improveLocationUsingAxes(CvGraphic img, CoordinateList coords, Location baseLocation) {
		// Do countour finding to get the axes
		CvLineDetector lp = new MapAxesLineDetector(img);
		List<LineSegment> lines = lp.detect();
		if (lines.isEmpty()) {
			return false;
		}
		
		List<Axis> axes = new ArrayList<>();
		for(LineSegment line : lines) {
			axes.add(new Axis(line));
		}

		for (CoordinateFromText coord : coords) {
			if (!(coord instanceof CoordinateFromOcr)) {
				continue;
			}
			CoordinateFromOcr ocrCoord = (CoordinateFromOcr) coord;
			Coordinate c = ocrCoord.getBoundingBoxCenter();

			Axis nearestAxis = null;
			double minDistanceFromNearestAxis = Double.MAX_VALUE;
			for(Axis axis : axes) {
				LineSegment l = axis.getLine();
				// Check whether the text is not inside the line bounds => reject it
				if (axis.isMostlyHorizontal() && (c.x < l.minX() || c.x > l.maxX())) {
					continue;
				}
				else if (axis.isMostlyVertical() && (c.y < l.minY() || c.y > l.maxY())) {
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
			if (!(coord instanceof CoordinateFromOcr)) {
				continue;
			}
			CoordinateFromOcr ocrCoord = (CoordinateFromOcr) coord;
			Axis axis = ocrCoord.getNearestAxis();
			if (axis == null) {
				continue;
			}
			if (ocrCoord instanceof CoordinateFromOcrWithUnknownOrientation) {
				if (axis.isMostlyHorizontal()) {
					// Probably a longitude value, remove the latitude value
					ocrCoord.setLatitude(null);
					ocrCoord.setProbability(0.5);
				}
				else if (axis.isMostlyVertical()) {
					// Probably a latitude value, remove the longitude value
					ocrCoord.setLongitude(null);
					ocrCoord.setProbability(0.5);
				}
			}
			axis.addCoordinate(ocrCoord);
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
			if (axis.isMostlyHorizontal()) {
				Double xMinPred = axis.predictValue(0);
				Double xMaxPred = axis.predictValue(length);
				if (xMinPred < -200 || xMaxPred > 200) {
					continue; // Completely wrong, ignore it
				}
				if (xMinPred < -180) {
					xMinPred = -180d; // Correct to min. value if slightly over
				}
				if (xMaxPred > 180) {
					xMaxPred = 180d; // Correct to max. value if slightly over
				}
				if (Double.isNaN(xMin) || xMin > xMinPred) {
					xMin = xMinPred;
				}
				if (Double.isNaN(xMax) || xMax < xMaxPred) {
					xMax = xMaxPred;
				}
			}
			else {
				Double yMinPred = axis.predictValue(0);
				Double yMaxPred = axis.predictValue(length);
				if (yMinPred < -100 || yMaxPred > 100) {
					continue; // Completely wrong, ignore it
				}
				if (yMinPred < -90) {
					yMinPred = -90d; // Correct to min. value if slightly over
				}
				if (yMaxPred > 90) {
					yMaxPred = 90d; // Correct to max. value if slightly over
				}
				if (Double.isNaN(yMin) || yMin > yMinPred) {
					yMin = yMinPred;
				}
				if (Double.isNaN(yMax) || yMax < yMaxPred) {
					yMax = yMaxPred;
				}
			}
		}

		int numberOfChanges = 0;
		if (!Double.isNaN(xMin) && !Double.isNaN(xMax)) {
			baseLocation.setX(xMin, xMax);
			numberOfChanges++;
		}
		if (!Double.isNaN(yMin) && !Double.isNaN(yMax)) {
			baseLocation.setY(yMin, yMax);
			numberOfChanges++;
		}
		if (numberOfChanges > 0) {
			baseLocation.setProbability((baseLocation.getProbability() + numberOfChanges) / (1+numberOfChanges));
		}
		
		return (numberOfChanges > 0);
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
		
		public boolean isMostlyHorizontal() {
			return GeoTools.isMostlyHorizontal(this.line.angle(), true);
		}
		
		public boolean isMostlyVertical() {
			return GeoTools.isMostlyVertical(this.line.angle(), true);
		}
		
		public void addCoordinate(CoordinateFromOcr coord) {
			if (this.isMostlyHorizontal() && coord.getLongitude() != null) {
				this.data.add(coord);
				double x = coord.getBoundingBoxCenter().x - this.line.minX();
				this.regression.addData(x, coord.getLongitude());
			}
			else if (this.isMostlyVertical() && coord.getLatitude() != null) {
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
		
	public class CoordinateFromOcrWithUnknownOrientation extends CoordinateFromOcr {

		public CoordinateFromOcrWithUnknownOrientation(CoordinateFromOcr.UnknownOrientation coord, Rectangle rect) {
			super(coord, rect);
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
