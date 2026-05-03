package com.jamesfirstok.aegis.ai

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream

class AegisAIAnalyzer(context: Context) {
    private var interpreter: Interpreter? = null

    init {
        try {
            interpreter = Interpreter(loadModelFile(context, "aegis_model.tflite"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    fun analyzeThreat(signalData: FloatArray): Float {
        val output = FloatArray(1)
        interpreter?.run(signalData, output)
        return output[0] // نسبة احتمالية التهديد (0.0 - 1.0)
    }
}
