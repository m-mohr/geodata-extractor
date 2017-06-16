package de.lutana.geodataextractor.parser;

import de.lutana.geodataextractor.Config;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import de.lutana.geodataextractor.util.FileExtension;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Parses figures from HTML documents.
 * 
 * @author Matthias Mohr
 */
public class HtmlParser implements Parser {

	/**
	 * Parses contents from img tags in HTML documents.
	 * 
	 * Other tags and (X)HTML blocks are currently not supported.
	 * 
	 * @param document File referencing an HTML based document
	 * @return
	 * @throws de.lutana.geodataextractor.parser.ParserException
	 * @see http://wiki.selfhtml.org/wiki/HTML/Textstrukturierung/figure
	 * @see http://wiki.selfhtml.org/wiki/HTML/Textstrukturierung/img
	 */
	@Override
	public FigureCollection parse(File document) throws ParserException {
		FigureCollection collection = new FigureCollection();
		try {
			Document doc = Jsoup.parse(document, "UTF-8");
			// Try to get img tags
			Elements img = doc.getElementsByTag("img");
			for (Element el : img) {
				Figure figure = new Figure(document);
				File file = this.downloadImage(el, "src", document.getParentFile());
				if (file != null) {
					figure.setGraphic(file);
				}
				String alt = el.attr("alt");
				String title = el.attr("title");
				if(title != null && !title.isEmpty()) {
					figure.setCaption(title);
				}
				else if(alt != null && !alt.isEmpty()) {
					figure.setCaption(alt);
				}
				collection.add(figure);
			}
			// ToDo: Add figure/figcaption support, but make sure it doesn't
			// conflict with the img parsing above (double elements, ...)
			return collection;

		} catch (IOException ex) {
			throw new ParserException(ex);
		}
	}
	
	protected File downloadImage(Element el, String attr, File folder) throws IOException {
		if (!el.hasAttr(attr)) {
			return null;
		}
		
		String absUrl = el.absUrl(attr);
		if (!absUrl.isEmpty()) {
			return this.downloadImageFromURL(absUrl);
		}
		
		String relUrl = el.attr(attr);
		if (!relUrl.isEmpty()) {
			File path = new File(folder.getAbsolutePath() + File.separator + relUrl);
			return path.getCanonicalFile();
		}
		
		return null;
	}
	
	/**
	 * Downloads an image from the web and stores it in a temporary folder.
	 * 
	 * Returns the file for the downloaded file.
	 * Doesn't support extracting inline images using the data URI (data:image/png;base64,...).
	 * 
	 * @param srcUrl
	 * @return
	 * @throws IOException 
	 */
	protected File downloadImageFromURL(String srcUrl) throws IOException {
        URL url = new URL(srcUrl);
		String extension = FileExtension.get(url.getFile());
		File tempFile = File.createTempFile("fig", "." + extension, Config.getTempFolder(url.getFile()));

		OutputStream out = null;
		InputStream in = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(tempFile));
			in = url.openStream();
			for (int b; (b = in.read()) != -1;) {
				out.write(b);
			}
			out.close();
			in.close();
		} catch (IOException e) {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				in.close();
			}
			tempFile.delete();
			tempFile = null;
			throw e;
		}
		return tempFile;
	}

	/**
	 * Returns a list of file extensions that can be parsed by this parser.
	 * 
	 * Specify each extension without a leading dot.
	 * 
	 * @return List of valid file extensions
	 */
	@Override
	public String[] getExtensions() {
		return new String[] {"htm", "html"};
	}
	
}
