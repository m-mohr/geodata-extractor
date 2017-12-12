package de.lutana.geodataextractor.detector;

import java.io.InputStream;
import java.net.URISyntaxException;

public class TensorFlowWorldMapDetector extends TensorFlowDualDetector {

	private static final String MAP_CLASS = "world";
	private static final String LABEL_FILE = "worldmap_labels.txt";
	private static final String GRAPH_FILE = "worldmap_graph.pb";
	private static TensorFlowWorldMapDetector instance;

	public static TensorFlowWorldMapDetector getInstance() throws URISyntaxException {
		if (instance == null) {
			InputStream graphFile = TensorFlowWorldMapDetector.class.getClassLoader().getResourceAsStream("tensorflow/" + GRAPH_FILE);
			InputStream labelFile = TensorFlowWorldMapDetector.class.getClassLoader().getResourceAsStream("tensorflow/" + LABEL_FILE);
			instance = new TensorFlowWorldMapDetector(graphFile, labelFile, TensorFlowDualDetector.END_NODE, MAP_CLASS);
		}
		return instance;
	}
	
	protected TensorFlowWorldMapDetector(InputStream graphFile, InputStream labelFile, String endNode, String className) throws URISyntaxException {
		super(graphFile, labelFile, endNode, className);
	}
}
