package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.recognizer.cv.CvGraphic;
import de.lutana.geodataextractor.recognizer.cv.CvTableDetector;
import de.lutana.geodataextractor.recognizer.cv.MorphologicalTableDetector;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.util.GeoTools;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opencv.core.Rect;
import org.slf4j.LoggerFactory;

public class MapDetector implements GraphicDetector {

	// ToDo: Compare with and without "plot(ted)"
	// ToDo: check rvi|ndvi|vegetation\\s+index|
	private static final Pattern MAP_PATTERN = Pattern.compile("\\b(maps?|mapviews?|ortho(?:photos?|photographs?|images?)|aerial\\s+photographs?|(?:floor|locality)\\s+plans?|cartograms?|(?:sattelite|landsat\\s+(?:tm|mss)|geoeye|worldview[\\s-]+\\d|spot\\s+\\d|aster|blackbridge|rapideye|eros\\s+[ab]|meteosat|base)\\s+image(?:ry|s)?|(?:true|false|natural)\\s+colou?r\\s+composite|rvi|ndvi|modis|(covering|showing)\\s+the\\s+(?:cit(?:y|ies)|countr?(?:y|ies)|villages?|town|continents?|earth|districts?|states?)|spatial\\s+(variability|distribution|extent|variation)|(albers|mercator|equal\\s+area|gall-peters|peters|equirectangular|cylindrical|eckert\\s+[iv]+|conic|polyconic|orthographic|stereographic|equidistant|conformal)\\s+projection)\\b", Pattern.CASE_INSENSITIVE);
	private static final Pattern NOMAP_PATTERN = Pattern.compile("\\b((?:gap)?charts?|illustration|(dia|seismo|histo)grams?|tables?|(?<!aerial\\s{1,4}|ortho)(photos?|photographs?)|graphs?|curves?|pictures?|photos?|plot(s|ted)?|drafts?|gauges?|streamgraphs?|gapcharts?|gantt|(?:regression|dotted(?:\\s+\\w+)|dashed(?:\\s+\\w+)|solid(?:\\s+\\w+))\\s+lines?|velocity\\s+models?|(?:squares|linear|simple|polynomial)\\s+regressions?|seismic(?:[-\\s]+reflection)\\s+profiles?|(?<!in\\s{1,4})view\\s+of|over)\\b", Pattern.CASE_INSENSITIVE);
	
	private float defaultProbability;
	private TensorFlowMapDetector tfMapDetector;
	private float patternOffset = 0.5f;
	
	public MapDetector(boolean rejectByDefault) {
		this.defaultProbability = rejectByDefault ? 0 : 1;
		try {
			tfMapDetector = TensorFlowMapDetector.getInstance();
		} catch (URISyntaxException ex) {
			LoggerFactory.getLogger(getClass()).error("Loading TensorFlowMapDetector failed: " + ex.getMessage() + ". Continuing without map detection.");
		}
	}
	
	public void preload() throws URISyntaxException, IOException {
		tfMapDetector.preload();
	}
	
	@Override
	public float detect(Figure f, CvGraphic cachedGraphic) {
		Float result = null;

		// Detect whether it's a map or not using Tensorflow
		if (this.tfMapDetector != null) {
			result = tfMapDetector.detect(f);
		}

		// Check whether they are speaking about map(s) in the caption
		Matcher m = MAP_PATTERN.matcher(f.getCaption());
		if (m.find()) {
			result = Math.min(1, result + this.patternOffset);
		}

		// Check whether they are speaking about some non-map stuff in the caption
		Matcher n = NOMAP_PATTERN.matcher(f.getCaption());
		if (n.find()) {
			result = Math.max(0, result - this.patternOffset);
		}

//		if (this.containsTable(cachedGraphic)) {
//			result = Math.max(0, result - 0.5f);
//		}

		return result == null ? this.defaultProbability : result;
	}

	@Override
	public float detect(Figure f) {
		return this.detect(f, new CvGraphic(f));
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
