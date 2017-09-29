package de.lutana.geodataextractor.util;

import java.util.Collections;

public class Util {
	
	public static String strRepeat(String str, int num) {
		return String.join("", Collections.nCopies(num, str));
	}
	
}
