package org.template.tensorflow

import org.apache.predictionio.controller.P2LAlgorithm
import org.apache.predictionio.controller.Params

import org.apache.spark.SparkContext

import scala.io.Source
import grizzled.slf4j.Logger
import java.io.File
import java.nio.file.{Paths, Files}
import sun.misc.BASE64Decoder

case class TFAlgorithmParams(
  val inputLayer: String,
  val outputLayer: String,
  val modelFilename: String,
  val imageDir: String,
  val idToStringIdMap: String,
  val stringIdToLabelMap: String
) extends Params

class TFAlgorithm(
  val ap: TFAlgorithmParams
) extends P2LAlgorithm[PreparedData, TFModel, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]
  private lazy val base64Decoder = new BASE64Decoder

  def train(sc: SparkContext, pd: PreparedData): TFModel = {
    new TFModel(
      ap.inputLayer,
      ap.outputLayer,
      new File("data", ap.modelFilename))
  }

  def predict(model: TFModel, query: Query): PredictedResult = {
    val imageBytes = query.data match {
      case Some(data) => base64Decoder.decodeBuffer(data)
      case None => {
        Files.readAllBytes(Paths.get(
          new File(ap.imageDir, query.image.getOrElse("cropped_panda.jpg"))
            .getCanonicalPath))
      }
    }

    model.predict(imageBytes, ap.idToStringIdMap, ap.stringIdToLabelMap)
  }
}

class TFModel(
  val inputLayer: String,
  val outputLayer: String,
  val serializedModel: File
) extends Serializable {

  def predict(
    imageBytes: Array[Byte],
    idToStringIdMap: String,
    stringIdToLabelMap: String): PredictedResult = {

    var session = TensorFlowNative.native.tfCreateSession(
      serializedModel.getCanonicalPath)

    var scores = new Array[Float](2048) // placeholder

    var result = TensorFlow.using(serializedModel.getCanonicalPath) { tf =>
      tf.runBytes(inputLayer, outputLayer, imageBytes)
        .zipWithIndex
        .sortBy(-_._1)
        .take(1)
        .map { case (confidence, id) => PredictedResult(
          id,
          DataSourceUtils.categoryMap(
            idToStringIdMap,
            stringIdToLabelMap
          ).getOrElse(id, "Index not found"),
          confidence)}
    } 
    return result.head
  }
}

