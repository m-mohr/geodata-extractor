package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.detector.cv.CvGraphic;
import de.lutana.geodataextractor.util.GeoTools;
import de.lutana.geodataextractor.detector.cv.TesseractOCR;
import de.lutana.geodataextractor.detector.gazetteer.GeoName;
import de.lutana.geodataextractor.detector.gazetteer.LuceneIndex;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;
import net.sourceforge.tess4j.Word;
import org.opencv.core.Rect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoNamesGraphicDetector implements GraphicDetector {

	private LuceneIndex index;
	private boolean fuzzyIfNoResultsMode;

	public GeoNamesGraphicDetector(LuceneIndex index) {
		this.index = index;
		this.fuzzyIfNoResultsMode = false;
	}

	@Override
	public boolean detect(CvGraphic graphic, LocationCollection locations, double weight) {
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
		for(Word w : words) {
			String text = w.getText().trim();
			// We find much garbage that leads to unuseful results from the Gazetteer
			// and need ideas to filter some garbage, e.g. ... (see following comments)
			// - Ignore all words too short
			if (text.length() < 3) {
				continue;
			}
			// - ignore all entries that don't start with an uppercase letter
			else if (!Character.isUpperCase(text.charAt(0))) {
				continue;
			}
			// - Remove all entries containing more than one digit
			int numDigits = 0;
			// - Remove all entries containing more than one non letter per space or hyphen divided word
			int numNonWhiteLetter = 0;
			for(int i = 0; (i < text.length() && numDigits < 2 && numNonWhiteLetter < 2); i++) {
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
			
			List<GeoName> results = index.find(text, fuzzyIfNoResultsMode, 3);
			int i = 1;
			for (GeoName geoname : results) {
				Location l = geoname.getLocation();
				if (l != null) {
					// 0.0 base probability,
					// added by a max. of 0.25 depending on the text recognition confidence, 
					// added by a max. of 0.25 depending on the amount of search results, 
					// added by a max. of 0.25 depending on the list position of the search result
					// added by a max. of 0.25 depending on the importance (from the database)
					l.setProbability(w.getConfidence() / 400 + 0.25 / results.size() + 0.25 / i + geoname.getImportance() / 4);
					logger.debug("Parsed location from '" + text + "' as '" + geoname.getDisplayName()+ "' at " + l + " using GeoNamesGraphicDetector.");
					candidates.add(l);
				}
				i++;
			}
		}
		
		if (candidates.isEmpty()) {
			return false;
		}

		// Remove outliers - this one is a bit tricky.
		// At the moment we remove all entries that are outside the union of previous results.
		// ToDo: Improve this
		if (locations.size() > 0) {
			LocationCollection filteredCandidates = new LocationCollection();
			Location restrictingArea = locations.getUnifiedLocation();
			for(Location l : candidates) {
				if (l.intersects(restrictingArea)) {
					filteredCandidates.add(l);
				}
			}
			candidates = filteredCandidates;
		}

		// Merge remaining candidates
		Location union = candidates.getUnifiedLocation();
		if (union != null) {
			// Give this probability a bump if it was created using many locations.
			union.setProbability(0.1 * Math.min(candidates.size(), 5) + union.getProbability() / 2);
			union.setWeight(weight);
			locations.add(union);
			logger.debug("Merged to final location " + union + " in GeoNamesGraphicDetector.");
			return true;
		}
		return false;
	}

}
