package de.lutana.geodataextractor.parser;

import de.lutana.geodataextractor.Config;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.FigureCollection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.allenai.pdffigures2.FigureExtractor;
import org.allenai.pdffigures2.RasterizedFigure;
import org.apache.pdfbox.pdmodel.PDDocument;
import scala.Option;
import scala.collection.Iterator;

/**
 * Parses figures from PDF files.
 *
 * @author Matthias Mohr
 */
public class PdfParser implements Parser {

	/**
	 * Parses contents from PDF documents.
	 *
	 * This parser uses the PDFFigures 2.0 package from Allenai. Some figures
	 * might not be detected correctly, which is limited by PDFFigures.
	 *
	 * @param document File referencing an PDF based document
	 * @return
	 * @throws de.lutana.geodataextractor.parser.ParserException
	 * @see http://pdffigures2.allenai.org/
	 */
	@Override
	public FigureCollection parse(File document) throws ParserException {
		int dpi = 300;
		boolean allowOcr = true;
		boolean ignoreWhiteGraphics = true;
		boolean detectSectionTitlesFirst = true;
		boolean rebuildParagraphs = false;
		boolean cleanRasterizedFigureRegions = true;
		Option None = scala.Option.apply(null); // This is None, the scala way to use null

		FigureCollection collection = new FigureCollection();
		PDDocument doc = null;
		try {
//			VisualLogger vLogger = new VisualLogger(true, true, true, true, true, detectSectionTitlesFirst, cleanRasterizedFigureRegions);
			doc = PDDocument.load(document);
			FigureExtractor extractor = new FigureExtractor(allowOcr, ignoreWhiteGraphics, detectSectionTitlesFirst, rebuildParagraphs, cleanRasterizedFigureRegions);
			FigureExtractor.DocumentWithRasterizedFigures figures = extractor.getRasterizedFiguresWithText(doc, dpi, None, None);
			Iterator<RasterizedFigure> it = figures.figures().iterator();
			while(it.hasNext()) {
				RasterizedFigure rfigure = it.next();

				BufferedImage imgBuffer = rfigure.bufferedImage();
				File tempFile = File.createTempFile("fig", ".png", Config.getTempFolder());
				ImageIO.write(imgBuffer, "png", tempFile);

				String context = "Page " + rfigure.figure().page() + "; Figure " + rfigure.figure().name();
				Figure figure = new Figure(document, context);
				figure.setCaption(rfigure.figure().caption());
				figure.setGraphic(tempFile);
				collection.add(figure);
			}
//			vLogger.displayVisualLog(doc, dpi);
			doc.close();
			return collection;
		} catch (IOException ex) {
			if (doc != null) {
				try {
					doc.close();
				} catch(IOException e) {}
			}
			throw new ParserException(ex);
		}
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
		return new String[]{"pdf"};
	}

}
