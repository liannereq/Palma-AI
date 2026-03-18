package com.example.palma.ai.TensorFlow

import android.util.Log
import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

//START of CLASS: Classification
class Classification(context: Context){
    private val contextInterpreter: Interpreter by lazy{
        interpretContext(context, "context_classifier.tflite")
    }
    private val commandInterpreter: Interpreter by lazy {
        interpretCommand(context, "command_classifier.tflite")
    }
    private var classification = ""

    //START of FUNCTION: classifyContext
    fun classifyContext(prompt: String): String{

        //START of IF-STATEMENT:
        if(prompt.isBlank()){
            Log.e("TF", "Prompt is empty")
            return "query"
        }//END of IF-STATEMENT

        val tokens = Regex("\\S+").findAll(prompt).map { it.value }.toList()
        val countToken = tokens.size
        val countCharacter = prompt.length

        Log.d("token count", countToken.toString())
        Log.d("character count", countCharacter.toString())

        val inputFeatures = arrayOf(floatArrayOf(countToken.toFloat(), countCharacter.toFloat()))
        val output = Array(1){FloatArray(4)}

        //START of TRY
        try{
            contextInterpreter.run(inputFeatures, output)
        }//END of TRY

        //START of CATCH:
        catch(e: Exception){
            Log.e("TF", "Interpreter run failed: ${e.message}")
            return "none"
        }//END of CATCH

        val prediction = output[0].indices.maxByOrNull{output[0][it]} ?: 2

        val classification = when(prediction){
            0 -> "command"
            1 -> "etiquette"
            2 -> "query"
            3 -> "forecast"
            else -> "none"
        }

        Log.d("context classification", classification)
        return classification
    }

    //START of FUNCTION: interpretContext
    private fun interpretContext(context: Context, fileName: String): Interpreter{
        val fileDescriptor = context.assets.openFd(fileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val buffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        inputStream.close()

        val options = Interpreter.Options().apply { setNumThreads(2) }
        return Interpreter(buffer, options)
    }//END of FUNCTION: interpretContext

    //START of FUNCTION: classifyCommand
    fun classifyCommand(prompt: String): String{
        //START of IF-STATEMENT
        if(prompt.isBlank()){
            Log.e("TF", "Command prompt is empty")
            return "default"
        }//END of IF-STATEMENT

        val list = Regex("[A-Za-z]+|\\d+|[-.,!?;:]").findAll(prompt).map{it.value}.toList()
        val countToken = list.size
        val countCharacter = prompt.length

        Log.d("command token count", countToken.toString())
        Log.d("command character count", countCharacter.toString())

        val inputFeatures = arrayOf(floatArrayOf(countToken.toFloat(), countCharacter.toFloat()))
        val output = Array(1) { FloatArray(4) }

        //START of TRY:
        try{
            commandInterpreter.run(inputFeatures, output)
        }//END of TRY

        //START of CATCH:
        catch(e: Exception){
            Log.e("TF", "Command interpreter failed: ${e.message}")
            return "default"
        }//END of CATCH

        val prediction = output[0].indices.maxByOrNull{output[0][it]} ?: 3

        this.classification = when(prediction){
            0 -> "list"
            1 -> "reminder"
            2 -> "contact"
            else -> "default"
        }

        Log.d("command classification", this.classification)
        return this.classification
    }//END of FUNCTION: classifyCommand

    //START of FUNCTION: interpretCommand
    private fun interpretCommand(context: Context, fileName: String): Interpreter{
        val fileDescriptor = context.assets.openFd(fileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        val buffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        inputStream.close()

        val options = Interpreter.Options().apply{setNumThreads(2)}

        return Interpreter(buffer, options)
    }//END of FUNCTION: interpretCommand
}//END of CLASS: Classification