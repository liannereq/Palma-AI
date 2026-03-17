package com.example.palma.ai.palma

import android.content.Context
import com.example.palma.ai.Classification

//START of CLASS: Command
class Command{
    //START of FUNCTION: writeCommand:
    fun writeCommand(context: Context, userKey: String, messageKey: String, prompt: String){
        val command = Classification(context).classifyCommand(prompt)

        //START of IF-STATEMENT
        if(command == "list"){
            List().writeList(userKey, messageKey, prompt)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT
        if(command == "contact"){
            Contact().writeContact(userKey, messageKey, prompt)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(command == "reminder"){
            Reminder().writeReminder(userKey, messageKey, prompt)
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeCommand
}//END of CLASS: Command