/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.template.tensorflow

import java.io.File
import java.nio.file.{Paths, Files}
import scala.collection.JavaConversions._
import scala.sys.process._


class TFModel {
  val inputLayer = "DecodeJpeg/contents:0"
  val outputLayer = "softmax:0"
  val model = "data/classify_image_graph_def.pb"
  val metaLabelMap = "data/imagenet_2012_challenge_label_map_proto.pbtxt"
  val labelMap = "data/imagenet_synset_to_human_label_map.txt"

  def maybeDownload() {
    if (! new File(model).exists) {
      val c = Process("./download.sh", new File("data"))
      c.!
    }
  }

  def indexToStringMap: Map[Int, String] = {
    val lm = Files.readAllLines(Paths.get(labelMap))
      .map(_.split("\\s+", 2))
      .map { case Array(s, l) => (s.trim, l.trim) }
      .toMap

    val result = Files.readAllLines(Paths.get(metaLabelMap))
      .dropWhile(_.trim.startsWith("#"))
      .grouped(4)
      .map { grouped =>
        val targetClass = grouped(1).split(":")(1).trim.toInt
        val targetClassString = grouped(2).split(":")(1).trim.stripPrefix("\"").stripSuffix("\"")
        (targetClass, lm(targetClassString))
      }
      .toMap
    return result
  }
}
