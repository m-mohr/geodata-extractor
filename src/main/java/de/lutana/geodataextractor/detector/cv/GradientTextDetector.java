package de.lutana.geodataextractor.detector.cv;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Gradient based text detection.
 * 
 * @see https://stackoverflow.com/questions/23506105/extracting-text-opencv
 */
public class GradientTextDetector extends CvTextDetector {

	private float threshold;
	private Size sizeConstraint;
	
	public GradientTextDetector(Mat img) {
		super(img);
		this.threshold = 0.45f;
		this.sizeConstraint = new Size(8, 8);
	}
	
	public Size getSizeConstraint() {
		return this.sizeConstraint;
	}
	
	/**
	 * Constraints on region size (default: 8x8)
	 * 
	 * @param width
	 * @param height
	 */
	public void setSizeConstraint(int width, int height) {
		this.sizeConstraint = new Size(width, height);
	}

	public float getThreshold() {
		return threshold;
	}

	/**
	 * Assume at least [threshold] percent of the area is filled if it contains text (default: 0.45 = 45%).
	 * 
	 * @param threshold 
	 */
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	/**
	 * Tries to detect text in images.
	 * 
	 * @return
	*/
	@Override
	public List<Rect> detect() {
		List<Rect> rectangles = new ArrayList<>();
		
		Mat gray = OpenCV.getInstance().toGrayscale(this.img);
		// morphological gradient
		Mat grad = new Mat();
		Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
		Imgproc.morphologyEx(gray, grad, Imgproc.MORPH_GRADIENT, morphKernel);
		// binarize
		Mat bw = new Mat();
		Imgproc.threshold(grad, bw, 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
		// connect horizontally oriented regions
		Mat connected = new Mat();
		morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 1));
		Imgproc.morphologyEx(bw, connected, Imgproc.MORPH_CLOSE, morphKernel);
		// find contours
		Mat mask = Mat.zeros(bw.size(), CvType.CV_8UC1);
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(connected, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
		// filter contours
		for (int idx = 0; idx < contours.size(); idx++) {
			Rect rect = Imgproc.boundingRect(contours.get(idx));
			Mat maskROI = new Mat(mask, rect);
			// fill the contour
			Imgproc.drawContours(mask, contours, idx, new Scalar(255, 255, 255), Core.FILLED);
			// ratio of non-zero pixels in the filled region
			double r = (double) Core.countNonZero(maskROI) / (rect.width * rect.height);

			// these two conditions alone are not very robust. better to use something like the number of significant peaks in a horizontal projection as a third condition
			if (r > threshold && (rect.height > sizeConstraint.height && rect.width > sizeConstraint.width)) {
				rectangles.add(rect);
			}
		}
		return rectangles;
	}
	
}
