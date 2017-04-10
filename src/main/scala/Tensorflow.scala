/*
Wrapper around TensorflowNative.scala
*/

package org.template.tensorflow

class TensorFlow(
  modelPath: String,
  maxSize: Int = 2048) {

  private var session = TensorFlowNative.native.tfCreateSession(modelPath)

  def runBytes(
    inputLayer: String,
    outputLayer: String,
    data: Array[Byte],
    expectedOutputSize: Int = maxSize): Array[Float] = {

    val result = new Array[Float](expectedOutputSize)
    val outputSize = TensorFlowNative.native.tfRunString(
      session, inputLayer, outputLayer, data, data.length, result)

    result.take(outputSize)
  }

  def runFloats(
    inputLayer: String,
    outputLayer: String,
    data: Array[Float],
    expectedOutputSize: Int = maxSize): Array[Float] = {

    val result = new Array[Float](expectedOutputSize)
    val outputSize = TensorFlowNative.native.tfRunFloatArray(
      session, inputLayer, outputLayer, data, data.length, result)
    result.take(outputSize)
  }

  def close(): Unit = {
    TensorFlowNative.native.tfCloseSession(session)
  }

}

object TensorFlow {
  private def using[A, B <: { def close(): Unit }](closeable: B)(f: B => A): A = {
    try {
      f(closeable)
    } finally {
      if (closeable != null) {
        closeable.close()
      }
    }
  }
  def using[T](modelPath: String)(f: TensorFlow => T): T =
    using(new TensorFlow(modelPath))(f)

  def using[T](modelPath: String, maxSize: Int)(f: TensorFlow => T): T =
    using(new TensorFlow(modelPath, maxSize))(f)
}
