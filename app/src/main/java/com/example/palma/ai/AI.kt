package com.example.palma.ai

import android.content.Context
import com.example.palma.ai.index.Index
import com.example.palma.ai.mid.Mid
import com.example.palma.ai.palma.Palma
import com.example.palma.ai.pinky.Pinky
import com.example.palma.ai.rin.Rin
import com.example.palma.ai.tom.Tom

//START of CLASS: AI
class AI{
    //START of FUNCTION: writeAI
    fun writeAI(context: Context, userKey: String, messageKey: String, ai: String, message: String){
        //START of IF-STATEMENT:
        if(ai == "Palma"){
            Palma().writeMessage(context, userKey, messageKey, message)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(ai == "Tom"){
            Tom().writeMessage(context, userKey, messageKey, message)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(ai == "Index"){
            Index().writeMessage(context, userKey, messageKey, message)
        }//END of IF-STATEMENT:

        //START of IF-STATEMENT:
        if(ai == "Mid"){
            Mid().writeMessage(context, userKey, messageKey, message)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT
        if(ai == "Rin"){
            Rin().writeMessage(context, userKey, messageKey, message)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(ai == "Pinky"){
            Pinky().writeMessage(context, userKey, messageKey, message)
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeAI
}//END of CLASS: AI