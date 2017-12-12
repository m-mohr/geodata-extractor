package de.lutana.geodataextractor;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.JCommander;
import de.lutana.geodataextractor.entity.Document;
import de.lutana.geodataextractor.entity.Figure;
import de.lutana.geodataextractor.entity.Location;
import de.lutana.geodataextractor.strategy.Strategy;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Command Line Interface for the Geodata Extractor.
 *
 * @author Matthias Mohr
 */
public class Cli {

	@Parameter(description = "A single publication file or a folder containing publications", converter = FileConverter.class)
	public File file;

	@Parameter(names = "-strategy", description = "Strategy", converter = StrategyConverter.class)
	public Strategy strategy;

	@Parameter(names = "-save", description = "Save extracted figures and data to the document's folder")
	public boolean save = false;

	@Parameter(names = "-ocr", description = "Use slower but more accurate OCR mode")
	public boolean improvedOcr = false;

	@Parameter(names = "-figures", description = "Include bounding boxes for figures")
	public boolean figures = false;

    @Parameter(names = "--help", help = true)
    private boolean help = false;

	public static void main(String[] args) {
		Cli cli = new Cli();
		JCommander jcommander = JCommander.newBuilder().addObject(cli).build();
		jcommander.parse(args);

        if (cli.help || cli.file == null || cli.strategy == null) {
            jcommander.usage();
            return;
        }
		
		GeodataExtractor gde = new GeodataExtractor();
		gde.enableFastOcrMode(!cli.improvedOcr);
		gde.enableCaching(cli.save);
		if (cli.file.isDirectory()) {
			gde.setFolder(cli.file);
		}
		else {
			Collection<File> files = new ArrayList<File>();
			files.add(cli.file);
			gde.setFiles(files);
		}
		gde.setStrategy(cli.strategy);
		Set<Document> documents = gde.run();
		for(Document document : documents) {
			Location dl = document.getLocation();
			System.out.println(document.getFile().getAbsolutePath() + ":" + dl);
			if (cli.figures) {
				for(Figure figure : document.getFigures()) {
					System.out.println(figure.getPage() + ":" + figure.getIndex() + ":" + figure.getLocation());
				}
			}
			System.out.println();
		}
		gde.shutdown();
	}

	class FileConverter implements IStringConverter<File> {

		@Override
		public File convert(String value) {
			if (value != null && !value.isEmpty()) {
				File file = new File(value);
				if (file.exists()) {
					return file;
				}
			}
			return null;
		}
	}

	class StrategyConverter implements IStringConverter<Strategy> {

		@Override
		public Strategy convert(String value) {
			if (value == null || value.isEmpty()) {
				value = "Default";
			}
			Class myClass;
			try {
				myClass = Class.forName(value);
			} catch (ClassNotFoundException e) {
				try {
					myClass = Class.forName("de.lutana.geodataextractor.strategy." + value + "Strategy");
				} catch (ClassNotFoundException e2) {
					return null;
				}
			}
			try {
				Class[] types = {this.getClass()};
				Constructor constructor = myClass.getConstructor(types);
				Object[] parameters = {this};
				return (Strategy) constructor.newInstance(parameters);
			} catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
				return null;
			}
		}
	}

}
