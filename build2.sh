#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

OS=`uname`

echo -e "\033[1;32mCleaning up old resources... \033[0m"
LIB_DIR="${PWD}/src/main/resources"
rm -rf $LIB_DIR/*

LIB="${LIB_DIR}/linux-x86-64/libtensorflow.so"
if [[ "$OS" = "Darwin" ]]; then
  LIB="${LIB_DIR}/darwin/libtensorflow.dylib"
fi
mkdir -p `dirname $LIB`

echo -e "\033[1;32mBuilding Tensorflow using Bazel. This will take a while... \033[0m"
pushd $PWD/tensorflow
bazel build --config opt //tensorflow/jna:libtensorflow.so
popd
mv tensorflow/bazel-bin/tensorflow/jna/libtensorflow.so $LIB

echo -e "\033[1;32mAll Done! \033[0m"
