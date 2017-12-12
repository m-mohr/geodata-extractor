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
import java.util.Set;

/**
 * Command Line Interface for the Geodata Extractor.
 *
 * @author Matthias Mohr
 */
public class Cli {

	@Parameter(description = "A single publication file or a folder containing publications")
	public String file;

	@Parameter(names = "--strategy", description = "Strategy (fully qualified class name, Default or Fast)")
	public String strategy;

	@Parameter(names = "--save", description = "Save extracted figures and data to the document's folder")
	public boolean save = false;

	@Parameter(names = "--ocr", description = "Use slower but more accurate OCR mode")
	public boolean improvedOcr = false;

	@Parameter(names = "--figures", description = "Include bounding boxes for figures")
	public boolean figures = false;

    @Parameter(names = "--help", help = true)
    private boolean help = false;

	public static void main(String[] args) {
		Cli cli = new Cli();
		JCommander jcommander = JCommander.newBuilder().addObject(cli).build();
		jcommander.parse(args);
		
		File file = cli.getFile();
		Strategy strategy = cli.getStrategy();

        if (cli.help || file == null || strategy == null) {
            jcommander.usage();
            return;
        }
		
		GeodataExtractor gde = new GeodataExtractor();
		gde.enableFastOcrMode(!cli.improvedOcr);
		gde.enableCaching(cli.save);
		if (file.isDirectory()) {
			gde.setFolder(file);
		}
		else {
			Collection<File> files = new ArrayList<File>();
			files.add(file);
			gde.setFiles(files);
		}
		gde.setStrategy(strategy);
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

	public File getFile() {
		if (this.file == null || this.file.isEmpty()) {
			return new File("./test-docs/");
		}
		File file = new File(this.file);
		if (file.exists()) {
			return file;
		}
		return null;
	}

	public Strategy getStrategy() {
		if (this.strategy == null || this.strategy.isEmpty()) {
			this.strategy = "Default";
		}
		Class myClass;
		try {
			myClass = Class.forName(this.strategy);
		} catch (ClassNotFoundException e) {
			try {
				myClass = Class.forName("de.lutana.geodataextractor.strategy." + this.strategy + "Strategy");
			} catch (ClassNotFoundException e2) {
				return null;
			}
		}
		try {
			Class[] types = {};
			Constructor constructor = myClass.getConstructor(types);
			Object[] parameters = {};
			return (Strategy) constructor.newInstance(parameters);
		} catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
			return null;
		}
	}

}
