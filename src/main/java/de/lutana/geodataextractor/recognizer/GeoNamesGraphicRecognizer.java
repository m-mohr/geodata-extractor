package de.lutana.geodataextractor.recognizer;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.recognizer.cv.CvGraphic;
import de.lutana.geodataextractor.util.GeoTools;
import de.lutana.geodataextractor.recognizer.cv.TesseractOCR;
import de.lutana.geodataextractor.recognizer.gazetteer.GeoName;
import de.lutana.geodataextractor.recognizer.gazetteer.LuceneIndex;
import de.lutana.geodataextractor.recognizer.nlp.Dict;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;
import net.sourceforge.tess4j.Word;
import org.opencv.core.Rect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoNamesGraphicRecognizer implements GraphicRecognizer {

	private LuceneIndex index;
	private boolean fuzzyIfNoResultsMode;

	public GeoNamesGraphicRecognizer(LuceneIndex index) {
		this.index = index;
		this.fuzzyIfNoResultsMode = false;
	}

	@Override
	public boolean recognize(CvGraphic graphic, LocationCollection locations, double weight) {
		Logger logger = LoggerFactory.getLogger(this.getClass());
		
		// ToDo: Settings are affected globally, this might have side-effects when used in threads, ...
		TesseractOCR.getInstance().optimizeForGeoNames();

		BufferedImage img = graphic.getBufferedImage();

		List<Rect> rects = graphic.getTextBoxes();
		if (rects.size() > 100) {
			return false; // TODO
		}

		List<Word> words = new ArrayList<>();
		try {
			for(Rect rect : rects) {
				int margin = Math.round(rect.height / 4);
				rect = GeoTools.addMargin(rect, margin, margin, img.getWidth(), img.getHeight());
				BufferedImage subImg = img.getSubimage(rect.x, rect.y, rect.width, rect.height);
				List<Word> parts = TesseractOCR.getInstance().getWords(subImg, TessPageIteratorLevel.RIL_BLOCK);
				// The bbox from Tesseract relates to the sub image(!).
				// To get the bbox for the whole image add the offset/position of the sub image to the detected bbox.
				for(Word w : parts) {
					Rectangle r = w.getBoundingBox();
					r.x = rect.x + r.x;
					r.y = rect.y + r.y;
					words.add(new Word(w.getText(), w.getConfidence(), r));
				}
			}
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			logger.error("Tess4J not installed correctly, please visit http://tess4j.sourceforge.net/usage.html for instructions.");
			return false;
		}

		LocationCollection candidates = new LocationCollection();
		Rectangle candidateBounds = null;
		for(Word w : words) {
			String text = w.getText().trim();
			int len = text.length();
			// We find much garbage that leads to unuseful results from the Gazetteer
			// and need ideas to filter some garbage, e.g. ... (see following comments)
			// - Ignore all words too short
			if (len < 3) {
				continue;
			}
			// - ignore all entries that don't start with an uppercase letter
			else if (!Character.isUpperCase(text.charAt(0))) {
				continue;
			}
			// - ignore all entries that end with a non-letter
			else if (!Character.isAlphabetic(text.charAt(len - 1))) {
				continue;
			}
			// - Remove all entries containing more than one digit
			int numDigits = 0;
			// - Remove all entries containing more than one non letter per space or hyphen divided word
			int numNonWhiteLetter = 0;
			for(int i = 0; (i < len && numDigits < 2 && numNonWhiteLetter < 2); i++) {
				char c = text.charAt(i);
				if (Character.isDigit(c)) {
					numDigits++;
				}
				if (Character.isSpaceChar(c) || c == '-') {
					numNonWhiteLetter = 0;
				}
				if (!Character.isLetter(c)) {
					numNonWhiteLetter++;
				}
			}
			if (numDigits >= 2 || numNonWhiteLetter >= 2) {
				continue;
			}
			else if (Dict.contains(text)) {
				continue;
			}
			
			Location restrictingArea = null;
			if (locations.size() > 0) {
				restrictingArea = locations.getUnifiedLocation();
			}
			
			List<GeoName> results = index.find(text, fuzzyIfNoResultsMode, 3);
			int i = 1;
			for (GeoName geoname : results) {
				Location l = geoname.getLocation();
				if (l != null) {
					// Remove outliers - this one is a bit tricky.
					// At the moment we remove all entries that are outside the union of previous results.
					// ToDo: Improve this
					if (restrictingArea != null && !l.intersects(restrictingArea)) {
						continue;
					}
					
					// 0.0 base probability,
					// added by a max. of 0.25 depending on the text recognition confidence, 
					// added by a max. of 0.1 depending on the amount of search results, 
					// added by a max. of 0.4 depending on the list position of the search result
					// added by a max. of 0.25 depending on the importance (from the database)
					l.setProbability(w.getConfidence() / 400 + 0.1 / results.size() + 0.4 / i + geoname.getImportance() / 4);
					l.setWeight(1);
					logger.debug("Parsed location from '" + text + "' as '" + geoname.getDisplayName()+ "' at " + l + " using GeoNamesGraphicRecognizer.");
					candidates.add(l);
					
					Rectangle wordBounds = w.getBoundingBox();
					int size = (int) Math.round(Math.min(wordBounds.getWidth(), wordBounds.getHeight()));
					Point centroid = new Point((int) Math.round(wordBounds.getCenterX()), (int) Math.round(wordBounds.getCenterY()));
					Rectangle wordRect = new Rectangle(centroid, new Dimension(size, size));
					if (candidateBounds == null) {
						candidateBounds = wordRect;
					}
					else {
						candidateBounds.add(wordRect);
					}
				}
				i++;
			}
		}
		
		double scaleTop = 0.5, scaleBottom = 0.5, scaleLeft = 0.5, scaleRight = 0.5;
		if (candidateBounds != null && candidateBounds.getHeight() > 0 && candidateBounds.getWidth() > 0) {
			// Calculate how much space the words take in the image to enlarge the bbox later
			double imgW = graphic.getWidth();
			double imgH = graphic.getHeight();
			double scaleX = 0.4 * (imgW / candidateBounds.getWidth());
			double scaleY = 0.4 * (imgH / candidateBounds.getHeight());
			scaleTop = candidateBounds.getMinY() / imgH * scaleY;
			scaleBottom = (imgH - candidateBounds.getMaxY()) / imgH * scaleY;
			scaleLeft = candidateBounds.getMinX() / imgW * scaleX;
			scaleRight = (imgW - candidateBounds.getMaxX()) / imgW * scaleX;
		}
		
		Location union = candidates.getUnifiedLocation();
		if (union != null) {
			// Enlarge the bounding box based on the label relations in the image
			double uW = union.getWidth();
			double uH = union.getHeight();
			union.setX(union.getMinX() - scaleLeft * uW, union.getMaxX() + scaleRight * uW);
			union.setY(union.getMinY() - scaleBottom * uH, union.getMaxY() + scaleTop * uH);
			union.makeValid();
			// Gives locations a probability bump if it was created using multiple locations.
			union.setProbability(0.2 * Math.min(locations.size(), 3) + 0.4 * union.getProbability());
			union.setWeight(weight);
			logger.debug("Merged to final location " + union + " in GeoNamesGraphicRecognizer.");
			locations.add(union);
			return true;
		}
		return false;
	}

	/**
	 * @return the index
	 */
	public LuceneIndex getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(LuceneIndex index) {
		this.index = index;
	}

	/**
	 * @return the fuzzyIfNoResultsMode
	 */
	public boolean isFuzzyIfNoResultsMode() {
		return fuzzyIfNoResultsMode;
	}

	/**
	 * @param fuzzyIfNoResultsMode the fuzzyIfNoResultsMode to set
	 */
	public void setFuzzyIfNoResultsMode(boolean fuzzyIfNoResultsMode) {
		this.fuzzyIfNoResultsMode = fuzzyIfNoResultsMode;
	}

}
