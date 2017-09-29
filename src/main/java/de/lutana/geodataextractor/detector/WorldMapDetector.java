package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.detector.cv.CvGraphic;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.recognizor.TensorFlowWorldMapRecognizer;
import java.io.IOException;
import java.net.URISyntaxException;

public class WorldMapDetector implements GraphicDetector {

	@Override
	public void detect(CvGraphic graphic, LocationCollection locations) {
		try {
			float result = TensorFlowWorldMapRecognizer.getInstance().recognize(graphic);
			if (result >= 0.5) {
				Location l = new Location(-180, 180, -90, 90);
				l.setProbability(result);
				locations.add(l);
			}
		} catch (URISyntaxException | IOException ex) {
			ex.printStackTrace();
		}
	}
	
}
