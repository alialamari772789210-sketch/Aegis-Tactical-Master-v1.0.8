package com.jamesfirstok.aegis.ai

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

data class ThreatPrediction(val classId: Int, val confidence: Float, val probabilities: FloatArray)

class AegisAIAnalyzer(context: Context) {
    private var interpreter: Interpreter? = null
    private var modelLoaded: Boolean = false
    private var inputShape: IntArray = intArrayOf(1, 64)

    init {
        try {
            val options = Interpreter.Options().apply { setNumThreads(4); setUseNNAPI(true) }
            interpreter = Interpreter(loadModelFile(context, "1.tflite"), options)
            interpreter?.allocateTensors()
            inputShape = interpreter?.getInputTensor(0)?.shape() ?: intArrayOf(1, 64)
            modelLoaded = true
        } catch (e: Exception) { modelLoaded = false }
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val afd = context.assets.openFd(modelName)
        return FileInputStream(afd.fileDescriptor).channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
    }

    fun analyzeThreat(signalData: FloatArray): ThreatPrediction {
        if (!modelLoaded) return analyzeWithRules(signalData)
        val targetSize = inputShape.lastOrNull() ?: 64
        val adaptedInput = adaptSignalToTargetSize(signalData, targetSize)
        val normalizedInput = normalize(adaptedInput)
        val output = Array(1) { FloatArray(4) }
        interpreter?.run(arrayOf(normalizedInput), output)
        val exp = output[0].map { kotlin.math.exp(it.toDouble()).toFloat() }
        val sum = exp.sum().coerceAtLeast(1e-6f)
        val soft = exp.map { it / sum }.toFloatArray()
        val id = soft.indices.maxByOrNull { soft[it] } ?: 0
        return ThreatPrediction(id, soft[id], soft)
    }

    private fun adaptSignalToTargetSize(data: FloatArray, targetSize: Int): FloatArray {
        if (data.size == targetSize) return data
        val adapted = FloatArray(targetSize)
        val scale = data.size.toFloat() / targetSize.toFloat()
        for (i in 0 until targetSize) {
            val idx = (i * scale).toInt().coerceIn(0, data.size - 1)
            adapted[i] = data[idx]
        }
        return adapted
    }

    private fun normalize(data: FloatArray): FloatArray {
        val mean = data.average().toFloat()
        val variance = data.map { (it - mean) * (it - mean) }.average().toFloat()
        val std = kotlin.math.sqrt(variance.coerceAtLeast(1e-6f))
        return data.map { (it - mean) / std }.toFloatArray()
    }

    private fun analyzeWithRules(data: FloatArray): ThreatPrediction {
        val energy = data.map { it * it }.average().toFloat()
        val id = if (energy > 0.6f) 2 else 0
        return ThreatPrediction(id, 0.85f, FloatArray(4) { if (it == id) 0.85f else 0.05f })
    }
}
