package com.jamesfirstok.aegis.ai

import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer

class InferenceEngine {
    private var interpreter: Interpreter? = null

    fun loadModel(model: MappedByteBuffer) {
        interpreter?.close()
        interpreter = Interpreter(model, Interpreter.Options().apply {
            setNumThreads(4)
        })
        interpreter?.allocateTensors()
    }

    fun runInference(input: FloatArray, outputSize: Int): FloatArray {
        val localInterpreter = requireNotNull(interpreter) { "Model not loaded" }
        val output = Array(1) { FloatArray(outputSize) }
        localInterpreter.run(arrayOf(input), output)
        return output[0]
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
