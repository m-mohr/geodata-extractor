package de.lutana.geodataextractor.detector.cv;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Rect;
import org.openimaj.image.FImage;
import org.openimaj.image.text.extraction.swt.LineCandidate;
import org.openimaj.image.text.extraction.swt.SWTTextDetector;
import org.openimaj.math.geometry.shape.Rectangle;

public class StrokeWidthTransformTextDetector extends CvTextDetector {

	protected SWTTextDetector.Options options;

	public StrokeWidthTransformTextDetector(CvGraphic img) {
		super(img);
		this.options = new SWTTextDetector.Options();
		this.options.direction = null;
		// Set default values we found to deliver good results

		// Letter related
//		this.setMinArea(38); // default: 38
		this.setLetterVarianceMean(1); // default: 0.93
//		this.setMaxAspectRatio(10); // default: 10
		this.setMaxDiameterStrokeRatio(25); // default: 10
		this.setMaxNumOverlappingBoxes(2); // default: 10
		this.setMinHeight(9); // default: 10
		this.setMaxHeight(img.getHeight() / 20); // default: 300

		// Word related
//		this.setWordBreakdownRatio(1); // default: 1

		// Line related
		this.setMedianStrokeWidthRatio(4); // default: 2
		this.setLetterHeightRatio(3); // default: 2
		this.setIntensityThreshold(0.25f); // default: 0.12
//		this.setWidthMultiplier(3); // default: 3
		this.setIntersectRatio(1.3f); // default: 1.3
//		this.setMinLettersPerLine(3); // default: 3

		// SWT related
		this.setStrokeWidthRatio(5); // default: 3.0
		this.setMaxStrokeWidth(50); // default: 70
	}
	
	public List<LineCandidate> detectLines() {
		FImage fimage = this.img.getFImage();
		
		if (this.options.direction == null) {
			this.options.direction = this.detectBestDirection();
		}

		SWTTextDetector swt = new SWTTextDetector(options);
		swt.analyseImage(fimage);
		List<LineCandidate> list = swt.getLines();
		// ToDo: Merge overlapping lines/bounds.
		return list;
	}
	
	public static Rect toRect(LineCandidate line) {
		Rectangle r = line.getRegularBoundingBox();
		return new Rect(Math.round(r.x), Math.round(r.y), Math.round(r.width), Math.round(r.height));
	}
	
	public static List<Rect> toRectList(List<LineCandidate> lines) {
		List<Rect> list = new ArrayList<>();
		for (LineCandidate line : lines) {
			list.add(toRect(line));
		}
		return list;
	}

	@Override
	public List<Rect> detect() {
		List<LineCandidate> lines = this.detectLines();
		return toRectList(lines);
	}

	protected SWTTextDetector.Direction detectBestDirection() {
		int brightness = img.getBackgroundBrightness();
		switch (brightness) {
			case 1:
				// probably white background
				return SWTTextDetector.Direction.DarkOnLight;
			case 0:
				// probably black background
				return SWTTextDetector.Direction.LightOnDark;
			default:
				// Can't decide, do both
				return SWTTextDetector.Direction.Both;
		}
	}

	public void setDetectDarkTextOnBrightBackgroundOnly() {
		this.options.direction = SWTTextDetector.Direction.DarkOnLight;
	}

	public void setDetectBrightTextOnDarkBackgroundOnly() {
		this.options.direction = SWTTextDetector.Direction.LightOnDark;
	}

	public void setDetectTextOnAnyBackground() {
		this.options.direction = SWTTextDetector.Direction.Both;
	}

	/**
	 * Maximum allowed ratio of a pair of stroke widths for them to be
	 * considered part of the same connected component.
	 *
	 * @return the strokeWidthRatio
	 */
	public float getStrokeWidthRatio() {
		return options.strokeWidthRatio;
	}

