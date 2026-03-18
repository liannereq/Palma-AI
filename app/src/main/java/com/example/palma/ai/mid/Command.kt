package com.example.palma.ai.mid

import android.content.Context
import com.example.palma.ai.TensorFlow.Classification

//START of CLASS: Command
class Command{
    //START of FUNCTION: writeCommand:
    fun writeCommand(context: Context, userKey: String, messageKey: String, prompt: String){
        val classification = Classification(context).classifyCommand(prompt)

        //START of IF-STATEMENT
        if(classification == "list"){
            List().writeList(userKey, messageKey, prompt)
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeCommand
}//END of CLASS: Command