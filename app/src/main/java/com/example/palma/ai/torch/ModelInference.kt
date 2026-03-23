package com.example.palma.ai.torch

import android.content.Context
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import kotlin.math.exp
import kotlin.random.Random

/**
 * ModelInference: Handles TFLite model loading and inference execution.
 * The agent's response is stored in: val response = tokenizer.decode(outputTokens.toIntArray())
 */
class ModelInference(private val context: Context) {
    private var interpreter: Interpreter? = null
    private lateinit var tokenizer: SentencePieceTokenizer
    private var vocabSize = 8000
    private var expectsInt64Input = false
    
    init {
        loadModel(context)
        tokenizer = SentencePieceTokenizer(context)
    }
    
    /**
     * Load the TFLite model from assets
     */
    private fun loadModel(context: Context) {
        try {
            val modelBuffer = FileUtil.loadMappedFile(context, "model_float32.tflite")
            val options = Interpreter.Options()
            options.setNumThreads(4)  // Adjust based on device cores
            
            // Uncomment for GPU acceleration (requires GPU delegate dependency)
            // val gpuDelegate = GpuDelegate()
            // options.addDelegate(gpuDelegate)

            interpreter = Interpreter(modelBuffer, options)

            // Introspect model IO so runtime code follows the exported model contract.
            val inputTensor = interpreter!!.getInputTensor(0)
            val outputTensor = interpreter!!.getOutputTensor(0)
            expectsInt64Input = inputTensor.dataType() == DataType.INT64
            val outShape = outputTensor.shape()
            vocabSize = outShape.last()

            println("✓ Model loaded successfully")
        } catch (e: Exception) {
            throw RuntimeException("Failed to load model: ${e.message}", e)
        }
    }
    

    fun generateResponse(
        prompt: String,
        maxTokens: Int = 50,
        temperature: Float = 0.8f,
        topK: Int = 40
    ): String {
        if (interpreter == null) {
            throw RuntimeException("Model not loaded")
        }
        
        // Tokenize input
        val inputTokens = tokenizer.tokenize(prompt)
        if (inputTokens.isEmpty()) {
            throw IllegalArgumentException("Prompt tokenization resulted in empty sequence")
        }
        
        val outputTokens = mutableListOf<Int>()
        var currentSequence = inputTokens.toMutableList()
        
        // Generate tokens iteratively
        for (step in 0 until maxTokens) {
            val nextInputToken = currentSequence.last()
            val inputObject: Any = if (expectsInt64Input) {
                arrayOf(longArrayOf(nextInputToken.toLong()))
            } else {
                arrayOf(intArrayOf(nextInputToken))
            }
            val outputObject = Array(1) { Array(1) { FloatArray(vocabSize) } }
            
            // Run inference
            try {
                interpreter!!.run(inputObject, outputObject)
            } catch (e: Exception) {
                throw RuntimeException("Inference failed at step $step: ${e.message}", e)
            }
            
            val logits = outputObject[0][0]
            
            // Apply temperature
            val safeTemp = if (temperature <= 1e-6f) 1e-6f else temperature
            val scaledLogits = logits.map { it / safeTemp }.toFloatArray()
            
            // Numerically stable softmax
            val maxLogit = scaledLogits.maxOrNull() ?: 0f
            val expLogits = scaledLogits.map { exp(it - maxLogit) }.toFloatArray()
            val sumExp = expLogits.sum()
            val probabilities = if (sumExp > 0f) {
                expLogits.map { it / sumExp }.toFloatArray()
            } else {
                FloatArray(vocabSize) { 1f / vocabSize }
            }
            
            // Sample top-k token (or use greedy if k >= vocab size)
            val nextToken = if (topK <= 1 || topK >= vocabSize) {
                // Greedy: take highest probability
                probabilities.indices.maxByOrNull { probabilities[it] } ?: 1
            } else {
                // Top-k sampling
                val topKIndices = probabilities.withIndex()
                    .sortedByDescending { it.value }
                    .take(topK)
                    .map { it.index }
                
                val topKProbs = topKIndices.map { probabilities[it] }.toFloatArray()
                val sum = topKProbs.sum()
                val normalizedProbs = topKProbs.map { it / sum }.toFloatArray()
                
                // Sample from top-k
                var r = Random.nextFloat()
                var selected = topKIndices[0]
                for (i in normalizedProbs.indices) {
                    r -= normalizedProbs[i]
                    if (r <= 0) {
                        selected = topKIndices[i]
                        break
                    }
                }
                selected
            }
            
            outputTokens.add(nextToken)
            currentSequence.add(nextToken)
            
            // Stop if end token reached
            if (nextToken == 0 || nextToken == 1) break
        }
        
        val response = tokenizer.decode(outputTokens.toIntArray())
        
        return response
    }
    
    /**
     * Clean up resources
     */
    fun release() {
        interpreter?.close()
        interpreter = null
    }
}
