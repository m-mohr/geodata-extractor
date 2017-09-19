package de.lutana.geodataextractor.detector.cv;

import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * ROBUST TEXT DETECTION IN NATURAL IMAGES WITH EDGE-ENHANCED MAXIMALLY STABLE EXTREMAL REGIONS.
 * 
 * @see https://stackoverflow.com/questions/11116199/stroke-width-transform-swt-implementation-python/11142278
 */
public class EdgeEnhancedTextDetector extends CvTextDetector {
	
	public EdgeEnhancedTextDetector(Mat img) {
		super(img);
	}

	/**
	 * Tries to detect text in images.
	 * 
	 * @return
	*/
	public List<Rect> detect() {
		// bw8u : we want to calculate the SWT of this.
		// NOTE: Its background pixels are 0 and forground pixels are 1 (not 255!)
		Mat bw = OpenCV.getInstance().toMonotoneCustom(img, true);
		Imgproc.threshold(bw, bw, 128, 1, Imgproc.THRESH_BINARY);
		OpenCV.getInstance().showImage(bw);

		Mat bw32f = new Mat();
		bw.convertTo(bw32f, CvType.CV_32F); // format conversion for multiplication
		
		Mat swt32f = new Mat();
		Imgproc.distanceTransform(bw, swt32f, Imgproc.CV_DIST_L2, 5); // distance transform
		MinMaxLocResult minmax = Core.minMaxLoc(swt32f);  // find max
		int strokeRadius = (int)Math.ceil(minmax.maxVal);  // half the max stroke width
		
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)); // 3x3 kernel used to select 8-connected neighbors
		for (int j = 0; j < strokeRadius; j++) {
			Imgproc.dilate(swt32f, swt32f, kernel); // assign the max in 3x3 neighborhood to each center pixel
			swt32f = swt32f.mul(bw32f); // apply mask to restore original shape and to avoid unnecessary max propogation
		}
		OpenCV.getInstance().showImage(swt32f);
		return null;
	}
	
}
