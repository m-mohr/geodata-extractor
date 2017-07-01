package de.lutana.geodataextractor.fileparser;

import de.lutana.geodataextractor.Config;
import de.lutana.geodataextractor.entity.Document;
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
	 * @throws de.lutana.geodataextractor.fileparser.ParserException
	 * @see http://wiki.selfhtml.org/wiki/HTML/Textstrukturierung/figure
	 * @see http://wiki.selfhtml.org/wiki/HTML/Textstrukturierung/img
	 */
	@Override
	public void parse(Document document) throws ParserException {
		try {
			File docFile = document.getFile();
			org.jsoup.nodes.Document doc = Jsoup.parse(docFile, "UTF-8");

			// Get title of document, description is not supported.
			Elements titles = doc.getElementsByTag("title");
			for (Element el : titles) {
				if (el.hasText()) {
					String title = el.text();
					document.setTitle(title);
					break;
				}
			}
			// Try to get images from img tags
			Elements img = doc.getElementsByTag("img");
			Integer i = 1;
			for (Element el : img) {
				File gfxFile = this.downloadImage(el, "src", docFile.getParentFile());
				if (gfxFile == null) {
					continue;
				}
				Figure figure = document.addFigure(gfxFile, i.toString());
				String altTag = el.attr("alt");
				String titleTag = el.attr("title");
				if(titleTag != null && !titleTag.isEmpty()) {
					figure.setCaption(titleTag);
				}
				else if(altTag != null && !altTag.isEmpty()) {
					figure.setCaption(altTag);
				}
				i++;
			}
			// ToDo: Add figure/figcaption support, but make sure it doesn't
			// conflict with the img parsing above (double elements, ...)

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
	 * Returns the gfxFile for the downloaded gfxFile.
 Doesn't support extracting inline images using the data URI (data:image/png;base64,...).
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
	 * Returns a list of gfxFile extensions that can be parsed by this parser.
	 * 
	 * Specify each extension without a leading dot.
	 * 
	 * @return List of valid gfxFile extensions
	 */
	@Override
	public String[] getExtensions() {
		return new String[] {"htm", "html"};
	}
	
}
