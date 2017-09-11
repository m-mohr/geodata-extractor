package de.lutana.geodataextractor.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class JImageFrame extends JFrame {

	private final BufferedImage img;
	
	public JImageFrame(BufferedImage img) {
		this.img = img;
		this.setLayout(new BorderLayout());
		this.setSize(this.img.getWidth() + 10, this.img.getHeight() + 50);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.add(new JImagePanel(this), BorderLayout.CENTER);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public BufferedImage getImage() {
		return this.img;
	}

	private class JImagePanel extends JPanel {
		
		public JImageFrame frame;
		
		public JImagePanel(JImageFrame frame) {
			this.frame = frame;
		}

		public double getScaleFactor(int iMasterSize, int iTargetSize) {
			double dScale = 1;
			if (iMasterSize > iTargetSize) {
				dScale = (double) iTargetSize / (double) iMasterSize;
			}
			else {
				dScale = (double) iTargetSize / (double) iMasterSize;
			}
			return dScale;
		}

		public double getScaleFactorToFit(Dimension original, Dimension toFit) {
			double dScale = 1d;
			if (original != null && toFit != null) {
				double dScaleWidth = getScaleFactor(original.width, toFit.width);
				double dScaleHeight = getScaleFactor(original.height, toFit.height);
				dScale = Math.min(dScaleHeight, dScaleWidth);
			}
			if (dScale > 1) {
				dScale = 1;
			}
			return dScale;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			BufferedImage image = this.frame.getImage();

			double scaleFactor = Math.min(1d, getScaleFactorToFit(new Dimension(image.getWidth(), image.getHeight()), getSize()));
			int scaleWidth = (int) Math.round(image.getWidth() * scaleFactor);
			int scaleHeight = (int) Math.round(image.getHeight() * scaleFactor);

			Image scaled = image.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH);

			int width = getWidth() - 1;
			int height = getHeight() - 1;
			int x = (width - scaled.getWidth(this)) / 2;
			int y = (height - scaled.getHeight(this)) / 2;

			g.drawImage(scaled, x, y, this);
		}

	}

}