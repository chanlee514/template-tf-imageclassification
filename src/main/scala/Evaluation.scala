package org.template.tensorflow

import org.apache.predictionio.controller.AverageMetric
import org.apache.predictionio.controller.Evaluation
import org.apache.predictionio.controller.EmptyEvaluationInfo
import org.apache.predictionio.controller.EngineParamsGenerator
import org.apache.predictionio.controller.EngineParams

/** Create an accuracy metric for evaluating our supervised learning model. */
case class Accuracy()
  extends AverageMetric[EmptyEvaluationInfo, Query, PredictedResult, ActualResult] {

  /** Method for calculating prediction accuracy. */
  def calculate(
    query: Query,
    predicted: PredictedResult,
    actual: ActualResult
  ) : Double = if (predicted.labelId == actual.labelId) 1.0 else 0.0
}


/** Define your evaluation object implementing the accuracy metric defined
  * above.
  */
object AccuracyEvaluation extends Evaluation {
  // Define Engine and Metric used in Evaluation.
  engineMetric = (
    TensorflowEngine(),
    new Accuracy
  )
}

/** Set your engine parameters for evaluation procedure.*/
object EngineParamsList extends EngineParamsGenerator {
  private[this] val baseEP = EngineParams(
    dataSourceParams = DataSourceParams(appName = "TFApp1", evalK = Some(3))
  )

  val baseAlgoParams = TFAlgorithmParams(
    inputLayer = "DecodeJpeg/contents:0",
    outputLayer = "softmax:0",
    modelFilename = "",
    imageDir = "data/images",
    idToStringIdMap = "data/imagenet_2012_challenge_label_map_proto.pbtxt",
    stringIdToLabelMap = "data/imagenet_synset_to_human_label_map.txt")

  // Set the algorithm params for which we will assess an accuracy score.
  engineParamsList = Seq(
    baseEP.copy(algorithmParamsList = Seq(("tf", baseAlgoParams.copy(
      modelFilename = "classify_image_graph_def.pb")))),
    baseEP.copy(algorithmParamsList = Seq(("tf", baseAlgoParams.copy(
      modelFilename = "classify_image_graph_def_2.pb")))))
}
