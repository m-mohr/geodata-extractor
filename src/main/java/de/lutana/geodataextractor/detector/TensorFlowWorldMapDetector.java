package de.lutana.geodataextractor.detector;

import java.net.URISyntaxException;
import java.net.URL;

public class TensorFlowWorldMapDetector extends TensorFlowDualDetector {

	private static final String MAP_CLASS = "world";
	private static final String LABEL_FILE = "worldmap_labels.txt";
	private static final String GRAPH_FILE = "worldmap_graph.pb";
	private static TensorFlowWorldMapDetector instance;

	public static TensorFlowWorldMapDetector getInstance() throws URISyntaxException {
		if (instance == null) {
			URL graphFile = TensorFlowWorldMapDetector.class.getClassLoader().getResource("tensorflow/" + GRAPH_FILE);
			URL labelFile = TensorFlowWorldMapDetector.class.getClassLoader().getResource("tensorflow/" + LABEL_FILE);
			instance = new TensorFlowWorldMapDetector(graphFile, labelFile, TensorFlowDualDetector.END_NODE, MAP_CLASS);
		}
		return instance;
	}
	
	protected TensorFlowWorldMapDetector(URL graphFile, URL labelFile, String endNode, String className) throws URISyntaxException {
		super(graphFile, labelFile, endNode, className);
	}
}
