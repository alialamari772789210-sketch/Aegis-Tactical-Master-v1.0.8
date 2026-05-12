package com.jamesfirstok.aegis.ai

import android.content.Context
import android.util.Log
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
    private val modelLoaded: Boolean
    private var inputShape: IntArray = intArrayOf(1, 64)
    private var outputShape: IntArray = intArrayOf(1, 4)

    init {
        modelLoaded = try {
            interpreter = Interpreter(loadModelFile(context, "1.tflite"))
            interpreter?.allocateTensors()
            
            // قراءة أبعاد النموذج الفعلية
            val inputTensor = interpreter?.getInputTensor(0)
            val outputTensor = interpreter?.getOutputTensor(0)
            
            inputShape = inputTensor?.shape() ?: intArrayOf(1, 64)
            outputShape = outputTensor?.shape() ?: intArrayOf(1, 4)
            
            Log.i("AegisAI", "✅ TFLite model loaded: 1.tflite")
            Log.i("AegisAI", "   Input shape: ${inputShape.joinToString()}")
            Log.i("AegisAI", "   Output shape: ${outputShape.joinToString()}")
            true
        } catch (e: Exception) {
            Log.w("AegisAI", "⚠️ Model not found, using rule-based mode: ${e.message}")
            false
        }
    }

    /**
     * تحميل النموذج من مجلد الأصول
     */
    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val afd = context.assets.openFd(modelName)
        val inputStream = FileInputStream(afd.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
    }

    /**
     * تحليل التهديد: إذا النموذج موجود يستخدمه، وإلا يستخدم قواعد ثابتة.
     */
    fun analyzeThreat(signalData: FloatArray): ThreatPrediction {
        return if (modelLoaded) {
            analyzeWithAI(signalData)
        } else {
            analyzeWithRules(signalData)
        }
    }

    /**
     * التحليل باستخدام نموذج TFLite
     */
    private fun analyzeWithAI(signalData: FloatArray): ThreatPrediction {
        val localInterpreter = requireNotNull(interpreter) { "Interpreter not initialized" }

        // تجهيز الدخل: قص أو تمديد الإشارة لتطابق حجم دخل النموذج
        val inputSize = inputShape.lastOrNull() ?: 64
        val normalized = normalize(signalData)
        val modelInput = prepareInput(normalized, inputSize)

        // تحضير مصفوفة الخرج حسب شكل النموذج
        val outputSize = outputShape.lastOrNull() ?: 4
        val output = Array(1) { FloatArray(outputSize) }

        localInterpreter.run(arrayOf(modelInput), output)

        val probabilities = softmax(output[0])
        val classId = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        val confidence = probabilities[classId]

        return ThreatPrediction(classId, confidence, probabilities)
    }

    /**
     * التحليل بالقواعد عندما لا يوجد نموذج
     */
    private fun analyzeWithRules(data: FloatArray): ThreatPrediction {
        val energy = data.map { it * it }.average().toFloat()
        val classId = if (energy > 0.5f) 1 else 0
        val confidence = if (energy > 0.5f) 0.85f else 0.1f
        val probabilities = FloatArray(4) { i ->
            if (i == classId) confidence else (1f - confidence) / 3f
        }
        return ThreatPrediction(classId, confidence, probabilities)
    }

    /**
     * تطبيع Z-Score
     */
    private fun normalize(data: FloatArray): FloatArray {
        if (data.isEmpty()) return FloatArray(64) { 0f }
        val mean = data.average().toFloat()
        val variance = data.map { (it - mean) * (it - mean) }.average().toFloat()
        val std = kotlin.math.sqrt(variance.coerceAtLeast(1e-6f))
        return data.map { ((it - mean) / std) }.toFloatArray()
    }

    /**
     * تحضير الدخل ليطابق حجم النموذج
     */
    private fun prepareInput(data: FloatArray, targetSize: Int): FloatArray {
        return when {
            data.size == targetSize -> data
            data.size > targetSize -> data.copyOf(targetSize)
            else -> {
                val padded = FloatArray(targetSize)
                System.arraycopy(data, 0, padded, 0, data.size)
                padded
            }
        }
    }

    /**
     * تحويل المخرجات إلى احتمالات
     */
    private fun softmax(values: FloatArray): FloatArray {
        val max = values.maxOrNull() ?: 0f
        val exp = values.map { kotlin.math.exp((it - max).toDouble()).toFloat() }
        val sum = exp.sum().coerceAtLeast(1e-6f)
        return exp.map { it / sum }.toFloatArray()
    }

    /**
     * إغلاق المحلل وتحرير الموارد
     */
    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
