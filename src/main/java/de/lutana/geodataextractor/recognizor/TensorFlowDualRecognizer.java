package de.lutana.geodataextractor.recognizor;

import de.lutana.geodataextractor.entity.Figure;
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
public abstract class TensorFlowDualRecognizer implements Recognizor {
	public static final String END_NODE = "final_result";

	protected Path graphFile;
	protected Path labelFile;
	protected String endNode;
	protected String className;

	protected List<String> labels;
	protected byte[] graph;
	
	
	protected TensorFlowDualRecognizer(Path graphFile, Path labelFile, String endNode, String className) {
		this.graphFile = graphFile;
		this.labelFile = labelFile;
		this.endNode = endNode;
		this.className = className;
		this.labels = null;
		this.graph = null;
	}
	
	protected TensorFlowDualRecognizer(File graphFile, File labelFile, String endNode, String className) {
		this(graphFile.toPath(), labelFile.toPath(), endNode, className);
	}
	
	protected TensorFlowDualRecognizer(URL graphFile, URL labelFile, String endNode, String className) throws URISyntaxException {
		this(new File(graphFile.toURI()), new File(labelFile.toURI()), endNode, className);
	}

	protected void lazyLoad() throws IOException {
		if (labels == null || graph == null) {
			labels = Files.readAllLines(labelFile, Charset.forName("UTF-8"));
			graph = Files.readAllBytes(graphFile);
		}
	}

	@Override
	public float recognize(Figure f) {
		try {
			return this.recognize(f.getGraphic());
		} catch (IOException ex) {
			ex.printStackTrace();
			return -1;
		}
	}
	
	public float recognize(Graphic g) throws IOException {
		byte[] imgData = Files.readAllBytes(g.getFile().toPath());
		return this.recognize(imgData);
	}
	
	public float recognize(byte[] imgData) throws IOException {
		this.lazyLoad();
		try (Tensor image = Tensor.create(imgData)) {
			float[] labelProbabilities = this.executeInceptionGraph(graph, image);
			int indexMap = labels.indexOf(this.className);
			if (indexMap != -1) {
				return labelProbabilities[indexMap];
			}
		}
		return 0;
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

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

}
