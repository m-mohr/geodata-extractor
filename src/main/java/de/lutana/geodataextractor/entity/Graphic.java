package de.lutana.geodataextractor.entity;

import java.io.File;

public class Graphic {
	
	protected File file;
	
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
	
	@Override
	public String toString() {
		return this.file.getName();
	}
	
}
