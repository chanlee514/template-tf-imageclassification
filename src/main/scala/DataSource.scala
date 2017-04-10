package org.template.tensorflow

import org.apache.predictionio.controller.PDataSource
import org.apache.predictionio.controller.EmptyEvaluationInfo
import org.apache.predictionio.controller.EmptyActualResult
import org.apache.predictionio.controller.Params
import org.apache.predictionio.controller.SanityCheck
import org.apache.predictionio.data.storage.Event
import org.apache.predictionio.data.store.PEventStore

import org.apache.spark.SparkContext // namespace

import grizzled.slf4j.Logger

import java.nio.file.{Paths, Files}
import scala.collection.JavaConversions._

class TrainingData extends Serializable {}

case class DataSourceParams(
  appName: String,
  evalK: Option[Int]
) extends Params

class DataSource (
  val dsp : DataSourceParams
) extends PDataSource[TrainingData, EmptyEvaluationInfo, Query, ActualResult] {

  override
  def readTraining(sc: SparkContext): TrainingData = {
    new TrainingData
  }
}

/** Define Data Source parameters.
  * appName is the application name.
  * evalK is the the number of folds that are to be used for cross validation (optional)
  */
// case class DataSourceParams(
//   appId: Int
// ) extends Params


/** Define your DataSource component. Remember, you must
  * implement a readTraining method, and, optionally, a
  * readEval method.
  */
// class DataSource (
//   val dsp : DataSourceParams
// ) extends PDataSource[TrainingData, EmptyEvaluationInfo, Query, ActualResult] {

  // @transient lazy val logger = Logger[this.type]

  // /** Helper function used to store data given a SparkContext. */
  // private def readEventData(sc: SparkContext) : Observation = {
  //   // Get RDD of Events.
  //   PEventStore.find(
  //     appName = dsp.appName,
  //     entityType = Some("image") // specify data entity type

  //     // Convert collected RDD of events to and RDD of Observation
  //     // objects.
  //   )(sc).map(e => {
  //     val label : String = e.properties.get[String]("label")
  //     Observation(
  //       // e.properties.get[String]("text"),
  //       label
  //     )
  //   }).cache
  // }

  // /** Read in data and stop words from event server
  //   * and store them in a TrainingData instance.
  //   */
  // override
  // def readTraining(sc: SparkContext): TrainingData = {
  //   new TrainingData(readEventData(sc))
  // }

  
// }

/** Observation class serving as a wrapper for both our
  * data's class label and document string.
  */
// case class Observation(
//   label: String
// ) extends Serializable

/** TrainingData class serving as a wrapper for all
  * read in from the Event Server.
  */

