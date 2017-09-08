package de.lutana.geodataextractor.util;

import de.lutana.geodataextractor.detector.OcrDetector;
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
	
	
}
