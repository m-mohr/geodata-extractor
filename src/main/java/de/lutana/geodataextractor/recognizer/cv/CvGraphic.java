package de.lutana.geodataextractor.recognizer.cv;

import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Graphic;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.image.text.extraction.swt.LineCandidate;

public class CvGraphic extends Graphic {
	
	private BufferedImage bImage = null;
	private FImage fImage = null;
	private Mat mat = null;
	private Figure figure = null;

	private List<LineCandidate> textLines = null;
	private Integer backgroundBrightness = null;
	
	public CvGraphic(File file) {
		super(file);
	}
	
	public CvGraphic(Figure figure) {
		super(figure.getGraphicFile());
		this.figure = figure;
	}
	
	/**
	 * Releases images stored in memory.
	 */
	public void dispose() {
		this.bImage = null;
		this.textLines = null;
		this.backgroundBrightness = null;
	}
	
	public Figure getFigure() {
		return this.figure;
	}
	
	public void setFigure(Figure figure) {
		this.figure = figure;
	}
	
	public BufferedImage getBufferedImage() {
		try {
			this.bImage = ImageIO.read(this.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this.bImage;
	}
	
	public FImage getFImage() {
		if (this.fImage == null) {
			this.fImage = ImageUtilities.createFImage(this.getBufferedImage());
		}
		return this.fImage;
	}
	
	public Mat getMat() {
		if (this.mat == null) {
			this.mat = OpenCV.getInstance().toMat(this.getBufferedImage());
		}
		return this.mat;
	}
	
	public int getWidth() {
		return this.getBufferedImage().getWidth();
	}
	
	public int getHeight() {
		return this.getBufferedImage().getHeight();
	}
	
	public List<LineCandidate> getTextLines() {
		if (this.textLines == null) {
			StrokeWidthTransformTextDetector swt = new StrokeWidthTransformTextDetector(this);
			this.textLines = swt.detectLines();
		}
		return this.textLines;
	}
	
	public List<Rect> getTextBoxes() {
		return StrokeWidthTransformTextDetector.toRectList(getTextLines());
	}
	
	/**
	 * Returns 1 for white background, 0 for black background and -1 for unknown background.
	 * @return 
	 */
	public int getBackgroundBrightness() {
		if (this.backgroundBrightness == null) {
			HistogramModel model = new HistogramModel(5);
			model.estimateModel(this.getFImage());
			double[] hist = model.histogram.values;
			if (hist[4] > 0.5) {
				this.backgroundBrightness = 1; // probably white background
		}
			else if (hist[0] > 0.5) {
				this.backgroundBrightness = 0; // probably black background
			}
			else {
				this.backgroundBrightness = -1; // Can't decide
			}
		}
		return this.backgroundBrightness;
	}
	
}
