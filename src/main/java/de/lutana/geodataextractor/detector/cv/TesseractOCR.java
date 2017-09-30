package de.lutana.geodataextractor.detector.cv;

import de.lutana.geodataextractor.Config;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TesseractOCR extends Tesseract {
	
	public static final String[] resourceFiles = new String[] {
			// Custom words and patterns for Tesseract
			// See https://stackoverflow.com/questions/17209919/tesseract-user-patterns for user pattern syntax
			"eng.user-words", "eng.user-patterns",
			// Training data for the slower Cube OCR mode
			"eng.cube.bigrams", "eng.cube.fold", "eng.cube.lm", "eng.cube.nn",
			"eng.cube.params", "eng.cube.size", "eng.cube.size", "eng.cube.word-freq"
		};
	
	public static File dataFolder = null;

	private static TesseractOCR instance = null;
	
	public static TesseractOCR getInstance() {
		if (instance == null) {
			instance = new TesseractOCR();
			// Extract language data and copy custom rules
			dataFolder = LoadLibs.extractTessResources("tessdata"); // Maven build bundles English data
			extractCustomTessResources(dataFolder);
			instance.setDatapath(dataFolder.getAbsolutePath());
		}
		return instance;
	}

	private static void extractCustomTessResources(File tessDataFolder) {
		for (String name : resourceFiles) {
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
	
	public void setOcrEngineMode(boolean fastOcrMode) {
		Logger l = LoggerFactory.getLogger(getClass());
		int mode = TessAPI.TessOcrEngineMode.OEM_TESSERACT_ONLY;
		if (!Config.isOcrFastModeEnabled() && this.hasCubeFiles()) {
			try {
				this.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_TESSERACT_CUBE_COMBINED);
				this.doOCR(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
				mode =  TessAPI.TessOcrEngineMode.OEM_TESSERACT_CUBE_COMBINED;
			} catch(Error | TesseractException e) {
				l.error(e.getMessage());
			}
		}
		l.debug("Tesseract OCR Engine Mode: " + (mode == TessAPI.TessOcrEngineMode.OEM_TESSERACT_ONLY ? "Tesseract only" : " Tesseract + Cube"));
		this.setOcrEngineMode(mode);
	}
	
	private boolean hasCubeFiles() {
		for (String name : resourceFiles) {
			if (name.contains(".cube")) {
				File file = new File(dataFolder, name);
				if (!file.exists()) {
					return false;
				}
			}
		}
		return true;
	}
	
}
