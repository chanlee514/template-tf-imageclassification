package org.template.tensorflow

import org.apache.predictionio.controller.P2LAlgorithm
import org.apache.predictionio.controller.Params

import org.apache.spark.SparkContext

import scala.io.Source
import java.io.File
import java.nio.file.{Paths, Files}
import grizzled.slf4j.Logger

case class TFAlgorithmParams(
  val inputLayer: String,
  val outputLayer: String,
  val modelFilename: String
) extends Params

class TFAlgorithm(
  val ap: TFAlgorithmParams
) extends P2LAlgorithm[PreparedData, TFModel, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(sc: SparkContext, pd: PreparedData): TFModel = {
    new TFModel(
      inputLayer = ap.inputLayer,
      outputLayer = ap.outputLayer,
      serializedModel = new File("data", ap.modelFilename))
  }

  def predict(model: TFModel, query: Query): PredictedResult = {
    model.predict(
      new File("data/images", query.image.getOrElse("cropped_panda.jpg")).getCanonicalPath)
  }
}

class TFModel(
  val inputLayer: String,
  val outputLayer: String,
  val serializedModel: File
) extends Serializable {

  def predict(imageFilePath: String): PredictedResult = {
    val inputBytes = Files.readAllBytes(Paths.get(imageFilePath))

    var session = TensorFlowNative.tensorFlowNative.tfCreateSession(
      serializedModel.getCanonicalPath)
    var scores = new Array[Float](2048) // placeholder

    def idToCategories(categoryMap: String, labelMap: String): Map[Int, String] = {
      // ex: n02510455 => ["giant panda", "panda", "panda bear", ...]
      val lm = Source.fromFile("data/imagenet_synset_to_human_label_map.txt").getLines.toList
        .map(_.split("\\s+", 2))
        .map { case Array(s, l) => (s.trim, l.trim) }
        .toMap
      // ex: 169 => ["giant panda", "panda", "panda bear", ...]
      Source.fromFile("data/imagenet_2012_challenge_label_map_proto.pbtxt").getLines.toList
        .dropWhile(_.trim.startsWith("#"))
        .grouped(4)
        .map { grouped =>
          val cl = grouped(1).split(":")(1).trim.toInt
          val cls = grouped(2).split(":")(1).trim.stripPrefix("\"").stripSuffix("\"")
          (cl, lm(cls))
        }
        .toMap
    }
 
    var result = TensorFlow.using(serializedModel.getCanonicalPath) { tf =>
      tf.run(inputLayer, outputLayer, inputBytes)
        .zipWithIndex
        .sortBy(-_._1)
        .take(1)
        .map { case (confidence, id) => PredictedResult(
          id,
          idToCategories(
            "data/imagenet_2012_challenge_label_map_proto.pbtxt",
            "data/imagenet_synset_to_human_label_map.txt"
          ).getOrElse(id, "Index not found"),
          confidence)}
    }

    // TensorFlowNative.tensorFlowNative.tfRunString(
    //   session,
    //   inputLayer,
    //   outputLayer,
    //   inputBytes,
    //   inputBytes.length,
    //   scores)


    // TensorFlowNative.tensorFlowNative.tfCloseSession(session)
    return result.head
  }
}

