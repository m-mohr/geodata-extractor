package de.lutana.geodataextractor.detector.cv;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineSegment;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * @author Matthias Mohr
 */
public class HoughProbabilisticLineDetector extends CvLineDetector {
	
	protected int threshold;
	protected double minLineLength;
	protected double maxLineGap;
	protected int pixelPrecision;
	protected double radeanPrecision;
	protected Double angleTolerance;
	protected Integer distanceTolerance;
	
	public HoughProbabilisticLineDetector(CvGraphic img) {
		super(img);
		this.minLineLength = 50;
		this.maxLineGap = this.minLineLength / 10;
		this.threshold = (int) this.minLineLength / 2;
		this.pixelPrecision = 1;
		this.radeanPrecision = Math.PI / 180;
		this.angleTolerance = null;
		this.distanceTolerance = null;
	}
	
	public static List<LineSegment> detect(Mat mat, int pixelPrecision, double radeanPrecision, int threshold, double minLineLength, double maxLineGap) {
		Mat lines = new Mat();
		Imgproc.HoughLinesP(mat, lines, pixelPrecision, radeanPrecision, threshold, minLineLength, maxLineGap);

		List<LineSegment> list = new ArrayList<>();
		for(int i = 0; i < lines.rows(); i++) {
			double[] val = lines.get(i, 0);
			LineSegment line = new LineSegment(val[0], val[1], val[2], val[3]);
			list.add(line);
		}
		return list;
	}

	
	public static void mergeSimilarLines(List<LineSegment> lines, double angleTolerance, int distanceTolerance) {
		// ToDo: Improve merging of non-vertical and non-horizontal lines
		for(int i = 0; i < lines.size();) {
			LineSegment ls1 = lines.get(i);
			ls1.normalize();
			
			double angle1 = ls1.angle();

			boolean changed = false;
			for(int j = i+1; j < lines.size();) {
				LineSegment ls2 = lines.get(j);
				ls2.normalize();

				double angle2 = ls2.angle();
				double angleDiff = Math.abs(angle1 - angle2);

				double distance = ls1.distance(ls2);
				if (distance <= distanceTolerance && angleDiff < angleTolerance) {
					double slope = Math.tan((angle1 + angle2) / 2);
					
					Envelope env = new Envelope(ls1.p0, ls1.p1);
					env.expandToInclude(ls2.p0);
					env.expandToInclude(ls2.p1);
					
					if (slope < 0) {
						ls2.p0 = new Coordinate(env.getMinX(), env.getMinY());
						ls2.p1 = new Coordinate(env.getMaxX(), env.getMaxY());
					}
					else {
						ls2.p0 = new Coordinate(env.getMinX(), env.getMaxY());
						ls2.p1 = new Coordinate(env.getMaxX(), env.getMinY());
					}

					lines.remove(j);
					changed = true;
				}
				else {
					j++;
				}
			}
			if (!changed) {
				i++;
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public List<LineSegment> detect() {
		Mat source = img.getMat();
		Mat edges = OpenCV.getInstance().cannyAdaptive(source);
		List<LineSegment> lines = detect(edges, pixelPrecision, radeanPrecision, threshold, minLineLength, maxLineGap);
		if (angleTolerance != null && distanceTolerance != null) {
			mergeSimilarLines(lines, angleTolerance, distanceTolerance);
		}
		return lines;
	}
	
	public void enableSimilarLineMerge(double angleTolerance, int distanceTolerance) {
		this.angleTolerance = angleTolerance;
		this.distanceTolerance = distanceTolerance;
	}
	
	public void disableSimilarLineMerge() {
		this.angleTolerance = null;
		this.distanceTolerance = null;
	}

	/**
	 * @return the threshold
	 */
	public int getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold the threshold to set
	 */
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	/**
	 * @return the minLineLength
	 */
	public double getMinLineLength() {
		return minLineLength;
	}

	/**
	 * @param minLineLength the minLineLength to set
	 */
	public void setMinLineLength(double minLineLength) {
		this.minLineLength = minLineLength;
	}

	/**
	 * @return the maxLineGap
	 */
	public double getMaxLineGap() {
		return maxLineGap;
	}

	/**
	 * @param maxLineGap the maxLineGap to set
	 */
	public void setMaxLineGap(double maxLineGap) {
		this.maxLineGap = maxLineGap;
	}

}
