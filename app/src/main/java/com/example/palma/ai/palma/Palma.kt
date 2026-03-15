package com.example.palma.ai.palma

import android.util.Log
import com.example.palma.models.Contact
import com.example.palma.models.Message
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.database.database
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//START of CLASS: Palma
class Palma{
    private val database = Firebase.database

    //START of FUNCTION: writePalma
    fun writePalma(userKey: String, username: String): Task<Void> {
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

                val contact = Contact(messageKey, "Palma", "00000", "palma@ai.com", "ai")
                val message = Message("AI - 1", date, time, "Greetings $username, I am Palma")

                val contactTaskRef = contactReference.child(contactKey).setValue(contact)
                val messageTaskRef = messageReference.child("$messageKey/message1").setValue(message)

                return@continueWithTask Tasks.whenAll(contactTaskRef, messageTaskRef)
            }
        }
    }//END of FUNCTION: writePalma

    //START of FUNCTION: writeMessage
    fun writeMessage(userKey: String, messageKey: String, prompt: String){
        val list = Regex("[A-Za-z]+|\\d+|[-.,!?;:]").findAll(prompt).map{it.value}.toList()
        val countToken = list.size
        val countCharacter = prompt.length

        Log.d("token count", countToken.toString())
        Log.d("character count", countCharacter.toString())

        val context = classifyContext(prompt)
    }//END of FUNCTION: writeMessage

    //START of FUNCTION: classifyContext
    private fun classifyContext(prompt: String){
    }//END of FUNCTION: classifyContext
}//END of CLASS: Palma