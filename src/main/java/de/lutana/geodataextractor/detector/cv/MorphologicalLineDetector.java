package de.lutana.geodataextractor.detector.cv;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Detects (only) horizontal and vertical lines in images.
 * 
 * @see http://answers.opencv.org/question/63847/how-to-extract-tables-from-an-image/
 * @see http://answers.opencv.org/question/56496/implementation-question-how-to-create-bounding-boxes-around-answers-on-worksheets/
 * @author Matthias Mohr
 */
public class MorphologicalLineDetector extends CvLineDetector {
	
	private static final short DIRECTION_HORIZONTAL = 0;
	private static final short DIRECTION_VERTICAL = 1;
	
	private Integer scale;
	
	public MorphologicalLineDetector(CvGraphic img) {
		super(img);
		this.scale = 10;
	}
	
	/**
	 * Play with this variable in order to increase/decrease the amount of lines to be detected.
	 * 
	 * @param scale 
	 */
	public void setScale(int scale) {
		this.scale = scale;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public List<LineSegment> detect() {
		OpenCV cv = OpenCV.getInstance();

		// Convert to black and white image
		Mat bw = cv.toMonotoneAdaptive(this.img.getMat(), true);

		List<LineSegment> lines = new ArrayList<>();
		this.getLines(bw, OpenCV.MorphologicalDirection.HORIZONTAL, lines);
		this.getLines(bw, OpenCV.MorphologicalDirection.VERTICAL, lines);
		
		return lines;
	}
	
	private void getLines(Mat srcImg, OpenCV.MorphologicalDirection direction, List<LineSegment> lines) {
		Mat mask = OpenCV.getInstance().createLineMask(srcImg, direction, this.scale);
		this.extractLines(mask, direction, lines);
	}
	
	private void extractLines(Mat srcImg, OpenCV.MorphologicalDirection direction, List<LineSegment> list) throws IllegalArgumentException {
		// Find external contour
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(srcImg, contours, hierarchy, Imgproc.RETR_CCOMP,Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

		for(MatOfPoint c : contours) {
			// Get Polygon
			MatOfPoint2f contour = new MatOfPoint2f(c.toArray());
			MatOfPoint2f contour_poly = new MatOfPoint2f();
			Imgproc.approxPolyDP(contour, contour_poly, 3d, false); // TODO: Check params
			
			double length = Imgproc.arcLength(contour, true);
			// skip any noise lines
			if(length < 25) {
				continue;
			}
			
			// Get bounding rectangle for each polygon
			Rect rect = Imgproc.boundingRect(new MatOfPoint(contour_poly.toArray()));
			// The found contour is too large to be a useful line
			if (rect.area() > srcImg.size().area() / 100) {
				continue;
			}
			
			// Add the line to the list
			// TODO: Calculate the "middle" line
			Coordinate p1 = new Coordinate(rect.x, rect.y);
			Coordinate p2;
			if (direction == OpenCV.MorphologicalDirection.HORIZONTAL) {
				p2 = new Coordinate(rect.x + rect.width, rect.y);
			}
			else {
				p2 = new Coordinate(rect.x, rect.y + rect.height);
			}
			LineSegment line = new LineSegment(p1, p2);
			list.add(line);
		}
	}

}
