package de.lutana.geodataextractor.detector.cv;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class JImageFrame extends JFrame {
	
	private final List<BufferedImage> img;
	private final List<String> title;
	private int current;
	
	private JImagePanel panel;
	private JButton backBtn;
	private JButton forwardBtn;
	
	public JImageFrame(BufferedImage bImage, String title) {
		this.current = 0;
		this.img = new ArrayList<>();
		this.title = new ArrayList<>();
		
		this.panel = new JImagePanel(this);
		this.backBtn = new JButton("<<");
		this.backBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				prev();
			}
		});
		this.forwardBtn = new JButton(">>");
		this.forwardBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				next();
			}
		});

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (e.getID() != KeyEvent.KEY_PRESSED) {
					return false;
				}
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					return next();
				}
				else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					return prev();
				}
				return false;
			}
		});
		
		this.setLayout(new BorderLayout());
		this.setSize(640, 480);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.add(this.panel, BorderLayout.CENTER);
		this.add(this.backBtn, BorderLayout.WEST);
		this.add(this.forwardBtn, BorderLayout.EAST);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// needs to be last
		this.addImage(bImage, title);
	}
	
	public boolean prev() {
		int prev = current - 1;
		if (prev < 0) {
			return false;
		}
		updateUI(prev);
		return true;
	}
	
	public boolean next() {
		int next = current + 1;
		if (next >= img.size()) {
			return false;
		}
		updateUI(next);
		return true;
	}
	
	public void addImage(BufferedImage bImage, String title) {
		this.img.add(bImage);
		if (title == null || title.isEmpty()) {
			title = "Image #" + this.img.size();
		}
		this.title.add(title);
		this.updateUI(current);
	}
	
	private void updateUI(int num) {
		current = num;
		this.setTitle(this.title.get(current));
		this.panel.validate();
		this.backBtn.setEnabled(current > 0);
		this.forwardBtn.setEnabled(current < this.img.size()-1);
	}
	
	public BufferedImage getCurrentImage() {
		return this.img.get(current);
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
			
			BufferedImage image = this.frame.getCurrentImage();

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