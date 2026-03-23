package com.example.palma.ai.torch

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File

/** Tokenizer with SentencePiece runtime support and vocab fallback. */
class SentencePieceTokenizer(context: Context) {
    private val tag = "SentencePieceTokenizer"
    private val processor: Any?
    private val encodeMethod = "encodeAsIds"
    private val decodeMethod = "decodeIds"
    private val vocab: Map<String, Int>
    private val idToToken: Map<Int, String>
    private val unkId: Int
    
    init {
        val loadedVocab = loadVocab(copyVocabToInternalStorage(context))
        vocab = loadedVocab
        idToToken = loadedVocab.entries.associate { it.value to it.key }
        unkId = loadedVocab["<unk>"] ?: 3

        val modelPath = copyModelToInternalStorage(context)
        processor = tryCreateProcessorInstance(modelPath)

        if (processor != null) {
            Log.i(tag, "Tokenizer mode: runtime SentencePiece")
        } else {
            Log.i(tag, "Tokenizer mode: vocab fallback")
        }
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

    private fun copyVocabToInternalStorage(context: Context): File {
        try {
            val outFile = File(context.filesDir, "vocab.txt")
            if (!outFile.exists()) {
                context.assets.open("vocab.txt").use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            return outFile
        } catch (e: Exception) {
            throw RuntimeException("Failed to prepare vocab.txt: ${e.message}", e)
        }
    }

    private fun loadVocab(file: File): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        BufferedReader(file.reader()).use { reader ->
            reader.lineSequence().forEachIndexed { index, line ->
                val token = line.trim()
                if (token.isNotEmpty()) {
                    map[token] = index
                }
            }
        }
        if (map.isEmpty()) {
            throw RuntimeException("vocab.txt is empty")
        }
        return map
    }

    private fun tryCreateProcessorInstance(modelPath: String): Any? {
        val candidates = listOf(
            "com.google.sentencepiece.SentencePieceProcessor",
            "org.tensorflow.lite.support.text.tokenizer.SentencePieceTokenizer"
        )

        for (className in candidates) {
            try {
                val clazz = Class.forName(className)
                val instance = clazz.getDeclaredConstructor().newInstance()

                try {
                    clazz.getMethod("load", String::class.java).invoke(instance, modelPath)
                    return instance
                } catch (_: NoSuchMethodException) {
                }

                try {
                    clazz.getMethod("initialize", String::class.java).invoke(instance, modelPath)
                    return instance
                } catch (_: NoSuchMethodException) {
                }
            } catch (t: Throwable) {
            }
        }

        return null
    }

    private fun runtimeTokenize(text: String): IntArray {
        val p = processor ?: throw IllegalStateException("Runtime tokenizer unavailable")
        val method = p.javaClass.methods.firstOrNull {
            it.name == encodeMethod && it.parameterCount == 1 && it.parameterTypes[0] == String::class.java
        } ?: throw NoSuchMethodException("encodeAsIds(String) not found")

        val result = method.invoke(p, text)
        return when (result) {
            is IntArray -> result
            is LongArray -> result.map { it.toInt() }.toIntArray()
            is List<*> -> result.mapNotNull { (it as? Number)?.toInt() }.toIntArray()
            else -> throw IllegalStateException("Unsupported encode return type: ${result?.javaClass?.name}")
        }
    }

    private fun runtimeDecode(tokenIds: IntArray): String {
        val p = processor ?: throw IllegalStateException("Runtime tokenizer unavailable")
        val method = p.javaClass.methods.firstOrNull {
            it.name == decodeMethod && it.parameterCount == 1
        } ?: throw NoSuchMethodException("decodeIds(...) not found")

        val arg: Any = when {
            method.parameterTypes[0].isArray -> tokenIds
            List::class.java.isAssignableFrom(method.parameterTypes[0]) -> tokenIds.toList()
            else -> tokenIds.toList()
        }

        val result = method.invoke(p, arg)
        return result?.toString() ?: ""
    }

    private fun fallbackTokenize(text: String): IntArray {
        val ids = mutableListOf<Int>()
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

        for (word in words) {
            var remaining = "▁$word"
            while (remaining.isNotEmpty()) {
                var found: Int? = null
                var end = remaining.length
                while (end > 0) {
                    val piece = remaining.substring(0, end)
                    val id = vocab[piece]
                    if (id != null) {
                        found = id
                        ids.add(id)
                        remaining = remaining.substring(end)
                        break
                    }
                    end -= 1
                }

                if (found == null) {
                    ids.add(unkId)
                    break
                }
            }
        }

        return if (ids.isEmpty()) intArrayOf(unkId) else ids.toIntArray()
    }

    private fun fallbackDecode(tokenIds: IntArray): String {
        val special = setOf("<pad>", "<s>", "</s>", "<unk>")
        val pieces = tokenIds.mapNotNull { idToToken[it] }.filterNot { it in special }

        val sb = StringBuilder()
        for (piece in pieces) {
            if (piece.startsWith("▁")) {
                val content = piece.removePrefix("▁")
                if (content.isEmpty()) continue
                if (sb.isNotEmpty()) sb.append(' ')
                sb.append(content)
            } else {
                sb.append(piece)
            }
        }

        return sb.toString()
            .replace(Regex("\\s+([,.!?;:])"), "$1")
            .trim()
    }
    
    fun tokenize(text: String): IntArray {
        try {
            return if (processor != null) {
                runtimeTokenize(text)
            } else {
                fallbackTokenize(text)
            }
        } catch (e: Exception) {
            throw RuntimeException("SentencePiece encode failed: ${e.message}", e)
        }
    }
    
    fun decode(tokenIds: IntArray): String {
        try {
            return if (processor != null) {
                runtimeDecode(tokenIds)
            } else {
                fallbackDecode(tokenIds)
            }
        } catch (e: Exception) {
            throw RuntimeException("SentencePiece decode failed: ${e.message}", e)
        }
    }
}