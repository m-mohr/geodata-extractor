package de.lutana.geodataextractor.util;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {
	
	public static String strRepeat(String str, int num) {
		return String.join("", Collections.nCopies(num, str));
	}
	
    /**
     * Splits a list into a set of sublists (preserving order) where
     * the size of each sublist is bound by a given max size, and
     * ensuring that no list has fewer than half the max size number
     * of elements.
     * 
     * In other words, you won't get a little rinky-dink sublist at the
     * end that only has one or two items from the original list.
     * 
     * Based on: http://www.chinhdo.com/20080515/chunking/
     * 
     * @param list          list to be chunkified
     * @param maxChunkSize  how big you want the chunks to be
     * @return              a chunkified list (i.e., list of sublists)
	 * @see https://github.com/Berico-Technologies/CLAVIN/blob/master/src/main/java/com/bericotech/clavin/resolver/LuceneLocationResolver.java
     */
    public static <T> List<List<T>> chunkifyList(List<T> list, int maxChunkSize) {
        // sanity-check input param
        if (maxChunkSize < 1)
            throw new InvalidParameterException("maxChunkSize must be greater than zero");
        
        // initialize return object
        List<List<T>> chunkedLists = new ArrayList<List<T>>();
        
        // if the given list is smaller than the maxChunksize, there's
        // no need to break it up into chunks
        if (list.size() <= maxChunkSize) {
            chunkedLists.add(list);
            return chunkedLists;
        }
        
        // initialize counters
        int index = 0;
        int count;
        
        // loop through and grab chunks of maxChunkSize from the
        // original list, but stop early enough so that we don't wind
        // up with tiny runt chunks at the end
        while (index < list.size() - (maxChunkSize * 2)) {
            count = Math.min(index + maxChunkSize, list.size());
            chunkedLists.add(list.subList(index, count));
            index += maxChunkSize;
        }
        
        // take whatever's left, split it into two relatively-equal
        // chunks, and add these to the return object
        count = index + ((list.size() - index) / 2);
        chunkedLists.add(list.subList(index, count));
        chunkedLists.add(list.subList(count, list.size()));
        
        return chunkedLists;
    }
	
}
