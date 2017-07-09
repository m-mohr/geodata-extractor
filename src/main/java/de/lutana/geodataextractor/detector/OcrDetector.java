package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.LocationCollection;
import java.io.File;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;

public class OcrDetector implements GraphicDetector {
	
	private final CoordinateDetector cd = new CoordinateDetector();

	@Override
	public void detect(File graphicFile, LocationCollection locations) {
        ITesseract instance = new Tesseract();  // JNA Interface Mapping
        // ITesseract instance = new Tesseract1(); // JNA Direct Mapping
        File tessDataFolder = LoadLibs.extractTessResources("tessdata"); // Maven build bundles English data
        instance.setDatapath(tessDataFolder.getParent());
        try {
            String result = instance.doOCR(graphicFile);
			cd.detect(result, locations);
			// TODO: More to come...
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
	}
	
}
