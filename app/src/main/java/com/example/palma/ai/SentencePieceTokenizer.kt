package com.example.palma.ai

import android.content.Context
import java.io.File
import org.bytedeco.sentencepiece.IntVector
import org.bytedeco.sentencepiece.SentencePieceProcessor

class SentencePieceTokenizer(context: Context) {
    private val processor: SentencePieceProcessor
    
    init {
        val modelPath = copyModelToInternalStorage(context)
        processor = SentencePieceProcessor()
        processor.LoadOrDie(modelPath)
    }
    
    private fun copyModelToInternalStorage(context: Context): String {
        try {
            val outFile = File(context.filesDir, "spm_8k.model")
            if (!outFile.exists()) {
                context.assets.open("spm_8k.model").use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            return outFile.absolutePath
        } catch (e: Exception) {
            throw RuntimeException("Failed to prepare SentencePiece model: ${e.message}", e)
        }
    }

    fun tokenize(text: String): IntArray {
        try {
            val ids: IntVector = processor.EncodeAsIds(text)
            val size = ids.size().toInt()
            val result = IntArray(size)

            for (i in 0 until size) {
                result[i] = ids.get(i.toLong())
            }

            return result
        } catch (e: Exception) {
            throw RuntimeException("SentencePiece encode failed: ${e.message}", e)
        }
    }
    
    fun decode(tokenIds: IntArray): String {
        try {
            val ids = IntVector(tokenIds.size.toLong())
            for (i in tokenIds.indices) {
                ids.put(i.toLong(), tokenIds[i])
            }
            return processor.DecodeIds(ids)
        } catch (e: Exception) {
            throw RuntimeException("SentencePiece decode failed: ${e.message}", e)
        }
    }
}
