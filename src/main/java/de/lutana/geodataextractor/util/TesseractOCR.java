package de.lutana.geodataextractor.util;

import de.lutana.geodataextractor.Config;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.LoadLibs;
import org.apache.commons.io.FileUtils;

public class TesseractOCR extends Tesseract {

	private static TesseractOCR instance = null;
	
	public static TesseractOCR getInstance() {
		if (instance == null) {
			instance = new TesseractOCR();
			// Extract language data and copy custom rules
			File tessDataFolder = LoadLibs.extractTessResources("tessdata"); // Maven build bundles English data
			extractCustomTessResources(tessDataFolder);
			instance.setDatapath(tessDataFolder.getAbsolutePath());
		}
		return instance;
	}

	private static void extractCustomTessResources(File tessDataFolder) {
		String[] fileNames = new String[] {
			// Custom words and patterns for Tesseract
			// See https://stackoverflow.com/questions/17209919/tesseract-user-patterns for user pattern syntax
			"eng.user-words", "eng.user-patterns",
			// Training data for the slower Cube OCR mode
			"eng.cube.bigrams", "eng.cube.fold", "eng.cube.lm", "eng.cube.nn",
			"eng.cube.params", "eng.cube.size", "eng.cube.size", "eng.cube.word-freq"
		};
		for (String name : fileNames) {
			if (Config.isOcrFastModeEnabled() && name.contains(".cube")) {
				continue;
			}
			try {
				File target = new File(tessDataFolder, name);
				if (!target.exists()) {
					URL words = TesseractOCR.class.getClassLoader().getResource("tessdata/" + name);
					FileUtils.copyURLToFile(words, target);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	
}
