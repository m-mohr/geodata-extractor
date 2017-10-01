package de.lutana.geodataextractor.detector;

import de.lutana.geodataextractor.detector.gazetteer.GeoName;
import de.lutana.geodataextractor.detector.gazetteer.LuceneIndex;
import de.lutana.geodataextractor.detector.nlp.Demonyms;
import de.lutana.geodataextractor.detector.nlp.LocationExtractor;
import de.lutana.geodataextractor.detector.nlp.LocationOccurrence;
import de.lutana.geodataextractor.detector.nlp.StanfordExtractor;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.entity.LocationCollection;
import de.lutana.geodataextractor.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoNamesTextDetector implements TextDetector {
	
	private LocationExtractor extractor;
	private LuceneIndex index;
	private boolean fuzzyIfNoResultsMode;
	private int maxContextWindow;
	
	public GeoNamesTextDetector() throws IOException, ClassNotFoundException {
		this.index = new LuceneIndex();
		this.index.load();
		this.extractor = new StanfordExtractor();
		this.fuzzyIfNoResultsMode = true;
		this.maxContextWindow = 5;
	}

	@Override
	public void detect(String text, LocationCollection locations) {
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.debug("input: {}", text);

		List<LocationOccurrence> occurrences = extractor.extractLocationNames(text);
        logger.debug("extracted: {}", occurrences);

		List<GeoName> bestCandidates = this.resolveLocations(occurrences);
        logger.debug("resolved: {}", bestCandidates);

		for(GeoName geoname : bestCandidates) {
			Location l = geoname.getLocation();
			if (l != null) {
				locations.add(l);
			}
		}
	}
	
	protected List<GeoName> resolveLocations(List<LocationOccurrence> occurrences) {
		List<GeoName> bestCandidates = new ArrayList<GeoName>();
		
        // are you forgetting something? -- short-circuit if no locations were provided
        if (occurrences == null || occurrences.isEmpty()) {
            return bestCandidates;
        }

        /* Various named entity recognizers tend to mistakenly extract demonyms
         * (i.e., names for residents of localities (e.g., American, British))
         * as place names, which tends to gum up the works, so we make sure to
         * filter them out from the list of {@link LocationOccurrence}s passed
         * to the resolver.
         */
        List<LocationOccurrence> filteredLocations = new ArrayList<>();
        for (LocationOccurrence occurrence : occurrences) {
            if (!Demonyms.isDemonym(occurrence)) {
                filteredLocations.add(occurrence);
			}
		}

        // did we filter *everything* out?
        if (filteredLocations.isEmpty()) {
            return bestCandidates;
        }
		
		// stores all possible matches for each location name
		List<List<GeoName>> allCandidates = new ArrayList<>();

		// loop through all the location names
		for (LocationOccurrence location : filteredLocations) {
			// get all possible matches
			List<GeoName> candidates = index.find(location.getText(), fuzzyIfNoResultsMode);

			// if we found some possible matches, save them
			if (candidates.size() > 0) {
				allCandidates.add(candidates);
			}
		}

		// split-up allCandidates into reasonably-sized chunks to
		// limit computational load when heuristically selecting
		// the best matches
		for (List<List<GeoName>> theseCandidates : Util.chunkifyList(allCandidates, maxContextWindow)) {
			// select the best match for each location name based
			// based on heuristics
			bestCandidates.addAll(pickBestCandidates(theseCandidates));
		}
		
		return bestCandidates;
	}
/*
	public LocationCollection query(String text) {
		// Search in index
		LocationCollection locations = new LocationCollection();
		List<GeoName> geoNames = this.index.find(text, this.fuzzyIfNoResultsMode);
		for(GeoName geoName : geoNames) {
			Location location = geoName.getLocation();
			if (location != null && geoName.getResultScore() > 0.5 && geoName.getImportance() > 0.25) {
				location.setProbability(geoName.getImportance());
				locations.add(location);
			}
		}
		return locations;
	}
*/
    /**
     * Uses heuristics to select the best match for each location name
     * extracted from a document, choosing from among a list of lists
     * of candidate matches.
     *
     * Although not guaranteeing an optimal solution (enumerating &
     * evaluating each possible combination is too costly), it does a
     * decent job of cracking the "Springfield Problem" by selecting
     * candidates that would make sense to appear together based on
     * common country and admin1 codes (i.e., states or provinces).
     *
     * For example, if we also see "Boston" mentioned in a document
     * that contains "Springfield," we'd use this as a clue that we
     * ought to choose Springfield, MA over Springfield, IL or
     * Springfield, MO.
     *
     * TODO: consider lat/lon distance in addition to shared
     *       CountryCodes and Admin1Codes.
     *
     * @param allCandidates list of lists of candidate matches for locations names
     * @return              list of best matches for each location name
     */
    private List<GeoName> pickBestCandidates(final List<List<GeoName>> allCandidates) {
        // initialize return object
        List<GeoName> bestCandidates = new ArrayList<>();

        // variables used in heuristic matching
        Set<String> countries;
        Set<String> states;
        float score;

        // initial values for variables controlling recursion
        float newMaxScore = 0;
        float oldMaxScore;

        // controls window of Lucene hits for each location considered
        // context-based heuristic matching, initialized as a "magic
        // number" of *3* based on tests of the "Springfield Problem"
        int candidateDepth = 3;

        // keep searching deeper & deeper for better combinations of
        // candidate matches, as long as the scores are improving
        do {
            // reset the threshold for recursion
            oldMaxScore = newMaxScore;

            // loop through all combinations up to the specified depth.
            // first recursive call for each depth starts at index 0
            for (List<GeoName> combo : this.generateAllCombos(allCandidates, 0, candidateDepth)) {
                // these lists store the country codes & admin1 codes for each candidate
                countries = new HashSet<>();
                states = new HashSet<>();
                for (GeoName location : combo) {
                    countries.add(location.getCountryCode());
                    states.add(location.getCountryCode() + location.getState());
                }

                // calculate a score for this particular combination based on commonality
                // of country codes & admin1 codes, and the cost of searching this deep
                // TODO: tune this score calculation!
                score = ((float)allCandidates.size() / (countries.size() + states.size())) / candidateDepth;


                // if this is the best we've seen during this loop, update the return value
                if (score > newMaxScore) {
                    newMaxScore = score;
                    bestCandidates = combo;
                }
            }

            // search one level deeper in the next loop
            candidateDepth++;

        } while (newMaxScore > oldMaxScore);
        // keep searching while the scores are monotonically increasing

        return bestCandidates;
    }

    /**
     * Recursive helper function for
     * {@link #pickBestCandidates}.
     *
     * Generates all combinations of candidate matches from each
     * location, down to the specified depth through the lists.
     *
     * Adapted from:
     * http://www.daniweb.com/software-development/java/threads/177956/generating-all-possible-combinations-from-list-of-sublists#post882553
     *
     * @param allCandidates list of lists of candidate matches for all location names
     * @param index         keeps track of which location we're working on for recursive calls
     * @param depth         max depth into list we're searching during this recursion
     * @return              all combinations of candidate matches for each location, down to the specified depth
     */
    private List<List<GeoName>> generateAllCombos(final List<List<GeoName>> allCandidates, final int index, final int depth) {
        // stopping condition
        if (index == allCandidates.size()) {
            // return a list with an empty list
            List<List<GeoName>> result = new ArrayList<>();
            result.add(new ArrayList<GeoName>());
            return result;
        }

        // initialize return object
        List<List<GeoName>> result = new ArrayList<>();

        // recursive call
        List<List<GeoName>> recursive = generateAllCombos(allCandidates, index+1, depth);

        // for each element of the first list of input, up to depth or list size
        for (int j = 0; j < Math.min(allCandidates.get(index).size(), depth); j++) {
            // add the element to all combinations obtained for the rest of the lists
            for (List<GeoName> recList : recursive) {
                List<GeoName> newList = new ArrayList<GeoName>();
                // add element of the first list
                newList.add(allCandidates.get(index).get(j));
                // copy a combination from recursive
                for (GeoName listItem : recList) {
                    newList.add(listItem);
                }
                // add new combination to result
                result.add(newList);
            }
        }

        return result;
    }
	
	public void close() {
		this.index.close();
	}
	
	public void enableFuzzyMode(boolean enable) {
		this.fuzzyIfNoResultsMode = enable;
	}
	
	public boolean isFuzzyModeEnabled() {
		return this.fuzzyIfNoResultsMode;
	}

	/**
	 * @return the extractor
	 */
	public LocationExtractor getExtractor() {
		return extractor;
	}

	/**
	 * @param extractor the extractor to set
	 */
	public void setExtractor(LocationExtractor extractor) {
		this.extractor = extractor;
	}

	/**
	 * @return the index
	 */
	public LuceneIndex getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(LuceneIndex index) {
		this.index = index;
	}

	/**
	 * @return the maxContextWindow
	 */
	public int getMaxContextWindow() {
		return maxContextWindow;
	}

	/**
	 * @param maxContextWindow the maxContextWindow to set
	 */
	public void setMaxContextWindow(int maxContextWindow) {
		this.maxContextWindow = maxContextWindow;
	}
	
}