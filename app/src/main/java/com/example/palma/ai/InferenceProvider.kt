package com.example.palma.ai

import android.content.Context

object InferenceProvider {
    @Volatile
    private var instance: ModelInference? = null

    fun get(context: Context): ModelInference {
        val existing = instance
        if (existing != null) return existing

        return synchronized(this) {
            val recheck = instance
            if (recheck != null) {
                recheck
            } else {
                ModelInference(context.applicationContext).also { instance = it }
            }
        }
    }

    fun release() {
        synchronized(this) {
            instance?.release()
            instance = null
        }
    }
}