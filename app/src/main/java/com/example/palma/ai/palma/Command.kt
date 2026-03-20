package com.example.palma.ai.palma

import android.content.Context
import android.util.Log
import com.example.palma.ai.TensorFlow.Build
import com.example.palma.ai.TensorFlow.Classification

//START of CLASS: Command
class Command{
    //START of FUNCTION: writeCommand:
    fun writeCommand(context: Context, userKey: String, messageKey: String, prompt: String){
        val classification = Classification(context).classifyCommand(prompt)
        val command = Build().command(classification, prompt)

        //START of IF-STATEMENT
        if(classification == "list"){
            Log.d("command", command)
            List().writeList(userKey, messageKey, command)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT
        if(classification == "contact"){
            Log.d("command", command)
            Contact().writeContact(userKey, messageKey, command)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(classification == "reminder"){
            Log.d("command", command)
            Reminder().writeReminder(userKey, messageKey, command)
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeCommand
}//END of CLASS: Command