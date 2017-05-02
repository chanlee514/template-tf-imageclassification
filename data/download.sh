#!/usr/bin/env bash

echo -e "\033[1;32mDownloading and extracting pre-trained model... \033[0m"
DOWNLOAD_URL='http://download.tensorflow.org/models/image/imagenet/inception-2015-12-05.tgz'
curl -L $DOWNLOAD_URL > imagenet_model.tgz
tar zxf imagenet_model.tgz
rm -f imagenet_model.tgz

DATA_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
mkdir -p $DATA_DIR/images
mv cropped_panda.jpg $DATA_DIR/images 2>/dev/null

echo -e "\033[1;32mAll Done!\033[0m"
