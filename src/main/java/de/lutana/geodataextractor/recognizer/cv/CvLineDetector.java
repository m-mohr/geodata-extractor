package de.lutana.geodataextractor.recognizer.cv;

import com.vividsolutions.jts.geom.LineSegment;
import java.util.List;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public abstract class CvLineDetector {
		
	protected final CvGraphic img;
	
	public CvLineDetector(CvGraphic img) {
		this.img = img;
	}
	
	public abstract List<LineSegment> detect() throws CvException;
	
	public Mat paint(Mat mat, List<LineSegment> lines, Scalar color) {
		Mat drawing = Mat.zeros(mat.size(), CvType.CV_8UC3);
		for (LineSegment line : lines) {
			Point tl = new Point(line.p0.x, line.p0.y);
			Point br = new Point(line.p1.x, line.p1.y);
			Imgproc.line(drawing, tl, br, (color == null ? OpenCV.getRandomColorScalar() : color), 1);
		}
		return drawing;
	}

	public Mat paint(Mat mat, List<MatOfPoint> contours, Mat hierarchy, Scalar color) {
		Mat drawing = Mat.zeros(mat.size(), CvType.CV_8UC3);
		for (int i = 0; i < contours.size(); i++) {
			Imgproc.drawContours(drawing, contours, i, (color == null ? OpenCV.getRandomColorScalar() : color), 1, 8, hierarchy, 0, new Point());
		}
		return drawing;
	}

}
