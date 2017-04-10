package org.template.tensorflow

import org.apache.predictionio.controller.PPreparator
import org.apache.spark.SparkContext // namespace

/** define your Preparator class */
class Preparator()
  extends PPreparator[TrainingData, PreparedData] {

  def prepare(sc: SparkContext, td: TrainingData): PreparedData = {
    new PreparedData(
      modelPath = "data/classify_image_graph_def.pb",
      metaLabelMapPath = "data/imagenet_2012_challenge_label_map_proto.pbtxt",
      labelMapPath = "data/imagenet_synset_to_human_label_map.txt"
    )
  }
}

class PreparedData(
  val modelPath: String,
  val metaLabelMapPath: String,
  val labelMapPath: String
) extends Serializable
