package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Graphic;
import de.lutana.geodataextractor.recognizer.cv.CvGraphic;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

/**
 *
 * @author emara-geek
 * @author Matthias Mohr
 * @see https://github.com/emara-geek/object-recognition-tensorflow
 */
public abstract class TensorFlowDualDetector implements GraphicDetector {
	public static final String END_NODE = "final_result";

	protected InputStream graphFile;
	protected InputStream labelFile;
	protected String endNode;
	protected String className;

	protected List<String> labels;
	protected byte[] graph;
	
	
	protected TensorFlowDualDetector(InputStream graphFile, InputStream labelFile, String endNode, String className) {
		this.graphFile = graphFile;
		this.labelFile = labelFile;
		this.endNode = endNode;
		this.className = className;
		this.labels = null;
		this.graph = null;
	}

	public void preload() throws IOException {
		if (labels == null || graph == null) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.labelFile))) {
				labels = new ArrayList<>();
				for (;;) {
					String line = reader.readLine();
					if (line == null) {
						break;
					}
					labels.add(line);
				}
			}

			graph = readFully(graphFile, -1, true);
		}
	}
	
	// Taken from: sun.misc.IOUtils
	public static byte[] readFully(InputStream is, int length, boolean readAll) throws IOException {
		byte[] output = {};
		if (length == -1) length = Integer.MAX_VALUE;
		int pos = 0;
		while (pos < length) {
			int bytesToRead;
			if (pos >= output.length) { // Only expand when there's no room
				bytesToRead = Math.min(length - pos, output.length + 1024);
				if (output.length < pos + bytesToRead) {
					output = Arrays.copyOf(output, pos + bytesToRead);
				}
			} else {
				bytesToRead = output.length - pos;
			}
			int cc = is.read(output, pos, bytesToRead);
			if (cc < 0) {
				if (readAll && length != Integer.MAX_VALUE) {
					throw new IOException("Detect premature EOF");
				} else {
					if (output.length != pos) {
						output = Arrays.copyOf(output, pos);
					}
					break;
				}
			}
			pos += cc;
		}
		return output;
	}

	@Override
	public float detect(Figure f) {
		try {
			return this.detect(f.getGraphic());
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public float detect(Figure f, CvGraphic g) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			// JPEG is faster than PNG, but destroyes transparency
			ImageIO.write(g.getBufferedImage(), "jpg", baos);
			return this.detect(baos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public float detect(Graphic g) throws IOException {
		byte[] imgData = Files.readAllBytes(g.getFile().toPath());
		return this.detect(imgData);
	}
	
	public float detect(byte[] imgData) throws IOException {
		this.preload();
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