	/**
	 * Maximum allowed ratio of a pair of stroke widths for them to be
	 * considered part of the same connected component.
	 *
	 * @param strokeWidthRatio the strokeWidthRatio to set
	 */
	public final void setStrokeWidthRatio(float strokeWidthRatio) {
		options.strokeWidthRatio = strokeWidthRatio;
	}

	/**
	 * Maximum allowed variance of stroke width in a single character as a
	 * percentage of the mean.
	 *
	 * @return the letterVarianceMean
	 */
	public double getLetterVarianceMean() {
		return options.letterVarianceMean;
	}

	/**
	 * Maximum allowed variance of stroke width in a single character as a
	 * percentage of the mean.
	 *
	 * @param letterVarianceMean the letterVarianceMean to set
	 */
	public final void setLetterVarianceMean(double letterVarianceMean) {
		options.letterVarianceMean = letterVarianceMean;
	}

	/**
	 * Maximum allowed aspect ratio for a single letter
	 *
	 * @return the maxAspectRatio
	 */
	public double getMaxAspectRatio() {
		return options.maxAspectRatio;
	}

	/**
	 * Maximum allowed aspect ratio for a single letter
	 *
	 * @param maxAspectRatio the maxAspectRatio to set
	 */
	public final void setMaxAspectRatio(double maxAspectRatio) {
		options.maxAspectRatio = maxAspectRatio;
	}

	/**
	 * Maximum allowed ratio of diameter to stroke width for a single character.
	 *
	 * @return the maxDiameterStrokeRatio
	 */
	public double getMaxDiameterStrokeRatio() {
		return options.maxDiameterStrokeRatio;
	}

	/**
	 * Maximum allowed ratio of diameter to stroke width for a single character.
	 *
	 * @param maxDiameterStrokeRatio the maxDiameterStrokeRatio to set
	 */
	public final void setMaxDiameterStrokeRatio(double maxDiameterStrokeRatio) {
		options.maxDiameterStrokeRatio = maxDiameterStrokeRatio;
	}

	/**
	 * Minimum allowed component size; used to quickly filter out small
	 * components.
	 *
	 * @return the minArea
	 */
	public int getMinArea() {
		return options.minArea;
	}

	/**
	 * Minimum allowed component size; used to quickly filter out small
	 * components.
	 *
	 * @param minArea the minArea to set
	 */
	public final void setMinArea(int minArea) {
		options.minArea = minArea;
	}

	/**
	 * Minimum character height
	 *
	 * @return the minHeight
	 */
	public float getMinHeight() {
		return options.minHeight;
	}

	/**
	 * Minimum character height
	 *
	 * @param minHeight the minHeight to set
	 */
	public final void setMinHeight(float minHeight) {
		options.minHeight = minHeight;
	}

	/**
	 * Maximum character height
	 *
	 * @return the maxHeight
	 */
	public float getMaxHeight() {
		return options.maxHeight;
	}

	/**
	 * Maximum character height
	 *
	 * @param maxHeight the maxHeight to set
	 */
	public final void setMaxHeight(float maxHeight) {
		options.maxHeight = maxHeight;
	}

	/**
	 * Maximum allowed number of overlapping characters
	 *
	 * @return the maxNumOverlappingBoxes
	 */
	public int getMaxNumOverlappingBoxes() {
		return options.maxNumOverlappingBoxes;
	}

	/**
	 * Maximum allowed number of overlapping characters
	 *
	 * @param maxNumOverlappingBoxes the maxNumOverlappingBoxes to set
	 */
	public final void setMaxNumOverlappingBoxes(int maxNumOverlappingBoxes) {
		options.maxNumOverlappingBoxes = maxNumOverlappingBoxes;
	}

	/**
	 * Maximum allowed stroke width
	 *
	 * @return the maxStrokeWidth
	 */
	public int getMaxStrokeWidth() {
		return options.maxStrokeWidth;
	}

	/**
	 * Maximum allowed stroke width
	 *
	 * @param maxStrokeWidth the maxStrokeWidth to set
	 */
	public final void setMaxStrokeWidth(int maxStrokeWidth) {
		options.maxStrokeWidth = maxStrokeWidth;
	}

