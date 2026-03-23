package com.example.palma.ai

import android.content.Context
import com.example.palma.models.Message
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//START of CLASS: Torch
class Torch{
    private val database = Firebase.database

    //START of FUNCTION: torch
    fun torch(context: Context, aiKey: String, messageKey: String, prompt: String){
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        messageReference.addListenerForSingleValueEvent(object : ValueEventListener{
                //START of FUNCTION: onDataChange
                override fun onDataChange(snapshot: DataSnapshot){
                    var index = 1
                    val response = try {
                        InferenceProvider.get(context).generateResponse(prompt, maxTokens = 50)
                    } catch (e: Exception) {
                        "Sorry, I could not generate a response right now."
                    }
                    var key = "message$index"

                    //START of WHILE-LOOP:
                    while(snapshot.hasChild(key)){
                        index++
                        key = "message$index"
                    }//END of WHILE-LOOP

                    messageReference.child(key).setValue(Message(aiKey, date, time, response))
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError){
                }//END of FUNCTION: onCancelled
        })
    }//END of FUNCTION: torch
}//END of CLASS: Torch