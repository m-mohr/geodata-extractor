package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.recognizer.cv.CvGraphic;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.recognizer.nlp.LocationExtractor;
import de.lutana.geodataextractor.recognizer.nlp.LocationOccurrence;
import de.lutana.geodataextractor.recognizer.nlp.StanfordExtractor;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;

public class WorldMapDetector implements GraphicDetector {

	private static final Pattern WORLDMAP_PATTERN = Pattern.compile("\\b(global|international|world-?maps?|world|earth|worldwide|countries|continents)\\b", Pattern.CASE_INSENSITIVE);
	private static final Pattern OTHER_PATTERN = Pattern.compile("\\b(roads?|buildings?|lakes?|rivers?|states?|city|cities|villages?|districts?|departments?|neighborhoods?|boroughs?|towns?|provinces?|townships?|sites?|area[^s]|country|(?:floor|locality)\\s+plans?|aerial\\s+photographs?|(?:gap)?charts?|illustration|(dia|seismo|histo)grams?|tables?|(?<!aerial\\s{1,4}|ortho)(photos?|photographs?)|graphs?|curves?|pictures?|photos?|plot(s|ted)?|drafts?|gauges?|streamgraphs?|gapcharts?|gantt|(?:regression|dotted(?:\\s+\\w+)|dashed(?:\\s+\\w+)|solid(?:\\s+\\w+))\\s+lines?|velocity\\s+models?|(?:squares|linear|simple|polynomial)\\s+regressions?|seismic(?:[-\\s]+reflection)\\s+profiles?|(?<!in\\s{1,4})view\\s+of|over)\\b", Pattern.CASE_INSENSITIVE);
	
	
	private float defaultProbability;
	private float patternOffset = 0.35f;
	private float locationOffset = 0.25f;
	private float maxLocationOffset = 0.5f;
	private TensorFlowWorldMapDetector tfWorldMapDetector;
	private LocationExtractor locationExtractor;
	
	public WorldMapDetector() {
		this(true);
	}
	
	public WorldMapDetector(boolean rejectByDefault) {
		this.defaultProbability = rejectByDefault ? 0 : 1;
		try {
			tfWorldMapDetector = TensorFlowWorldMapDetector.getInstance();
		} catch (URISyntaxException ex) {
			LoggerFactory.getLogger(getClass()).error("Loading TensorFlowWorldMapDetector failed: " + ex.getMessage() + ". Continuing without world map detection.");
		}
		try {
			this.locationExtractor = new StanfordExtractor();
		} catch (ClassCastException | IOException | ClassNotFoundException ex) {
			LoggerFactory.getLogger(getClass()).error("Loading StanfordExtractor failed: " + ex.getMessage() + ". Continuing without caption improvements for world map detection.");
		}
	}
	
	public void preload() throws URISyntaxException, IOException {
		tfWorldMapDetector.preload();
	}
	
	@Override
	public float detect(Figure f, CvGraphic cachedGraphic) {
		Float result = null;

		// Detect whether it's a map or not using Tensorflow
		if (this.tfWorldMapDetector != null) {
			result = tfWorldMapDetector.detect(f);
		}

		// Check whether they are speaking about world map(s) in the caption
		Matcher m = WORLDMAP_PATTERN.matcher(f.getCaption());
		if (m.find()) {
			result = Math.min(1, result + this.patternOffset);
		}

		// Check whether they are speaking about some non-world-map stuff in the caption
		Matcher n = OTHER_PATTERN.matcher(f.getCaption());
		if (n.find()) {
			result = Math.max(0, result - this.patternOffset);
		}
		
		// Check for location names
		List<LocationOccurrence> locations = locationExtractor.extractLocationNames(f.getCaption());
		if (!locations.isEmpty()) {
			// For each location found add an offset of locationOffset, but limit it at maxLocationOffset.
			float offset = Math.min(this.maxLocationOffset, locations.size() * this.locationOffset);
			// Remove the offset calculated
			result = Math.max(0, result - offset);
		}

		return result == null ? this.defaultProbability : result;
	}

	@Override
	public float detect(Figure f) {
		return this.detect(f, new CvGraphic(f));
	}
	
}
