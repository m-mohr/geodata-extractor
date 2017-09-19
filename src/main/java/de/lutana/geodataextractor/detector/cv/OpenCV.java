package de.lutana.geodataextractor.detector.cv;

import de.lutana.geodataextractor.util.JImageFrame;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.slf4j.LoggerFactory;

public class OpenCV {

	private static OpenCV instance = null;
	private JImageFrame debugWindow;

	public static OpenCV getInstance() {
		if (instance == null) {
			instance = new OpenCV();
		}
		return instance;
	}

	private OpenCV() {
		nu.pattern.OpenCV.loadShared();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		this.debugWindow = null;
	}

	public LineParser createLineParser(BufferedImage img) {
		return new LineParser(toMat(img));
	}
	
	public GradientTextDetector createGradientTextDetector(BufferedImage img) {
		return new GradientTextDetector(toMat(img));
	}
	
	public EdgeEnhancedTextDetector createEdgeEnhancedTextDetector(BufferedImage img) {
		return new EdgeEnhancedTextDetector(toMat(img));
	}
	
	public StrokeWidthTransformTextDetector createStrokeWidthTransformTextDetector(BufferedImage img) {
		return new StrokeWidthTransformTextDetector(img);
	}
	
	public Mat sharpenGaussian(Mat source) {
        Mat destination = new Mat(source.rows(), source.cols(), source.type());
		Imgproc.GaussianBlur(source, destination, new Size(0,0), 10);
		Core.addWeighted(source, 1.5, destination, -0.5, 0, destination);
		return destination;
	}
	
	public Mat toMonotoneAdaptive(Mat source, boolean inverse) {
		Mat dest = toGrayscale(source);
		Imgproc.medianBlur(dest, dest, 5);
		Imgproc.adaptiveThreshold(dest, dest, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, inverse ? Imgproc.THRESH_BINARY_INV : Imgproc.THRESH_BINARY, 9, 0);
		return dest;
	}

	public Mat toMonotoneCustom(Mat source, boolean inverse) {
		Mat img = source.clone();
		
		if (source.channels() >= 3) {
			final double[] white = new double[] {255,255,255};

			for (int y = 0; y < img.rows(); ++y) {
				for (int x = 0; x < img.cols(); ++x) {
					double[] bgr = img.get(y, x);
					if (bgr[0] != 0 && bgr[1] != 0 && bgr[2] != 0) {
						double r = bgr[2] / 25;
						double g = bgr[1] / 25;
						double b = bgr[0] / 25;
						if (r > 3 || g > 3 || b > 3) {
							img.put(y, x, white);
						} else if (r != g || g != b || b != r) {
							img.put(y, x, white);
						}
					}
				}
			}
		}

		img = toGrayscale(img);
		Imgproc.threshold(img, img, 128, 255, inverse ? Imgproc.THRESH_BINARY_INV : Imgproc.THRESH_BINARY);
		return img;
	}

	/**
	 * Convert to grayscale image.
	 *
	 * @param srcImg
	 * @return
	 */
	public Mat toGrayscale(Mat srcImg) {
		int channels = srcImg.channels();
		if (channels >= 3) {
			Mat gray = new Mat();
			Imgproc.cvtColor(srcImg, gray, channels == 4 ? Imgproc.COLOR_BGRA2GRAY : Imgproc.COLOR_BGR2GRAY);
			return gray;
		} else {
			return srcImg;
		}
	}

