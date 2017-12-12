package de.lutana.geodataextractor.detector;

import java.io.InputStream;
import java.net.URISyntaxException;

public class TensorFlowMapDetector extends TensorFlowDualDetector {

	private static final String MAP_CLASS = "map";
	private static final String LABEL_FILE = "map_labels.txt";
	private static final String GRAPH_FILE = "map_graph.pb";
	private static TensorFlowMapDetector instance;

	public static TensorFlowMapDetector getInstance() throws URISyntaxException {
		if (instance == null) {
			InputStream graphFile = TensorFlowMapDetector.class.getClassLoader().getResourceAsStream("tensorflow/" + GRAPH_FILE);
			InputStream labelFile = TensorFlowMapDetector.class.getClassLoader().getResourceAsStream("tensorflow/" + LABEL_FILE);
			instance = new TensorFlowMapDetector(graphFile, labelFile, TensorFlowDualDetector.END_NODE, MAP_CLASS);
		}
		return instance;
	}
	
	protected TensorFlowMapDetector(InputStream graphFile, InputStream labelFile, String endNode, String className) throws URISyntaxException {
		super(graphFile, labelFile, endNode, className);
	}
}
