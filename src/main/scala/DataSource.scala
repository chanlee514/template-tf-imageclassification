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

object DataSourceUtils {
  def categoryMap(
    idToStringIdMap: String,
    stringIdToLabelMap: String): Map[Int, String] = {

    // ex: n02510455 => ["giant panda", "panda", "panda bear", ...]
    val lm = Source.fromFile(stringIdToLabelMap).getLines.toList
      .map(_.split("\\s+", 2))
      .map { case Array(s, l) => (s.trim, l.trim) }
      .toMap
    // ex: 169 => ["giant panda", "panda", "panda bear", ...]
    Source.fromFile(idToStringIdMap).getLines.toList
      .dropWhile(_.trim.startsWith("#"))
      .grouped(4)
      .map { grouped =>
        val cl = grouped(1).split(":")(1).trim.toInt
        val cls = grouped(2).split(":")(1).trim.stripPrefix("\"").stripSuffix("\"")
        (cl, lm(cls))
      }
      .toMap
  }
}

case class DataSourceParams(
  appName: String,
  labelMapFile: String,
  categoryMapFile: String,
  evalK: Option[Int]
) extends Params

class DataSource (
  val dsp: DataSourceParams
) extends PDataSource[TrainingData, EmptyEvaluationInfo, Query, ActualResult] {

  @transient lazy val logger = Logger[this.type]

  /** Helper function used to store data given a SparkContext. */
  private def readEventData(sc: SparkContext) : RDD[Observation] = {
    //Get RDD of Events.
    PEventStore.find(
      appName = dsp.appName
      // Convert collected RDD of events to and RDD of Observation
      // objects.
    )(sc).map(e => {
      val cl : Int = e.properties.get[Int]("labelId")
      Observation(
        e.properties.get[String]("filename"),
        cl,
        ""
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
) extends Serializable {}


