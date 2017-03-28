import AssemblyKeys._

assemblySettings

name := "template-tensorflow"

organization := "org.apache.predictionio"

libraryDependencies ++= Seq(
  "org.apache.predictionio" %% "apache-predictionio-core" % "0.10.0-incubating" % "provided",
  "net.java.dev.jna" % "jna" % "4.2.2"
)
