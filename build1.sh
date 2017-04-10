#!/usr/bin/env bash

TF_VERSION="1.0"

echo -e "\033[1;32mConfiguring Tensorflow as git submodule... \033[0m"
mkdir -p tensorflow
echo "%s/1.0/${TF_VERSION}/g
w
q
" | ex .gitmodules
git submodule update --remote
