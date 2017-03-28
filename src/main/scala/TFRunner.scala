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

import grizzled.slf4j.Logger
import java.nio.file.{Paths, Files}
import scala.collection.JavaConversions._

object TFRunner {

  def main(args: Array[String]) {
    val inputPicFile = args(0)
    val inputBytes = Files.readAllBytes(Paths.get(inputPicFile))

    val model = new TFModel
    model.maybeDownload

    TensorFlow.using(model.model) { tf =>
      tf.run(model.inputLayer, model.outputLayer, inputBytes)
        .zipWithIndex
        .sortBy(-_._1)
        .take(10)
        .foreach { case (score, i) =>
          val prediction = model.indexToStringMap.getOrElse(i, s"$i")
          println(s"\n  '${prediction}' (score = ${score}%.5f)")
        }
    }
  }
}
