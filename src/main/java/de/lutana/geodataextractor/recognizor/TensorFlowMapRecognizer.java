package de.lutana.geodataextractor.recognizor;

import java.net.URISyntaxException;
import java.net.URL;

public class TensorFlowMapRecognizer extends TensorFlowDualRecognizer {

	private static final String MAP_CLASS = "map";
	private static final String LABEL_FILE = "map_labels.txt";
	private static final String GRAPH_FILE = "map_graph.pb";
	private static TensorFlowMapRecognizer instance;

	public static TensorFlowMapRecognizer getInstance() throws URISyntaxException {
		if (instance == null) {
			URL graphFile = TensorFlowMapRecognizer.class.getClassLoader().getResource("tensorflow/" + GRAPH_FILE);
			URL labelFile = TensorFlowMapRecognizer.class.getClassLoader().getResource("tensorflow/" + LABEL_FILE);
			instance = new TensorFlowMapRecognizer(graphFile, labelFile, TensorFlowDualRecognizer.END_NODE, MAP_CLASS);
		}
		return instance;
	}
	
	protected TensorFlowMapRecognizer(URL graphFile, URL labelFile, String endNode, String className) throws URISyntaxException {
		super(graphFile, labelFile, endNode, className);
	}
}
