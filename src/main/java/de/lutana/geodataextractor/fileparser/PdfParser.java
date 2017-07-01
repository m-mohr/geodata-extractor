package de.lutana.geodataextractor.fileparser;

import de.lutana.geodataextractor.Config;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.allenai.pdffigures2.FigureExtractor;
import org.allenai.pdffigures2.RasterizedFigure;
import org.allenai.pdffigures2.SectionedTextBuilder.PdfText;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.metadata.model.DocumentMetadata;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;
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
	 * @throws de.lutana.geodataextractor.fileparser.ParserException
	 * @see http://pdffigures2.allenai.org/
	 */
	@Override
	public void parse(Document document) throws ParserException {
		int dpi = 300;
		boolean allowOcr = true;
		boolean ignoreWhiteGraphics = true;
		boolean detectSectionTitlesFirst = true;
		boolean rebuildParagraphs = false;
		boolean cleanRasterizedFigureRegions = true;
		Option None = scala.Option.apply(null); // This is None, the scala way to use null

		// Get title and abstract using CERMINE
		try {
			ContentExtractor extractor = new ContentExtractor();
			extractor.setPDF(new FileInputStream(document.getFile()));
			DocumentMetadata meta = extractor.getMetadata();
			document.setTitle(meta.getTitle());
			document.setDescription(meta.getAbstrakt());
		} catch (IOException | AnalysisException | TimeoutException | AssertionError ex) {
			ex.printStackTrace();
		}

		PDDocument pdfBoxDoc = null;
		try {
			pdfBoxDoc = PDDocument.load(document.getFile());
			FigureExtractor figExtractor = new FigureExtractor(allowOcr, ignoreWhiteGraphics, detectSectionTitlesFirst, rebuildParagraphs, cleanRasterizedFigureRegions);
			FigureExtractor.DocumentWithRasterizedFigures figures = figExtractor.getRasterizedFiguresWithText(pdfBoxDoc, dpi, None, None);
			
			// Get alternative title
			if (document.getTitle().isEmpty()) {
				PDDocumentInformation meta = pdfBoxDoc.getDocumentInformation();
				if (meta != null) {
					document.setTitle(meta.getTitle());
				}
			}
			
			// Get alternative abstract
			if (document.getDescription().isEmpty()) {
				Option<PdfText> description = figures.abstractText();
				if (description != None) {
					document.setDescription(description.get().text());
				}
			}
			
			// Get figures
			Iterator<RasterizedFigure> it = figures.figures().iterator();
			while(it.hasNext()) {
				RasterizedFigure rfigure = it.next();
				Integer page = rfigure.figure().page();
				String figName = rfigure.figure().name();

				File tempFile = new File(Config.getTempFolder(document.getFile().getName()), "pg"+page+"-fig" + figName + ".png");
				ImageIO.write(rfigure.bufferedImage(), "png", tempFile);

				Figure figure = document.addFigure(tempFile, figName, page);
				figure.setCaption(rfigure.figure().caption());
				figure.setGraphic(tempFile);
			}
			pdfBoxDoc.close();
		} catch (IOException ex) {
			if (pdfBoxDoc != null) {
				try {
					pdfBoxDoc.close();
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
