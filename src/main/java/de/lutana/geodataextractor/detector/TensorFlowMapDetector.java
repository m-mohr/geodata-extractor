package de.lutana.geodataextractor.detector;

import java.net.URISyntaxException;
import java.net.URL;

public class TensorFlowMapDetector extends TensorFlowDualDetector {

	private static final String MAP_CLASS = "map";
	private static final String LABEL_FILE = "map_labels.txt";
	private static final String GRAPH_FILE = "map_graph.pb";
	private static TensorFlowMapDetector instance;

	public static TensorFlowMapDetector getInstance() throws URISyntaxException {
		if (instance == null) {
			URL graphFile = TensorFlowMapDetector.class.getClassLoader().getResource("tensorflow/" + GRAPH_FILE);
			URL labelFile = TensorFlowMapDetector.class.getClassLoader().getResource("tensorflow/" + LABEL_FILE);
			instance = new TensorFlowMapDetector(graphFile, labelFile, TensorFlowDualDetector.END_NODE, MAP_CLASS);
		}
		return instance;
	}
	
	protected TensorFlowMapDetector(URL graphFile, URL labelFile, String endNode, String className) throws URISyntaxException {
		super(graphFile, labelFile, endNode, className);
	}
}
