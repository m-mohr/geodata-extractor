
package de.lutana.geodataextractor.recognizer.cv;

import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public abstract class CvTextDetector {

	protected final CvGraphic img;
	
	protected CvTextDetector(CvGraphic img) {
		this.img = img;
	}
	
	public abstract List<Rect> detect() throws CvException;
	
	public Mat paint(Mat source, List<Rect> rects, Scalar color) {
		Mat dest = source.clone();
		for (Rect rect : rects) {
			Imgproc.rectangle(dest, rect.tl(), rect.br(), (color == null ? OpenCV.getRandomColorScalar() : color), 1);
		}
		return dest;
	}
	
}
