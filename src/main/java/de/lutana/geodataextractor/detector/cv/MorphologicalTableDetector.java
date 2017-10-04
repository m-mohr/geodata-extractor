package de.lutana.geodataextractor.detector.cv;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

/**
 * Detects tables in images.
 * 
 * Due to it's design some kind of charts (with lines at each tick) are recognized as tables.
 * 
 * @see http://answers.opencv.org/question/63847/how-to-extract-tables-from-an-image/
 * @author Matthias Mohr
 */
public class MorphologicalTableDetector extends CvTableDetector {
	
	private int scale;
	protected int minAreaSize;
	protected int minCellSize;
	
	public MorphologicalTableDetector(CvGraphic img) {
		super(img);
		this.scale = 10;
		this.minCellSize = Math.min(img.getWidth(), img.getHeight()) / 100;
		this.minAreaSize = 10; //this.minCellSize * this.minCellSize * 10;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public List<Rect> detect() {
		List<Rect> rectangles = new ArrayList<>();
		rectangles.addAll(this.detectTablesWithBorders());
		rectangles.addAll(this.detectTablesWithGhostBorders());
		return rectangles;
	}
	
	public List<Rect> detectTablesWithGhostBorders() {
		OpenCV cv = OpenCV.getInstance();

		Mat gray = cv.toGrayscale(this.img.getMat());

		// Convert to black and white image
		Imgproc.adaptiveThreshold(gray, gray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, -2);
		
		// Make text and other small elements to black blocks
		gray = cv.closeGaps(gray, this.minCellSize, Imgproc.MORPH_RECT);
		
		return this.detectTablesWithBorders(gray);
	}
	
	public List<Rect> detectTablesWithBorders() {
		OpenCV cv = OpenCV.getInstance();

		Mat gray = cv.toGrayscale(this.img.getMat());
		Core.bitwise_not(gray, gray);

		// Convert to black and white image
		Imgproc.adaptiveThreshold(gray, gray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, -2);
		
		return this.detectTablesWithBorders(gray);
	}
	
	public List<Rect> detectTablesWithBorders(Mat image) {
		List<Rect> list = new ArrayList<>();
		OpenCV cv = OpenCV.getInstance();

		// Create the images that will use to extract the horizonta and vertical lines
		Mat horizontal = cv.createLineMask(image, OpenCV.MorphologicalDirection.HORIZONTAL, scale);
		Mat vertical = cv.createLineMask(image, OpenCV.MorphologicalDirection.VERTICAL, scale);

		// Merge table borders that are close to each other
		horizontal = cv.closeGaps(horizontal, this.minCellSize, Imgproc.MORPH_RECT);
		vertical = cv.closeGaps(vertical, this.minCellSize, Imgproc.MORPH_RECT);

		// create a mask which includes the tables
		Mat mask = new Mat();
		Core.bitwise_or(horizontal, vertical, mask);

		// find the joints between the lines of the tables, we will use this information in order to descriminate tables from pictures (tables will contain more than 4 joints while a picture only 4 (i.e. at the corners))
		Mat joints = new Mat();
		Core.bitwise_and(horizontal, vertical, joints);

		// Find external contours from the mask, which most probably will belong to tables or to images
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

		for(MatOfPoint c : contours) {
			// find the area of each contour
			double area = Imgproc.contourArea(c);

	        // filter individual lines of blobs that might exist and they do not represent a table
			if(area < this.minAreaSize) {
				continue;
			}

			MatOfPoint2f contour = new MatOfPoint2f(c.toArray());
			MatOfPoint2f contours_poly = new MatOfPoint2f();
			Imgproc.approxPolyDP(contour, contours_poly, 3d, true);
			Rect boundRect = Imgproc.boundingRect(new MatOfPoint(contours_poly.toArray()));

			// find the number of joints that each table has
			Mat roi = joints.submat(boundRect);

			Mat joints_hierarchy = new Mat();
			List<MatOfPoint> joints_contours = new ArrayList<>();
			Imgproc.findContours(roi, joints_contours, joints_hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

			// if the number is lower than 12 it's most likely not a table (at least 2x3 table expected)
			if(joints_contours.size() < 12)
				continue;

			list.add(boundRect);
		}
		
		return list;
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
	 * The minimum area needed to be detected as table.
	 * 
	 * @param minAreaSize the minAreaSize to set
	 */
	public void setMinAreaSize(int minAreaSize) {
		this.minAreaSize = minAreaSize;
	}

	/**
	 * The minimum width or height needed to be detected as cell.
	 * 
	 * @param minCellSize the minCellSize to set
	 */
	public void setMinCellSize(int minCellSize) {
		this.minCellSize = minCellSize;
	}

}
