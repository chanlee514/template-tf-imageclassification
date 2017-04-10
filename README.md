## Description

Run image classification engine on [Apache PredictionIO](http://predictionio.incubator.apache.org) using a [Tensorflow](https://www.tensorflow.org) model. 

This template

* follows the workflow in official [Tensorflow tutorial for image classification](https://github.com/tensorflow/models/blob/master/tutorials/image/imagenet/classify_image.py).
* **uses a pre-trained model** from the inception challenge. The purpose of the engine is to be able to deploy a Tensorflow model and do inference via HTTP.
* for experimental purposes, builds Tensorflow from source using `bazel build`, instead of calling the Tensorflow Java API. For more details, see `src/main/cpp/jna.cc` and **Future explorations** section below.

## Installation
### Prerequisites
* [PredictionIO](http://predictionio.incubator.apache.org)
* [Bazel](https://bazel.build)
* [Swig](http://www.swig.org)

### Instructions
`./build1.sh`

`tensorflow/configure`

`./build2.sh`

## Workflow
### Downloading the model
Run `data/download.sh` to download the pre-trained imagenet model.

### Configuring PredictionIO
In `conf/pio-env.sh`, 

* `PIO_STORAGE_REPOSITORIES_MODELDATA_SOURCE=LOCALFS`
* `PIO_STORAGE_SOURCES_LOCALFS_TYPE=localfs`
* Set `PIO_STORAGE_SOURCES_LOCALFS_PATH` to the directory where you want to store the model metadata information. By default, this is `$HOME/.pio_store/models`.

### Deploying the engine
* Create a new PredictionIO app, and modify `engine.json` accordingly.
* After installing PredictionIO, run `pio build` in the engine directory. This produces fat jars in `target/scala-2.x/`.
* Run `pio train`. While we're using a pre-trained model, this command writes the new engine metadata to the PredictionIO metadata storage, and is required for deploy.
* Run `pio deploy`. By default, this deploys the engine using port 8000.

### Testing the engine
There are two ways of serving data to the engine.

1. Put the target image in `data/images`. You can change this path in `engine.json`. Then use `image` param as the filename of the target image such as `curl -H "Content-Type: application/json" -d '{ "image":"cropped_panda.jpg" }' http://localhost:8000/queries.json`.

2. Use `data` param to send a UTF-8 encoded string of the target image data.

If all goes well, the engine will return a JSON result such as `{"labelId":169,"categories":"giant panda, panda, panda bear, coon bear, Ailuropoda melanoleuca","confidence":0.8910738229751587}`

## Future explorations
### Enabling native training
In the Tensorflow community, there is active [work in progress](https://github.com/tensorflow/tensorflow/issues/6268) to support gradient operations via the Tensorflow C API. Using the C API, we can include such operations in our native java binding that we create. This will open possibilities to train a deep learning model natively, without using python scripts to create a protobuf file, or converting data between numpy arrays and java arrays which causes overhead.

