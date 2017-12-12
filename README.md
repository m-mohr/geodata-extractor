# geodata-extractor

Source code for the thesis "Machine-based extraction of location information from digital publications".

## Running the program

Due to the enormous size of the application (1 GB) we currently can't upload it here. Please build it yourself, see the Development section below.

Once you built the JAR files run the program using the command line interface. You can get information about the parameters:
```
java -jar geodata-extractor.jar --help
```

This will run the DefaultStrategy using all files in the test-docs folder:
```
java -jar geodata-extractor.jar --strategy=Default ./test-docs/
```

This will run the strategy located at com.company.package.YourCustomStrategy on the sample.pdf file:
```
java -jar geodata-extractor.jar --strategy=com.company.package.YourCustomStrategy sample.pdf
```

### System requirements

* ~10 GB of storage space
* 4 GB memory
* Java JRE 8 or higher (or the corresponding JDK)
* On Windows: [Microsoft Visual C++ Redistributable für Visual Studio 2015](https://www.microsoft.com/de-de/download/details.aspx?id=48145)

## Development

You would like to build the program yourself? Please follow the following instructions:
1. Install [Java JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (or newer)
2. Install [Netbeans IDE](https://netbeans.org/features/index.html) (or any other Java IDE supporting Maven development)
3. On Windows: Install [Microsoft Visual C++ Redistributable für Visual Studio 2015](https://www.microsoft.com/de-de/download/details.aspx?id=48145) in order to run [Tesseract OCR 3](https://github.com/tesseract-ocr/tesseract/wiki/Downloads)
4. Load the Maven/Netbeans project into the IDE.

Note: The (first) build process will may take several hours, depending on your internet connection. This is due to the fact that during the build process the OSMNames database (~1,4 GB) and all required dependencies will be downloaded. During first execution a Lucene Index will be created using the OSMNames data set (~8 GB storage required). Also the tests might take londer, depending on the amount of publications stored in the "test-docs" directory.

### Generate tensorflow training data for (world) map detection

1. Install Docker
2. Get docker image by running `docker run -it gcr.io/tensorflow/tensorflow:latest-devel` and stop docker afterwards
3. Create a directory (outside of docker) called `tf_files` containing a directory `data` which again contains two directories: `map` and `nomap` (or `world` and `other` for world map detection)
4. Paste all map images into `map` (world maps into `world`) and all non-map images into `nomap` (non-world-maps into `other`). Note: jpeg files only. I resized all files having the largest side a maximum of 500px.
5. Link the new folders into your docker machine: `docker run -it -v /path/to/your/tf_files/:/tf_files/ gcr.io/tensorflow/tensorflow:latest-devel`
6. Get the training script by executing the following commands inside your docker machine: `cd /tensorflow` and `git pull`
7. Run the training process in your docker machine:
  * Map detection: `python tensorflow/examples/image_retraining/retrain.py --bottleneck_dir=/tf_files/bottlenecks --model_dir=/tf_files/inception --output_graph=/tf_files/map_graph.pb --output_labels=/tf_files/map_labels.txt --image_dir /tf_files/data`
  * World map detection: `python tensorflow/examples/image_retraining/retrain.py --bottleneck_dir=/tf_files/bottlenecks --model_dir=/tf_files/inception --output_graph=/tf_files/worldmap_graph.pb --output_labels=/tf_files/worldmap_labels.txt --image_dir /tf_files/data`
8. Copy the generated files `map_graph.pb` and `map_labels.txt` (or  `worldmap_graph.pb` and `worldmap_labels.txt`) from the `tf_files` directory to the folder `src/main/resources/tensorflow` inside this repository and rebuild this project.
