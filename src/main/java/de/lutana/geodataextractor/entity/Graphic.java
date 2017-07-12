package de.lutana.geodataextractor.entity;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Graphic {
	
	private File file;
	private BufferedImage image;
	
	public Graphic(File file) {
		this.file = file;
	}

	/**
	 * @return The file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file The file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}
	
	public BufferedImage getBufferedImage() {
		try {
			image = ImageIO.read(this.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return image;
	}
	
	public void freeBufferedImage() {
		this.image = null;
	}
	
}
