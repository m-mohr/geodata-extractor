package de.lutana.geodataextractor.detector.cv;

import com.vividsolutions.jts.geom.LineSegment;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat4;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.LineSegmentDetector;

/**
 * @author Matthias Mohr
 */
public class LSDLineDetector extends CvLineDetector {
	
	protected int minSize;
	
	public LSDLineDetector(CvGraphic img) {
		super(img);
		this.minSize = 10;
	}

	/**
	 * 
	 * @return
	 */
	public List<LineSegment> detect() {
		Mat gray = OpenCV.getInstance().toGrayscale(img.getMat());
		MatOfFloat4 lines = new MatOfFloat4();
		LineSegmentDetector lsd = Imgproc.createLineSegmentDetector();
		lsd.detect(gray, lines);
		List<LineSegment> list = new ArrayList<>();
		for(int i = 0; i < lines.rows(); i++) {
			double[] val = lines.get(i, 0);
			LineSegment l = new LineSegment(val[0], val[1], val[2], val[3]);
			if (l.getLength() >= minSize) {
				list.add(l);
			}
		}

		return list;
	}

	/**
	 * @return the minSize
	 */
	public int getMinSize() {
		return minSize;
	}

	/**
	 * @param minSize the minSize to set
	 */
	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

}
