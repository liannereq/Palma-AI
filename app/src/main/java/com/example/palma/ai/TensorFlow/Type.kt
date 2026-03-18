package com.example.palma.ai.TensorFlow

import android.util.Log
import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

//START of CLASS: Type
class Type(context: Context){
    private val typeQueryInterpreter: Interpreter by lazy{
        interpretQuery(context, "type_query_classifier.tflite")
    }
    private var type = ""

    // START of FUNCTION: typeQuery
    fun typeQuery(prompt: String): String{

        //START of IF-STATEMENT:
        if(prompt.isBlank()){
            Log.e("TF", "Prompt is empty")
            return "user"
        }//END of IF-STATEMENT

        val maxLength = 12
        val numClasses = 3

        val tokens = prompt.lowercase().split(" ").map { it.trim() }
        val floatSequence = FloatArray(maxLength) { 0f }

        //START of FOR-LOOP:
        for(i in tokens.indices){
            if(i >= maxLength) break
            floatSequence[i] = (tokens[i].hashCode() % 1500).toFloat()
            if(floatSequence[i] < 0) floatSequence[i] += 1500f
        }//END of FOR-LOOP

        val inputFeatures = arrayOf(floatSequence)
        val output = Array(1){FloatArray(numClasses)}

        //START of TRY:
        try{
            typeQueryInterpreter.run(inputFeatures, output)
        }//END of TRY

        //START of CATCH:
        catch(e: Exception){
            Log.e("TF", "Interpreter run failed: ${e.message}")
            return "log"
        }//END of CATCH

        val prediction = output[0].indices.maxByOrNull{output[0][it]} ?: 0

        val type = when(prediction){
            0 -> "user"
            1 -> "ai"
            2 -> "log"
            else -> "log"
        }

        Log.d("found type", "$type (confidence: ${output[0][prediction]})")
        return type
    }// END of FUNCTION: typeQuery

    //START of FUNCTION: interpretQuery
    private fun interpretQuery(context: Context, fileName: String): Interpreter{
        val fileDescriptor = context.assets.openFd(fileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val buffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        inputStream.close()

        val options = Interpreter.Options().apply{setNumThreads(2)}
        return Interpreter(buffer, options)
    }//END of FUNCTION: interpretQuery

    //START of FUNCTION: typeForecast
    fun typeForecast(prompt: String): String{

        Log.d("found type", type)
        return type
    }//END of FUNCTION: typeForecast
}//END of CLASS: Type