package de.lutana.geodataextractor.recognizor;

import java.net.URISyntaxException;
import java.net.URL;

public class TensorFlowWorldMapRecognizer extends TensorFlowDualRecognizer {

	private static final String MAP_CLASS = "world";
	private static final String LABEL_FILE = "worldmap_labels.txt";
	private static final String GRAPH_FILE = "worldmap_graph.pb";
	private static TensorFlowWorldMapRecognizer instance;

	public static TensorFlowWorldMapRecognizer getInstance() throws URISyntaxException {
		if (instance == null) {
			URL graphFile = TensorFlowWorldMapRecognizer.class.getClassLoader().getResource("tensorflow/" + GRAPH_FILE);
			URL labelFile = TensorFlowWorldMapRecognizer.class.getClassLoader().getResource("tensorflow/" + LABEL_FILE);
			instance = new TensorFlowWorldMapRecognizer(graphFile, labelFile, TensorFlowDualRecognizer.END_NODE, MAP_CLASS);
		}
		return instance;
	}
	
	protected TensorFlowWorldMapRecognizer(URL graphFile, URL labelFile, String endNode, String className) throws URISyntaxException {
		super(graphFile, labelFile, endNode, className);
	}
}
