package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.detector.coordinates.Coordinate;
import de.lutana.geodataextractor.entity.Graphic;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.detector.coordinates.CoordinateList;
import de.lutana.geodataextractor.detector.coordinates.CoordinateParser;
import de.lutana.geodataextractor.util.TesseractOCR;
import java.awt.Point;
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

public class OcrDetector implements GraphicDetector {

	private final CoordinateParser parser = new CoordinateParser();
	private static final float MIN_CONFIDENCE = 0.25f;
	private static final int MIN_WORD_LENGTH = 3;

	public OcrDetector() {
		TesseractOCR instance = TesseractOCR.getInstance();
		// Sparse is more accurate for randomly located text parts than automatic detection as it assumes bigger text paragraphs.
		instance.setPageSegMode(TessAPI.TessPageSegMode.PSM_SPARSE_TEXT_OSD);
//		instance.setOcrEngineMode(TessAPI.TessOcrEngineMode.OEM_TESSERACT_CUBE_COMBINED); // Cube results in memory errors
		// Avoid word list/dictionaries as geonaames and coordinates are not in those lists
		instance.setTessVariable("load_system_dawg", "false");
		instance.setTessVariable("load_freq_dawg", "false");
		// Force proportional word segmentation on all rows
		instance.setTessVariable("textord_force_make_prop_words", "true");
		// Limit characters to the ones used for coordinates, especially to avoid confusion between - and _, dot and comma, ° and o etc.
		instance.setTessVariable("tessedit_char_whitelist", "-,.°'\"1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	}

	@Override
	public void detect(Graphic graphic, LocationCollection locations) {
		try {
			BufferedImage img = graphic.getBufferedImage();
			List<Word> words = TesseractOCR.getInstance().getWords(img, TessPageIteratorLevel.RIL_WORD);
			graphic.freeBufferedImage();

			Text textBuilder = new Text();
			for (Word word : words) {
				String text = word.getText();
				if (text.length() < MIN_WORD_LENGTH || word.getConfidence() < MIN_CONFIDENCE) {
					continue;
				}

				textBuilder.add(word);
			}

			CoordinateList coords = parser.parse(textBuilder.getText());
			Location location = coords.getLocation(true);
			if (location != null) {
				locations.add(location);
			}

		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			System.out.println("Tess4J not installed correctly, please visit http://tess4j.sourceforge.net/usage.html for instructions.");
		}
	}

	private Point getCenter(Rectangle r) {
		return new Point((int) Math.round(r.getCenterX()), (int) Math.round(r.getCenterY()));
	}

	public class Text {

		private final HashMap<Integer, Word> words;
		private StringBuilder text;

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
			while(it.hasNext()) {
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
