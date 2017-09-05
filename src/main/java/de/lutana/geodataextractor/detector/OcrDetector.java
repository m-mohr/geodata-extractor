package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Graphic;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.util.CoordinateParser;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.LoadLibs;
import org.apache.commons.io.FileUtils;

public class OcrDetector implements GraphicDetector {

	private final CoordinateParser parser = new CoordinateParser(true);
	private static ITesseract instance = null;
	private static final float MIN_CONFIDENCE = 0.25f;
	private static final int MIN_WORD_LENGTH = 3;

	public static ITesseract getTesseract() {
		if (instance == null) {
			instance = new Tesseract();
			// Extract language data and copy custom rules
			File tessDataFolder = LoadLibs.extractTessResources("tessdata"); // Maven build bundles English data
			extractCustomTessResources(tessDataFolder);
			instance.setDatapath(tessDataFolder.getAbsolutePath());
			// Sparse is more accurate for randomly located text parts than automatic detection as it assumes bigger text paragraphs.
			instance.setPageSegMode(TessAPI.TessPageSegMode.PSM_SPARSE_TEXT_OSD);
//			instance.setOcrEngineMode(TessAPI.TessOcrEngineMode.OEM_TESSERACT_CUBE_COMBINED); // Cube results in memory errors
			// Avoid word list/dictionaries as geonaames and coordinates are not in those lists
			instance.setTessVariable("load_system_dawg", "false");
			instance.setTessVariable("load_freq_dawg", "false");
			// Force proportional word segmentation on all rows
			instance.setTessVariable("textord_force_make_prop_words", "true");
			// Limit characters to the ones used for coordinates, especially to avoid confusion between - and _, dot and comma, ° and o etc.
			instance.setTessVariable("tessedit_char_whitelist", "-,.°'\"1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		}
		return instance;
	}

	private static void extractCustomTessResources(File tessDataFolder) {
		String[] fileNames = new String[] {"eng.user-words", "eng.user-patterns"};
		for (String name : fileNames) {
			try {
				File target = new File(tessDataFolder, name);
				if (!target.exists()) {
					URL words = OcrDetector.class.getClassLoader().getResource("tessdata/" + name);
					System.out.println(target);
					FileUtils.copyURLToFile(words, target);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void detect(Graphic graphic, LocationCollection locations) {
		try {
			BufferedImage img = graphic.getBufferedImage();
			List<Word> words = getTesseract().getWords(img, 0);
			// ToDo: This is a temporary workaround as Coordinate Detector needs 
			// lon/lat values in one string to combine them correctly together.
			String all = "";
			for(Word word : words) {
				String text = word.getText();
				if (text.length() >= MIN_WORD_LENGTH && word.getConfidence() >= MIN_CONFIDENCE) {
					all += word.getText() + " ";
				}
			}
			
			parser.parse(all, locations);

			graphic.freeBufferedImage();
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			System.out.println("Tess4J not installed correctly, please visit http://tess4j.sourceforge.net/usage.html for instructions.");
		}
	}

}
