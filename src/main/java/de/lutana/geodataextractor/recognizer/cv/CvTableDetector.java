package de.lutana.geodataextractor.recognizer.cv;

import java.util.List;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public abstract class CvTableDetector {
		
	protected final CvGraphic img;
	
	public CvTableDetector(CvGraphic img) {
		this.img = img;
	}
	
	public abstract List<Rect> detect() throws CvException;
	
	public Mat paint(Mat mat, List<Rect> tables, Scalar color) {
		Mat drawing = Mat.zeros(mat.size(), CvType.CV_8UC3);
		for (Rect table : tables) {
			Imgproc.rectangle(drawing, table.tl(), table.br(), (color == null ? OpenCV.getRandomColorScalar() : color), 3);
		}
		return drawing;
	}

}
