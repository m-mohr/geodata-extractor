package de.lutana.geodataextractor.recognizer.gazetteer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharTokenizer;

public final class LuceneAnalyzer extends Analyzer {
	
	public List<String> parse(String text) {
		List<String> result = new ArrayList<>();
		TokenStream tokenStream = this.tokenStream("alternativeNames", new StringReader(text));
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		try {
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				result.add(charTermAttribute.toString());
			}
			tokenStream.close();
		} catch (IOException e) {}
		return result;
	}

	@Override
	protected TokenStreamComponents createComponents(String fn) {
		final Tokenizer tokenizer = new CsvTokenizer();

		// All lowercase
		TokenStream filter = new LowerCaseFilter(tokenizer);
		// Remove possessive suffixes, e.g. "Matt's" => "Matt"
		filter = new EnglishPossessiveFilter(filter);
		// Ignore dots (usually used for abbrev., e.g. St. Gallen)
		// Replace hyphen with space, e.g. Rheda-Wiedenbrück => Rheda Wiedenbrück
		// TODO: Normalize diacritics (e.g. Angoulême => Angouleme)
		filter = new GeoNameFilter(filter);
		// Trim whitespaces
		filter = new TrimFilter(filter);

		return new TokenStreamComponents(tokenizer, filter);
	}

	public class CsvTokenizer extends CharTokenizer {

		@Override
		protected boolean isTokenChar(int c) {
			return c != ',';
		}

	}

	private final class GeoNameFilter extends TokenFilter {

		private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

		public GeoNameFilter(TokenStream in) {
			super(in);
		}

		@Override
		public boolean incrementToken() throws IOException {
			if (input.incrementToken()) {
				char buffer[] = termAtt.buffer();
				int length = termAtt.length();
				for (int i = 0; i < length; i++) {
					final char c = buffer[i];
					switch (c) {
						// Replace '-' with ' '
						case '-':
							buffer[i] = ' ';
							break;
						// Remove '.'
						case '.':
							System.arraycopy(buffer, i + 1, buffer, i, (length - i - 1));
							length--;
							buffer = termAtt.resizeBuffer(length);
							break;
					}
				}
				termAtt.setLength(length);
				return true;
			} else {
				return false;
			}
		}
	}
}
