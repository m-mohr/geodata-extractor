package de.lutana.geodataextractor.detector.cv;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import de.lutana.geodataextractor.util.GeoTools;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

/**
 * @author Matthias Mohr
 */
public class MapAxesLineDetector extends CvLineDetector {
	
	private int dim;
	
	public MapAxesLineDetector(CvGraphic img) {
		super(img);
		this.dim = Math.min(img.getWidth(), img.getHeight()) / 100;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public List<LineSegment> detect() {
		Mat dest = this.img.getMat().clone();
		
		OpenCV cv = OpenCV.getInstance();

		// Remove text to avoid being detected as (part of the) line
		this.removeText(dest);

		// Use colour clustering to get b/w image that groups big chunks together
		dest = this.cluster(dest, 3).get(0); // ToDo: Speed up?

		// Invert image for morph. ops.
		cv.invertMonotone(dest);

		// Morph. ops. to remove noise and gaps in image
		dest = cv.closeGaps(dest, dim);
		
		// Get edges using Canny
		dest = cv.cannyAdaptive(dest);

		// Merge nearby edges
		Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
		Imgproc.dilate(dest, dest, structure, new Point(-1, -1), 1);

		// Detect lines using HoughP Transform
		List<LineSegment> lines = HoughProbabilisticLineDetector.detect(dest, 1, Math.PI / 180, dim * 20, dim * 30, dim * 2.5);
		if (lines.size() > 200) {
			return new ArrayList<>(); // Too many results. Something went wrong and good results are unlikely.
		}

		// Merge similar lines detected by HoughP Transform
		HoughProbabilisticLineDetector.mergeSimilarLines(lines, Math.PI / 36, dim); // PI / 36 = five degrees

		return lines;
	}
	
	public void removeText(Mat source) {
		// create mask
		List<Rect> boxes = img.getTextBoxes();
		Mat mask = Mat.zeros(source.size(), CvType.CV_8UC1);
		for(Rect box : boxes) {
			Imgproc.rectangle(mask, box.tl(), box.br(), new Scalar(255,255,255), Core.FILLED);
		}
		// fill holes content aware
		Photo.inpaint(source, mask, source, 3, Photo.INPAINT_TELEA);
	}
	
	public List<Mat> cluster(Mat cutout, int k) {
		Mat samples = cutout.reshape(1, cutout.cols() * cutout.rows());
		Mat samples32f = new Mat();
		samples.convertTo(samples32f, CvType.CV_32F, 1.0 / 255.0);
		
		Mat labels = new Mat();
		TermCriteria criteria = new TermCriteria(TermCriteria.COUNT, 100, 1);
		Mat centers = new Mat();
		Core.kmeans(samples32f, k, labels, criteria, 1, Core.KMEANS_PP_CENTERS, centers);

		centers.convertTo(centers, CvType.CV_8UC1, 255.0);
		centers.reshape(3);
		
		List<Mat> clusters = new ArrayList<>();
		for(int i = 0; i < centers.rows(); i++) {
			clusters.add(Mat.zeros(cutout.size(), cutout.type()));
		}
		
		Map<Integer, Integer> counts = new HashMap<>();
		for(int i = 0; i < centers.rows(); i++) counts.put(i, 0);
		
		int rows = 0;
		for(int y = 0; y < cutout.rows(); y++) {
			for(int x = 0; x < cutout.cols(); x++) {
				int label = (int)labels.get(rows, 0)[0];
				int r = (int)centers.get(label, 2)[0];
				int g = (int)centers.get(label, 1)[0];
				int b = (int)centers.get(label, 0)[0];
				counts.put(label, counts.get(label) + 1);
				clusters.get(label).put(y, x, b, g, r);
				rows++;
			}
		}
		return clusters;
	}

}
