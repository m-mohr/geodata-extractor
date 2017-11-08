package de.lutana.geodataextractor;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.JCommander;
import de.lutana.geodataextractor.strategy.Strategy;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Command Line Interface for the Geodata Extractor.
 *
 * @author Matthias Mohr
 */
public class Cli {

	@Parameter(names = "-strategy", description = "Strategy", converter = StrategyConverter.class)
	public Strategy strategy;

	@Parameter(names = "-folder", description = "Folder containing publications", converter = FolderConverter.class)
	public File folder;

	@Parameter(names = "-save", description = "Save extracted figures and data to the document's folder")
	public boolean save = false;

	@Parameter(names = "-fastOCR", description = "Use faster but more inaccurate OCR mode")
	public boolean fastOcr = false;

	/*	@Parameter(names = "-debug", description = "Debug mode")
	private boolean debug = false; */
	public static void main(String[] args) {
		Cli cli = new Cli();
		JCommander.newBuilder().addObject(cli).build().parse(args);

		GeodataExtractor gde = new GeodataExtractor();
		if (cli.folder != null) {
			gde.setFolder(cli.folder);
		} else {
			throw new IllegalArgumentException("Invalid folder specified");
		}
		if (cli.strategy != null) {
			gde.setStrategy(cli.strategy);
		}
		if (cli.save) {
			gde.enableCaching(cli.save);
		}
		if (cli.fastOcr) {
			gde.enableFastOcrMode(true);
		}
		gde.run();
		gde.shutdown();
	}

	class FolderConverter implements IStringConverter<File> {

		@Override
		public File convert(String value) {
			File file = new File(value);
			if (file.isDirectory()) {
				return file;
			}
			return null;
		}
	}

	class StrategyConverter implements IStringConverter<Strategy> {

		@Override
		public Strategy convert(String value) {
			if (value == null || value.length() == 0) {
				return null;
			}
			try {
				Class myClass = Class.forName(value);
				Class[] types = {this.getClass()};
				Constructor constructor = myClass.getConstructor(types);
				Object[] parameters = {this};
				return (Strategy) constructor.newInstance(parameters);
			} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
				return null;
			}
		}
	}

}
