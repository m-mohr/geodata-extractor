package de.lutana.geodataextractor.recognizer;

import de.lutana.geodataextractor.recognizer.cv.CvGraphic;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.detector.TensorFlowWorldMapDetector;
import java.io.IOException;
import java.net.URISyntaxException;
import org.slf4j.LoggerFactory;

public class WorldMapRecognizer implements GraphicRecognizer {

	@Override
	public boolean recognize(CvGraphic graphic, LocationCollection locations, double weight) {
		try {
			float result = TensorFlowWorldMapDetector.getInstance().detect(graphic);
			if (result >= 0.5) {
				Location l = new Location(-180, 180, -90, 90);
				// probability can be anything between 0.5 and 1.
				// Values near to 50 are usually not very likely to be good.
				// Therefore we are stretching the probability to have a range from 0 to 1 again.
				l.setProbability((result - 0.5) * 2);
				l.setWeight(weight);
				locations.add(l);
				LoggerFactory.getLogger(getClass()).debug("Parsed location " + l + " from WorldMapRecognizer.");
				return true;
			}
		} catch (URISyntaxException | IOException ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
}
