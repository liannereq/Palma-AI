package com.example.palma.ai.tom

import android.content.Context
import com.example.palma.ai.Classification
import com.example.palma.ai.palma.Command
import com.example.palma.ai.palma.Etiquette
import com.example.palma.ai.palma.Forecast
import com.example.palma.ai.palma.Query
import com.example.palma.models.Contact
import com.example.palma.models.Message
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.database.database
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//START of CLASS: Tom
class Tom{
    private val database = Firebase.database

    //START of FUNCTION: writeTom
    fun writeTom(userKey: String, username: String): Task<Void> {
        val contactReference = database.getReference("Palma/User/$userKey/Contact")
        val messageReference = database.getReference("Palma/Message")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        return messageReference.get().continueWithTask { messageTask ->
            //START of IF-STATEMENT:
            if (!messageTask.isSuccessful) {
                throw messageTask.exception ?: Exception("Failed to fetch messages")
            }//END of IF-STATEMENT

            val snapshot = messageTask.result
            var index = 1
            var messageKey = "Message - $index"

            //START of WHILE-LOOP:
            while(snapshot.hasChild(messageKey)) {
                index++
                messageKey = "Message - $index"
            }//END of WHILE-LOOP

            contactReference.get().continueWithTask { contactTask ->
                //START of IF-STATEMENT:
                if (!contactTask.isSuccessful) {
                    throw contactTask.exception ?: Exception("Failed to fetch contacts")
                }//END of IF-STATEMENT

                val contactSnapshot = contactTask.result
                var index = 1
                var contactKey = "Contact - $index"

                //START of WHILE-LOOP:
                while (contactSnapshot.hasChild(contactKey)) {
                    index++
                    contactKey = "Contact - $index"
                }//END of WHILE-LOOP

                val contact = Contact(messageKey, "Tom", "11111", "tom@ai.com", "ai")
                val message = Message("AI - 2", date, time, "Greetings $username, I am Tom")

                val contactTaskRef = contactReference.child(contactKey).setValue(contact)
                val messageTaskRef = messageReference.child("$messageKey/message1").setValue(message)

                return@continueWithTask Tasks.whenAll(contactTaskRef, messageTaskRef)
            }
        }
    }//END of FUNCTION: writeTom

    //START of FUNCTION: writeMessage
    fun writeMessage(context: Context, userKey: String, messageKey: String, prompt: String){
        val classification = Classification(context).classifyContext(prompt)

        //START of IF-STATEMENT:
        if(classification == "etiquette"){
            Etiquette().writeEtiquette(userKey, messageKey, prompt)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(classification == "forecast"){
            Forecast().writeForecast(userKey, messageKey, prompt)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(classification == "query"){
            Query().writeQuery(userKey, messageKey, prompt)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(classification == "command"){
            Command().writeCommand(context, userKey, messageKey, prompt)
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeMessage
}//END of CLASS: Tom