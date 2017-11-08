package de.lutana.geodataextractor.recognizer.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

public class Demonyms {

    /**
     * Set of demonyms to filter out from extracted location names.
     */
    private static HashSet<String> demonyms;
	
    /**
     * Various named entity recognizers tend to mistakenly extract
     * demonyms (i.e., names for residents of localities (e.g.,
     * American, British)) as place names, which tends to gum up the
     * works for the resolver, so this method filters them out from
     * the list of {@link LocationOccurrence}s passed to the resolver.
     *
     * @param extractedLocation extraction location name to filter
     * @return                  true if input is a demonym, false otherwise
     */
    public static boolean isDemonym(LocationOccurrence extractedLocation) {
        // lazy load set of demonyms
        if (demonyms == null) {
            // populate set of demonyms to filter out from results, source:
            // http://en.wikipedia.org/wiki/List_of_adjectival_and_demonymic_forms_for_countries_and_nations
            demonyms = new HashSet<>();

            BufferedReader br = new BufferedReader(new InputStreamReader(Demonyms.class.getClassLoader().getResourceAsStream("clavin/Demonyms.txt")));

            String line;
            try {
                while ((line = br.readLine()) != null) {
                    demonyms.add(line);
				}
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return demonyms.contains(extractedLocation.getText());
    }
}
