package com.jamesfirstok.aegis.ai

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer

class InferenceEngine(context: Context) {

    private var interpreter: Interpreter? = null

    fun loadModel(model: MappedByteBuffer) {
        interpreter = Interpreter(model)
    }

    fun runInference(input: FloatArray): FloatArray {
        val output = Array(1) { FloatArray(4) }

        interpreter?.run(arrayOf(input), output)

        return output[0]
    }

    fun close() {
        interpreter?.close()
    }
}
