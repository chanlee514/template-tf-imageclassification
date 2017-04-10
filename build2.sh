#!/usr/bin/env bash

OS=`uname`

echo -e "\033[1;32mCleaning up old resources... \033[0m"
LIB_DIR="${PWD}/src/main/resources"
rm -rf $LIB_DIR/*

LIB="${LIB_DIR}/linux-x86-64/libtensorflow.so"
if [[ "$OS" = "Darwin" ]]; then
  LIB="${LIB_DIR}/darwin/libtensorflow.dylib"
fi
mkdir -p `dirname $LIB`

echo -e "\033[1;32mCopying resources for build... \033[0m"
mkdir -p tensorflow/tensorflow/jna
cp src/main/cpp/* tensorflow/tensorflow/jna

echo -e "\033[1;32mBuilding Tensorflow using Bazel. This will take a while... \033[0m"
pushd $PWD/tensorflow
bazel build --config opt //tensorflow/jna:libtensorflow.so
popd
mv tensorflow/bazel-bin/tensorflow/jna/libtensorflow.so $LIB

echo -e "\033[1;32mAll Done! \033[0m"
