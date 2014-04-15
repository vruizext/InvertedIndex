#!/bin/bash

TARGET_DIR=${TARGET_DIR:-./target}
java -cp "${TARGET_DIR}"/InvertedIndex-*.jar -Xmx2G -Xms1G com.vruiz.invertedindex.Indexer $*
