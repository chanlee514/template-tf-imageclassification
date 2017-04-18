package org.template.tensorflow

import org.apache.predictionio.controller.PDataSource
import org.apache.predictionio.controller.EmptyEvaluationInfo
import org.apache.predictionio.controller.EmptyActualResult
import org.apache.predictionio.controller.Params
import org.apache.predictionio.controller.SanityCheck
import org.apache.predictionio.data.storage.Event
import org.apache.predictionio.data.store.PEventStore

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import grizzled.slf4j.Logger

import java.nio.file.{Paths, Files}
import scala.collection.JavaConversions._
import scala.io.Source

case class DataSourceParams(
  appName: String,
  labelMapFile: String,
  categoryMapFile: String,
  evalK: Option[Int]
) extends Params

class DataSource (
  val dsp : DataSourceParams
) extends PDataSource[TrainingData, EmptyEvaluationInfo, Query, ActualResult] {

  @transient lazy val logger = Logger[this.type]

  def idToCategories: Map[Int, Array[String]] = {
    // ex: n02510455 => ["giant panda", "panda", "panda bear", ...]
    val lm = Source.fromFile(dsp.categoryMapFile).getLines.toList
      .map(_.split("\\s+", 2))
      .map { case Array(s, l) => (s.trim, l.trim.split(", ")) }
      .toMap
    // ex: 169 => ["giant panda", "panda", "panda bear", ...]
    Source.fromFile(dsp.labelMapFile).getLines.toList
      .dropWhile(_.trim.startsWith("#"))
      .grouped(4)
      .map { grouped =>
        val cl = grouped(1).split(":")(1).trim.toInt
        val cls = grouped(2).split(":")(1).trim.stripPrefix("\"").stripSuffix("\"")
        (cl, lm(cls))
      }
      .toMap
  }

  /** Helper function used to store data given a SparkContext. */
  private def readEventData(sc: SparkContext) : RDD[Observation] = {
    //Get RDD of Events.
    PEventStore.find(
      appName = dsp.appName,
      entityType = Some("image"), // specify data entity type
      eventNames = Some(List("image")) // specify data event name

      // Convert collected RDD of events to and RDD of Observation
      // objects.
    )(sc).map(e => {
      val cl : Int = e.properties.get[Int]("labelId")
      Observation(
        e.properties.get[String]("filename"),
        cl,
        idToCategories(cl).mkString(",")
      )
    }).cache
  }


  /** Read in data and stop words from event server
    * and store them in a TrainingData instance.
    */
  override
  def readTraining(sc: SparkContext): TrainingData = {
    new TrainingData(readEventData(sc))
  }

  // /** Used for evaluation: reads in event data and creates cross-validation folds. */
  // override
  // def readEval(sc: SparkContext):
  // Seq[(TrainingData, EmptyEvaluationInfo, RDD[(Query, ActualResult)])] = {
  //   // Zip your RDD of events read from the server with indices
  //   // for the purposes of creating our folds.
  //   val data = readEventData(sc).zipWithIndex()
  //   // Create cross validation folds by partitioning indices
  //   // based on their index value modulo the number of folds.
  //   (0 until dsp.evalK.get).map { k =>
  //     // Prepare training data for fold.
  //     val train = new TrainingData(
  //       data.filter(_._2 % dsp.evalK.get != k).map(_._1),
  //       readStopWords
  //         ((sc)))

  //     // Prepare test data for fold.
  //     val test = data.filter(_._2 % dsp.evalK.get == k)
  //       .map(_._1)
  //       .map(e => (new Query(e.text), new ActualResult(e.category)))

  //     (train, new EmptyEvaluationInfo, test)
  //   }
  // }

}

/** Observation class serving as a wrapper for both our
  * data's class label and document string.
  */
case class Observation(
  filename: String,
  labelId: Integer,
  categories: String
) extends Serializable

class TrainingData(
  val data : RDD[Observation]
) extends Serializable with SanityCheck {

  /** Sanity check to make sure your data is being fed in correctly. */
  def sanityCheck(): Unit = {
    try {
      println()
    } catch {
      case (e : ArrayIndexOutOfBoundsException) => {
        println()
        println("Data set is empty, make sure event fields match imported data.")
        println()
      }
    }

  }

}