	/**
	 *
	 * @param bi
	 * @return
	 * @see
	 * https://stackoverflow.com/questions/33403526/how-to-match-the-color-models-of-bufferedimage-and-mat
	 */
	public Mat toMat(BufferedImage bi) {
		int curCVtype = CvType.CV_8UC4; //Default type
		boolean supportedType = true;

		switch (bi.getType()) {
			case BufferedImage.TYPE_3BYTE_BGR:
				curCVtype = CvType.CV_8UC3;
				break;
			case BufferedImage.TYPE_BYTE_GRAY:
			case BufferedImage.TYPE_BYTE_BINARY:
				curCVtype = CvType.CV_8UC1;
				break;
			case BufferedImage.TYPE_INT_BGR:
			case BufferedImage.TYPE_INT_RGB:
				curCVtype = CvType.CV_32SC3;
				break;
			case BufferedImage.TYPE_INT_ARGB:
			case BufferedImage.TYPE_INT_ARGB_PRE:
				curCVtype = CvType.CV_32SC4;
				break;
			case BufferedImage.TYPE_USHORT_GRAY:
				curCVtype = CvType.CV_16UC1;
				break;
			case BufferedImage.TYPE_4BYTE_ABGR:
			case BufferedImage.TYPE_4BYTE_ABGR_PRE:
				curCVtype = CvType.CV_8UC4;
				break;
			default:
				// BufferedImage.TYPE_BYTE_INDEXED;
				// BufferedImage.TYPE_CUSTOM;
				LoggerFactory.getLogger(getClass()).warn("Unsupported format:" + bi.getType());
				supportedType = false;
		}

		//Convert to Mat
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), curCVtype);
		if (supportedType) {
			// Insert pixel buffer directly
			byte[] pixels = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
			mat.put(0, 0, pixels);
		} else {
			// Convert to RGB first
			int height = bi.getHeight();
			int width = bi.getWidth();
			int[] pixels = bi.getRGB(0, 0, width - 1, height - 1, null, 0, width);

			// Convert ints to bytes
			ByteBuffer byteBuffer = ByteBuffer.allocate(pixels.length * 4);
			IntBuffer intBuffer = byteBuffer.asIntBuffer();
			intBuffer.put(pixels);

			byte[] pixelBytes = byteBuffer.array();

			mat.put(0, 0, pixelBytes);

			// Reorder the channels for Opencv BGRA format from
			// BufferedImage ARGB format
			Mat imgMix = mat.clone();
			ArrayList<Mat> imgSrc = new ArrayList<>();
			imgSrc.add(imgMix);

			ArrayList<Mat> imgDest = new ArrayList<>();
			imgDest.add(mat);

			int[] fromTo = {0, 3, 1, 2, 2, 1, 3, 0}; //Each pair is a channel swap
			Core.mixChannels(imgSrc, imgDest, new MatOfInt(fromTo));
		}
		return mat;
	}

	public void showImage(BufferedImage bImage) {
		this.showImage(bImage, "");
	}

	public void showImage(BufferedImage bImage, String title) {
		// Clone image
		ColorModel cm = bImage.getColorModel();
		BufferedImage bImage2 = new BufferedImage(cm, bImage.copyData(null), cm.isAlphaPremultiplied(), null);
		// Open window
		if (this.debugWindow == null) {
			this.debugWindow = new JImageFrame(bImage2, title);
			this.debugWindow.setVisible(true);
		}
		else {
			this.debugWindow.addImage(bImage2, title);
		}
	}

	public void showImage(Mat mImage) {
		this.showImage(mImage, "");
	}

	public void showImage(Mat mImage, String title) {
		this.showImage(this.toBufferedImage(mImage), title);
	}
	
	public void showContours(List<MatOfPoint> contours, Mat hierarchy, Size srcImgSize) {
		Mat drawing = Mat.zeros(srcImgSize, CvType.CV_8UC3);
		for (int i = 0; i < contours.size(); i++) {
			Imgproc.drawContours(drawing, contours, i, getRandomColorScalar(), 2, 8, hierarchy, 0, new Point());
		}
		showImage(drawing, "Contours");
	}
	
	public static Scalar getRandomColorScalar() {
		return new Scalar(getRandomColor());
	}
	
	public static double[] getRandomColor() {
		return new double[] {Math.random() * 255, Math.random() * 255, Math.random() * 255};
	}

	/**
	 * Convert Mat to BufferedImage.
	 *
	 * According to
	 * http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
	 * this is pretty fast.
	 *
	 * @param m
	 * @return
	 */
	public BufferedImage toBufferedImage(Mat m) {
		int mType = m.type();
		int biType;
		int convertTo = -1;
		if (mType == CvType.CV_8UC3) {
			biType = BufferedImage.TYPE_3BYTE_BGR;
		}
		else if (mType == CvType.CV_8UC1) {
			biType = BufferedImage.TYPE_BYTE_GRAY;
		}
		else if (mType == CvType.CV_32F) {
			biType = BufferedImage.TYPE_BYTE_GRAY;
			convertTo = CvType.CV_8UC1;
		}
		else if (mType == CvType.CV_32SC1) {
			biType = BufferedImage.TYPE_BYTE_GRAY;
			convertTo = CvType.CV_8UC1;
		}
		else if (mType == CvType.CV_32SC3) {
			biType = BufferedImage.TYPE_INT_RGB;
			convertTo = CvType.CV_8UC3;
		}
		else if (mType == CvType.CV_32SC4) {
			biType = BufferedImage.TYPE_INT_ARGB;
			convertTo = CvType.CV_8UC3;
		}
		else {
			throw new UnsupportedOperationException(String.format("Unsupported Mat type %d, channels %d, depth %d", m.type(), m.channels(), m.depth()));
		}
		
		if (convertTo != -1) {
			Mat temp = new Mat();
			m.convertTo(temp, convertTo, 255);
			m = temp;
		}

		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), biType);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;
	}

}
