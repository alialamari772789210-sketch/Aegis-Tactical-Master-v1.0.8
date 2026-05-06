package com.jamesfirstok.aegis.ai

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

data class ThreatPrediction(
    val classId: Int,
    val confidence: Float,
    val probabilities: FloatArray
)

class AegisAIAnalyzer(context: Context) {
    private var interpreter: Interpreter? = null

    init {
        interpreter = Interpreter(loadModelFile(context, "aegis_model.tflite"))
        interpreter?.allocateTensors()
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val afd = context.assets.openFd(modelName)
        val inputStream = FileInputStream(afd.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
    }

    fun analyzeThreat(signalData: FloatArray): ThreatPrediction {
        val localInterpreter = requireNotNull(interpreter) { "Interpreter not initialized" }
        val input = normalize(signalData)
        val output = Array(1) { FloatArray(4) }

        localInterpreter.run(arrayOf(input), output)

        val probabilities = softmax(output[0])
        val classId = probabilities.indices.maxBy { probabilities[it] }
        val confidence = probabilities[classId]

        return ThreatPrediction(classId, confidence, probabilities)
    }

    private fun normalize(data: FloatArray): FloatArray {
        val mean = data.average().toFloat()
        val variance = data.map { (it - mean) * (it - mean) }.average().toFloat()
        val std = kotlin.math.sqrt(variance.coerceAtLeast(1e-6f))
        return data.map { ((it - mean) / std) }.toFloatArray()
    }

    private fun softmax(values: FloatArray): FloatArray {
        val max = values.maxOrNull() ?: 0f
        val exp = values.map { kotlin.math.exp((it - max).toDouble()).toFloat() }
        val sum = exp.sum().coerceAtLeast(1e-6f)
        return exp.map { it / sum }.toFloatArray()
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
