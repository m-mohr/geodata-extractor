package de.lutana.geodataextractor.util;

import java.io.File;

/**
 *
 * @author Matthias Mohr
 */
public class FileExtension {

	/**
	 * Returns the file extension in lower case and without leading dot.
	 * 
	 * If no dot (= no file extension) is found an empty string is returned.
	 * 
	 * @param path
	 * @return 
	 */
	public static String get(String path) {
		return FileExtension.get(new File(path));
	}
	
	/**
	 * Returns the file extension in lower case and without leading dot.
	 * 
	 * If no dot (= no file extension) is found an empty string is returned.
	 * 
	 * @param file
	 * @return 
	 */
	public static String get(File file) {
		String fileName = file.getName();
		int i = fileName.lastIndexOf('.');
		if (i >= 0) {
			return fileName.substring(i + 1).toLowerCase();
		}
		return "";
	}
	
	/**
	 * Replaces the file extension with another file extension (without dot).
	 * 
	 * If no dot (= no file extension) is found the extension (including a leading dot) is appended.
	 * 
	 * @param path
	 * @param newExtension
	 * @return 
	 */
	public static String replace(String path, String newExtension) {
		int i = path.lastIndexOf('.');
		if (i >= 0) {
			return path.substring(0, i + 1) + newExtension;
		}
		return path + "." + newExtension;
	}
	
}
