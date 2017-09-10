package de.lutana.geodataextractor.detector.cv;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Detects horizontal and vertical lines in images.
 * 
 * @see http://answers.opencv.org/question/63847/how-to-extract-tables-from-an-image/
 * @see http://answers.opencv.org/question/56496/implementation-question-how-to-create-bounding-boxes-around-answers-on-worksheets/
 * @author Matthias Mohr
 */
public class LineParser {
	
	private static final short DIRECTION_HORIZONTAL = 0;
	private static final short DIRECTION_VERTICAL = 1;
	
	private final BufferedImage img;
	private Integer scale;
	
	protected LineParser(BufferedImage img) {
		this.img = img;
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
	 * @throws NullPointerException 
	 */
	public List<LineSegment> parse() throws NullPointerException {
		List<LineSegment> lines = new ArrayList<>();
		if (this.img == null) {
			throw new NullPointerException();
		}
		
		OpenCV cv = OpenCV.getInstance();

		// Convert to black and white image
		Mat bw = cv.toMonotoneCustom(this.img, true);

		Mat horizontal = this.createLineMask(bw, DIRECTION_HORIZONTAL);
		this.extractLines(horizontal, DIRECTION_HORIZONTAL, lines);
		
		Mat vertical = this.createLineMask(bw, DIRECTION_VERTICAL);
		this.extractLines(vertical, DIRECTION_VERTICAL, lines);
		
		return lines;
	}
	
	private void extractLines(Mat srcImg, short direction, List<LineSegment> list) throws IllegalArgumentException {
		if (direction != DIRECTION_VERTICAL && direction != DIRECTION_HORIZONTAL) {
			throw new IllegalArgumentException("Second parameter needs to be DIRECTION_HORIZONTAL or DIRECTION_VERTICAL.");
		}
		
		// Find external contour
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(srcImg, contours, hierarchy, Imgproc.RETR_CCOMP,Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

		for(int i = 0; i < contours.size(); i++ ) {
			// Get Polygon
			MatOfPoint2f contour = new MatOfPoint2f(contours.get(i).toArray());
			MatOfPoint2f contour_poly = new MatOfPoint2f();
			Imgproc.approxPolyDP(contour, contour_poly, 3d, false); // TODO: Check params
			
			double length = Imgproc.arcLength(contour, true);
			// skip any noise lines
			if(length < 25) {
				continue;
			}
			
			// Get bounding rectangle for each polygon
			Rect rect = Imgproc.boundingRect(new MatOfPoint(contour_poly.toArray()));
			
			// Add the line to the list
			// TODO: Calculate the "middle" line
			Coordinate p1 = new Coordinate(rect.x, rect.y);
			Coordinate p2;
			if (direction == DIRECTION_HORIZONTAL) {
				p2 = new Coordinate(rect.x + rect.width, rect.y);
			}
			else {
				p2 = new Coordinate(rect.x, rect.y + rect.height);
			}
			LineSegment line = new LineSegment(p1, p2);
			list.add(line);
		}
	}
	
	private Mat createLineMask(Mat srcImg, short direction) throws IllegalArgumentException {
		if (direction != DIRECTION_VERTICAL && direction != DIRECTION_HORIZONTAL) {
			throw new IllegalArgumentException("Second parameter needs to be DIRECTION_HORIZONTAL or DIRECTION_VERTICAL.");
		}
		
		Mat resultImg = srcImg.clone();

		// Specify size on axis
		Size size;
		if (direction == DIRECTION_HORIZONTAL) {
			size = new Size(resultImg.cols() / this.scale, 1);
		}
		else { // VERTICAL
			size = new Size(1, resultImg.rows() / this.scale);
		}

		// Create structure element for extracting horizontal lines through morphology operations
		Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, size);

		// Apply morphology operation
		Imgproc.erode(resultImg, resultImg, structure, new Point(-1, -1), 1);
		Imgproc.dilate(resultImg, resultImg, structure, new Point(-1, -1), 1);

		return resultImg;
	}
			
}
