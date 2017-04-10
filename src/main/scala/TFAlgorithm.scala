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
  val serializedModelFilename: String,
  val metaLabelMapFilename: String,
  val labelMapFilename: String
) extends Params

class TFAlgorithm(
  val ap: TFAlgorithmParams
) extends P2LAlgorithm[PreparedData, TFModel, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(sc: SparkContext, pd: PreparedData): TFModel = {
    new TFModel(
      inputLayer = ap.inputLayer,
      outputLayer = ap.outputLayer,
      serializedModel = new File("data", ap.serializedModelFilename),
      metaLabelMap = new File("data", ap.metaLabelMapFilename),
      labelMap = new File("data", ap.labelMapFilename))
  }

  def predict(model: TFModel, query: Query): PredictedResult = {
    model.predict(query.imageFilePath)
  }
}

class TFModel(
  val inputLayer: String,
  val outputLayer: String,
  val serializedModel: File,
  val metaLabelMap: File,
  val labelMap: File
) extends Serializable {

  def predict(imageFilePath: String): PredictedResult = {
    val inputBytes = Files.readAllBytes(Paths.get(imageFilePath))

    // Helper method
    def indexToStringMap: Map[Int, String] = {
      val lm = Source.fromFile(labelMap.getName).getLines.toList
        .map(_.split("\\s+", 2))
        .map { case Array(s, l) => (s.trim, l.trim) }
        .toMap

      Source.fromFile(metaLabelMap.getName).getLines.toList
        .dropWhile(_.trim.startsWith("#"))
        .grouped(4)
        .map { grouped =>
          val targetClass = grouped(1).split(":")(1).trim.toInt
          val targetClassString = grouped(2).split(":")(1).trim.stripPrefix("\"").stripSuffix("\"")
          (targetClass, lm(targetClassString))
        }
        .toMap
    }

    var session = TensorFlowNative.tensorFlowNative.tfCreateSession(serializedModel.getName)
    var scores = new Array[Float](2048) // placeholder
    TensorFlowNative.tensorFlowNative.tfRunString(
      session,
      inputLayer,
      outputLayer,
      inputBytes,
      inputBytes.length,
      scores)

    val result = scores.zipWithIndex.sortBy(-_._1).take(5).map { case (s, i) =>
      PredictedResult(
        indexToStringMap.getOrElse(i, i.toString).split(", ").head,
        s)}

    TensorFlowNative.tensorFlowNative.tfCloseSession(session)
    return result.head
  }
}

