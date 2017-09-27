package de.lutana.geodataextractor.util;

import de.lutana.geodataextractor.entity.Graphic;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

/**
 *
 * @author emara-geek
 * @author Matthias Mohr
 * @see https://github.com/emara-geek/object-recognition-tensorflow
 */
public class TensorFlowMapRecognizer {

	private static final String MAP_CLASS = "map";
	private static final String LABEL_FILE = "map_labels.txt";
	private static final String GRAPH_FILE = "map_graph.pb";
	private static final String END_NODE = "final_result";
	private static TensorFlowMapRecognizer instance;

	public static TensorFlowMapRecognizer getInstance() throws URISyntaxException {
		if (instance == null) {
			URL graphFile = TensorFlowMapRecognizer.class.getClassLoader().getResource("tensorflow/" + GRAPH_FILE);
			URL labelFile = TensorFlowMapRecognizer.class.getClassLoader().getResource("tensorflow/" + LABEL_FILE);
			instance = new TensorFlowMapRecognizer(graphFile, labelFile, END_NODE);
		}
		return instance;
	}

	protected Path graphFile;
	protected Path labelFile;
	protected String endNode;

	protected List<String> labels;
	protected byte[] graph;
	
	
	protected TensorFlowMapRecognizer(Path graphFile, Path labelFile, String endNode) {
		this.graphFile = graphFile;
		this.labelFile = labelFile;
		this.endNode = endNode;
		this.labels = null;
		this.graph = null;
	}
	
	protected TensorFlowMapRecognizer(File graphFile, File labelFile, String endNode) {
		this(graphFile.toPath(), labelFile.toPath(), endNode);
	}
	
	protected TensorFlowMapRecognizer(URL graphFile, URL labelFile, String endNode) throws URISyntaxException {
		this(new File(graphFile.toURI()), new File(labelFile.toURI()), endNode);
	}

	protected void lazyLoad() throws IOException {
		if (labels == null || graph == null) {
			labels = Files.readAllLines(labelFile, Charset.forName("UTF-8"));
			graph = Files.readAllBytes(graphFile);
		}
	}
	
	public Match recognize(Graphic g) throws IOException {
		byte[] imgData = Files.readAllBytes(g.getFile().toPath());
		return this.recognize(imgData);
	}
	
	public Match recognize(byte[] imgData) throws IOException {
		this.lazyLoad();
		Match m = null;
		try (Tensor image = Tensor.create(imgData)) {
			float[] labelProbabilities = this.executeInceptionGraph(graph, image);
			int bestLabelIdx = this.getMaxIndex(labelProbabilities);
			m = new Match(labels.get(bestLabelIdx), labelProbabilities[bestLabelIdx]);
		}
		return m;
	}

	private float[] executeInceptionGraph(byte[] graphDef, Tensor image) {
		try (Graph g = new Graph()) {
			g.importGraphDef(graphDef);
			try (Session s = new Session(g); Tensor result = s.runner().feed("DecodeJpeg/contents", image).fetch(this.endNode).run().get(0)) {
				final long[] rshape = result.shape();
				if (result.numDimensions() != 2 || rshape[0] != 1) {
					throw new RuntimeException(String.format("Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s", Arrays.toString(rshape)));
				}
				int nlabels = (int) rshape[1];
				return result.copyTo(new float[1][nlabels])[0];
			}
		}
	}

	private int getMaxIndex(float[] probabilities) {
		int best = 0;
		for (int i = 1; i < probabilities.length; ++i) {
			if (probabilities[i] > probabilities[best]) {
				best = i;
			}
		}
		return best;
	}

	/**
	 * @return the graphFile
	 */
	public Path getGraphFile() {
		return graphFile;
	}

	/**
	 * @return the labelFile
	 */
	public Path getLabelFile() {
		return labelFile;
	}

	/**
	 * @return the endNode
	 */
	public String getEndNode() {
		return endNode;
	}

	/**
	 * @return the labels
	 */
	public List<String> getLabels() {
		return labels;
	}

	/**
	 * @return the graph
	 */
	public byte[] getGraph() {
		return graph;
	}

	public static class Match {

		protected String className;
		protected float probability;

		public Match(String className, float probability) {
			this.className = className;
			this.probability = probability;
		}

		/**
		 * @return the className
		 */
		public String getClassName() {
			return className;
		}
		
		public boolean isMap(float minProbability) {
			return (className.equals(MAP_CLASS) && probability >= minProbability);
		}

		/**
		 * @return the probability
		 */
		public float getProbability() {
			return probability;
		}

	}

}
