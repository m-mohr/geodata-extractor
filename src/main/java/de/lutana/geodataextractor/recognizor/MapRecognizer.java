package de.lutana.geodataextractor.recognizor;

import de.lutana.geodataextractor.detector.cv.CvGraphic;
import de.lutana.geodataextractor.detector.cv.CvTableDetector;
import de.lutana.geodataextractor.detector.cv.MorphologicalTableDetector;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.util.GeoTools;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opencv.core.Rect;
import org.slf4j.LoggerFactory;

public class MapRecognizer implements Recognizor {

	private static final Pattern MAP_PATTERN = Pattern.compile("\bmaps?\b");
	
	private float defaultProbability;
	private TensorFlowMapRecognizer tfMapRecognizer;
	
	public MapRecognizer(boolean rejectByDefault) {
		this.defaultProbability = rejectByDefault ? 0 : 1;
		try {
			tfMapRecognizer = TensorFlowMapRecognizer.getInstance();
		} catch (URISyntaxException ex) {
			LoggerFactory.getLogger(getClass()).error("Loading TensorFlowMapRecognizer failed: " + ex.getMessage() + ". Continuing without map detection.");
		}
	}
	
	public float recognize(Figure f, CvGraphic cachedGraphic) {
		Float result = null;
//		if (this.containsTable(cachedGraphic)) {
//			return 0.75f;
//		}

		// Detect whether it's a map or not using Tensorflow
		if (this.tfMapRecognizer != null) {
			result = tfMapRecognizer.recognize(f);
		}

		// Check whether they are speaking about map(s) in the caption
		Matcher m = MAP_PATTERN.matcher(f.getCaption());
		if (m.matches() && result < 0.5) {
			result = 0.5f;
		}

		return result == null ? this.defaultProbability : result;
	}

	@Override
	public float recognize(Figure f) {
		return this.recognize(f, new CvGraphic(f.getGraphic()));
	}
	
	protected boolean containsTable(CvGraphic graphic) {
		// Detect whether the figure contains a table
		CvTableDetector tableDetector = new MorphologicalTableDetector(graphic);
		List<Rect> tableBounds = tableDetector.detect();
		Location imageLocation = new Location(0, graphic.getWidth(), 0, graphic.getHeight());
		for(Rect rect : tableBounds) {
			// make sure it's not just a legend or a really small table.
			Location tableLocation = new Location(rect.x, rect.x + rect.width, rect.y, rect.y + rect.y + rect.height); // Well, somehow they are locations ;)
			if (GeoTools.calcJaccardIndex(tableLocation, imageLocation) > 0.5) {
				return true;
			}
		}
		return false;
	}
	
}
