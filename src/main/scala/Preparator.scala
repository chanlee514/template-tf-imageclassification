package org.template.tensorflow

import org.apache.predictionio.controller.PPreparator
import org.apache.spark.SparkContext // namespace

/** define your Preparator class */
class Preparator()
  extends PPreparator[TrainingData, PreparedData] {

  def prepare(sc: SparkContext, td: TrainingData): PreparedData = {
    new PreparedData()
  }
}

class PreparedData(
) extends Serializable