	/**
	 * Maximum ratio of stroke width for two letters to be considered to be
	 * related
	 *
	 * @return the medianStrokeWidthRatio
	 */
	public float getMedianStrokeWidthRatio() {
		return options.medianStrokeWidthRatio;
	}

	/**
	 * Maximum ratio of stroke width for two letters to be considered to be
	 * related
	 *
	 * @param medianStrokeWidthRatio the medianStrokeWidthRatio to set
	 */
	public final void setMedianStrokeWidthRatio(float medianStrokeWidthRatio) {
		options.medianStrokeWidthRatio = medianStrokeWidthRatio;
	}

	/**
	 * Maximum ratio of height for two letters to be considered to be related
	 *
	 * @return the letterHeightRatio
	 */
	public float getLetterHeightRatio() {
		return options.letterHeightRatio;
	}

	/**
	 * Maximum ratio of height for two letters to be considered to be related
	 *
	 * @param letterHeightRatio the letterHeightRatio to set
	 */
	public final void setLetterHeightRatio(float letterHeightRatio) {
		options.letterHeightRatio = letterHeightRatio;
	}

	/**
	 * Maximum difference in intensity for two letters to be considered to be
	 * related
	 *
	 * @return the intensityThreshold
	 */
	public float getIntensityThreshold() {
		return options.intensityThreshold;
	}

	/**
	 * Maximum difference in intensity for two letters to be considered to be
	 * related
	 *
	 * @param intensityThreshold the intensityThreshold to set
	 */
	public final void setIntensityThreshold(float intensityThreshold) {
		options.intensityThreshold = intensityThreshold;
	}

	/**
	 * The width multiplier for two letters to be considered to be related.
	 * Distance between centroids must be less than widthMultiplier *
	 * maxLetterWidth.
	 *
	 * @return the widthMultiplier
	 */
	public float getWidthMultiplier() {
		return options.widthMultiplier;
	}

	/**
	 * The width multiplier for two letters to be considered to be related.
	 * Distance between centroids must be less than widthMultiplier *
	 * maxLetterWidth.
	 *
	 * @param widthMultiplier the widthMultiplier to set
	 */
	public final void setWidthMultiplier(float widthMultiplier) {
		options.widthMultiplier = widthMultiplier;
	}

	/**
	 * Minimum number of allowed letters on a line
	 *
	 * @return the minLettersPerLine
	 */
	public int getMinLettersPerLine() {
		return options.minLettersPerLine;
	}

	/**
	 * Minimum number of allowed letters on a line
	 *
	 * @param minLettersPerLine the minLettersPerLine to set
	 */
	public final void setMinLettersPerLine(int minLettersPerLine) {
		options.minLettersPerLine = minLettersPerLine;
	}

	/**
	 * Ratio of vertical intersection for character pairing. This helps ensure
	 * that the characters are horizontal.
	 *
	 * @return the intersectRatio
	 */
	public float getIntersectRatio() {
		return options.intersectRatio;
	}

	/**
	 * Ratio of vertical intersection for character pairing. This helps ensure
	 * that the characters are horizontal.
	 *
	 * @param intersectRatio the intersectRatio to set
	 */
	public final void setIntersectRatio(float intersectRatio) {
		options.intersectRatio = intersectRatio;
	}

	/**
	 * Ratio of the interclass std dev of the letter spacings to the mean to
	 * suggest a word break.
	 *
	 * @return the wordBreakdownRatio
	 */
	public float getWordBreakdownRatio() {
		return options.wordBreakdownRatio;
	}

	/**
	 * Ratio of the interclass std dev of the letter spacings to the mean to
	 * suggest a word break.
	 *
	 * @param wordBreakdownRatio the wordBreakdownRatio to set
	 */
	public final void setWordBreakdownRatio(float wordBreakdownRatio) {
		options.wordBreakdownRatio = wordBreakdownRatio;
	}

}
