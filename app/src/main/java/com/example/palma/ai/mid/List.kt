package com.example.palma.ai.mid

import android.util.Log
import com.example.palma.models.Message
import com.example.palma.models.User
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//START of CLASS: List
class List{
    private val database = Firebase.database
    private val aiKey = "AI - 4"
    private val curseKey = setOf("fuck", "fucking", "fucked", "shit", "bullshit", "damn", "hell", "piss", "pissed", "screw", "jackass", "asshole", "douche", "prick", "bastard", "dumbass", "moron", "idiot", "jerk", "tool", "pussy", "chicken", "coward", "weakling", "spineless", "scaredy-cat", "dick", "dickhead", "bitch", "cunt", "motherfucker", "fucker", "crap", "freaking", "frick", "heck", "darn")

    //START of FUNCTION: writeList
    fun writeList(userKey: String, messageKey: String, message: String){
        val list = message.lowercase().trim().split(" ")

        //START of IF-STATEMENT:
        if(list[1] == "command"){
            writeCommand(userKey, messageKey)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(list[1] == "contact"){
            writeContact(userKey, messageKey)
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeList

    //START of FUNCTION: censor
    private fun censor(message: String): String{
        var censored = message

        curseKey.forEach{ curse ->
            val regex = Regex("\\b$curse\\b", RegexOption.IGNORE_CASE)
            censored = censored.replace(regex){
                val word = it.value
                val middle = buildString{
                    repeat(word.length - 2){
                        append("!@#$%^&*?".random())
                    }
                }
                if(word.length > 2) "${word.first()}$middle${word.last()}" else word
            }
        }

        return censored
    }//END of FUNCTION

    //START of FUNCTION: writeCommand
    private fun writeCommand(userKey: String, messageKey: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")

        userReference.get().addOnSuccessListener{ userSnapshot ->
            val list = arrayOf(
                "#list command",
                "#list contact"
            )

            messageReference.addListenerForSingleValueEvent(object: ValueEventListener{
                //START of FUNCTION: onDataChange
                override fun onDataChange(snapshot: DataSnapshot){
                    var index = 1
                    var key = "message$index"

                    //START of WHILE-LOOP:
                    while(snapshot.hasChild(key)){
                        index++
                        key = "message$index"
                    }//END of WHILE-LOOP

                    //START of FOR-LOOP:
                    for(i in 0 until list.size){
                        val current = LocalDateTime.now()
                        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                        val key = "message$index"

                        messageReference.child(key).setValue(Message(aiKey, date, time, list[i]))

                        index++
                    }//END of FOR-LOOP

                    success(userKey, messageKey, "command", "command")
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError){
                }//END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION:

    //START of FUNCTION: writeContact
    private fun writeContact(userKey: String, messageKey: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val contactReference = database.getReference("Palma/User/$userKey/Contact")
        val messageReference = database.getReference("Palma/Message/$messageKey")

        userReference.get().addOnSuccessListener{ snapshot ->
            val user = snapshot.getValue(User::class.java)

            messageReference.addListenerForSingleValueEvent(object : ValueEventListener{
                //START of FUNCTION: onDataChange
                override fun onDataChange(messageSnapshot: DataSnapshot){
                    contactReference.get().addOnSuccessListener { contactSnapshot ->
                        val contactList = mutableListOf<String>()
                        contactList.add(censor("Your fucking ${user?.username}\'s Contact/s:"))

                        //START of FOR-LOOP:
                        for(contact in contactSnapshot.children){
                            val username = contact.child("username").getValue(String::class.java) ?: ""
                            val type = contact.child("type").getValue(String::class.java) ?: ""
                            val mobile = contact.child("mobile").getValue(String::class.java) ?: ""
                            val email = contact.child("email").getValue(String::class.java) ?: ""

                            val contactString = "$username $type $mobile $email"
                            contactList.add(contactString)
                            Log.d("found contact", username)
                        }//END of FOR-LOOP

                        var index = 1
                        var key = "message$index"

                        //START of WHILE-LOOP:
                        while(messageSnapshot.hasChild(key)){
                            index++
                            key = "message$index"
                        }//END of WHILE-LOOP

                        //START of FOR-LOOP:
                        for(contactInfo in contactList){
                            val current = LocalDateTime.now()
                            val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

                            val message = Message(aiKey, date, time, contactInfo)
                            messageReference.child(key).setValue(message)
                            key = "message${index++}"
                        }//END of FOR-LOOP

                        success(userKey, messageKey, "contact", "contact")
                    }
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError) {
                }
                //END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION: writeContact

    //START of FUNCTION: success
    private fun success(userKey: String, messageKey: String, type: String, list: String){
        val userReference = database.getReference("Palma/User/$userKey")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        userReference.get().addOnSuccessListener{ userSnapshot ->
            messageReference.addListenerForSingleValueEvent(object : ValueEventListener{
                //START of FUNCTION: onDataChange
                override fun onDataChange(snapshot: DataSnapshot){
                    var index = 1
                    var key = "message$index"
                    var response = ""

                    //START of WHILE-LOOP:
                    while(snapshot.hasChild(key)){
                        index++
                        key = "message$index"
                    }//END of WHILE-LOOP

                    //START of IF-STATEMENT:
                    if((type == "command") || (type == "contact") || (type == "load")){
                        response = "Don't fucking make me load $list list again..."
                    }//END of IF-STATEMENT

                    val message = Message(aiKey, date, time, censor(response))
                    messageReference.child(key).setValue(message)
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError){
                }//END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION: success
}//END of CLASS: List