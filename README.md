# geodata-extractor
Machine-based extraction of location information from digital publications

## Generate tensorflow training data for map detection

1. Install Docker
2. Get docker image by running `docker run -it gcr.io/tensorflow/tensorflow:latest-devel` and stop docker afterwards
3. Create a directory (outside of docker) called `tf_files` containing a directory `data` which again contains two directories: `map` and `nomap`
4. Paste all map images into `map` and all non-map images into `nomap` (jpeg files only, we resized them to have the largest side a maximum of 500px).
5. Link the new folders into your docker machine: `docker run -it -v c:/Users/m_mohr08/Desktop/tf/tf_files/:/tf_files/ gcr.io/tensorflow/tensorflow:latest-devel`
6. Get the training script by executing the following commands inside your docker machine: `cd /tensorflow` and `git pull`
7. Run the training process in your docker machine: `python tensorflow/examples/image_retraining/retrain.py --bottleneck_dir=/tf_files/bottlenecks --how_many_training_steps 1000 --model_dir=/tf_files/inception --output_graph=/tf_files/map_graph.pb --output_labels=/tf_files/map_labels.txt --image_dir /tf_files/data`
8. Copy the generated files `map_graph.pb` and `map_labels.txt` from the `tf_files` directory to the folder `src/main/resources/tensorflow` inside this repository and rebuild this project.