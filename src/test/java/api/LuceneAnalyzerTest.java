package api;

import de.lutana.geodataextractor.detector.gazetteer.LuceneAnalyzer;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Assert;

public class LuceneAnalyzerTest {

	@org.junit.Test
	public void testAnalyzer() {
		String text = "Frankreich,Münster,O'Neil's,St. Gallen, hamburg";
		String[] expected = new String[]{"frankreich", "münster", "o'neil", "st gallen", "hamburg"};

		LuceneAnalyzer analyzer = new LuceneAnalyzer();
		List<String> result = analyzer.parse(text);
		Assert.assertArrayEquals(expected, result.toArray());
	}

}
