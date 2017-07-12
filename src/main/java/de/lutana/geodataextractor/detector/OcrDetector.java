package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Graphic;
import de.lutana.geodataextractor.entity.LocationCollection;
import java.io.File;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;

public class OcrDetector implements GraphicDetector {
	
	private final CoordinateDetector cd = new CoordinateDetector();
	private static ITesseract instance = null;
	
	public static ITesseract getTesseract() {
        if (instance == null) {
			instance = new Tesseract();
			File tessDataFolder = LoadLibs.extractTessResources("tessdata"); // Maven build bundles English data
			instance.setDatapath(tessDataFolder.getParent());
		}
		return instance;
	}
	
	@Override
	public void detect(Graphic graphic, LocationCollection locations) {
        try {
            String result = getTesseract().doOCR(graphic.getBufferedImage());
			cd.detect(result, locations);
			// TODO: More to come...
			graphic.freeBufferedImage();
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
	}
	
}
