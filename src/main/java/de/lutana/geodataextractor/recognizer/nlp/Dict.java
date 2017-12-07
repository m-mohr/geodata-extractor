package de.lutana.geodataextractor.recognizer.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

public class Dict {

    private static HashSet<String> dict;

    public static boolean contains(String word) {
        // lazy load the dictionary taken from http://app.aspell.net/create (SCOWL size 35, American and British English)
        if (dict == null) {
            dict = new HashSet<>();

            BufferedReader br = new BufferedReader(new InputStreamReader(Dict.class.getClassLoader().getResourceAsStream("dict/en.txt")));
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    dict.add(line.toLowerCase());
				}
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return dict.contains(word.toLowerCase());
    }
}
