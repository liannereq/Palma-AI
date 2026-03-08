package com.example.palma.ai.mid

import com.example.palma.models.Greeting
import com.example.palma.models.User
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//START of CLASS: Greeting
class Greeting{
    private val database = Firebase.database
    private val aiKey = "AI - 4"
    private val curseKey = setOf("fuck", "fucking", "fucked", "shit", "bullshit", "damn", "hell", "piss", "pissed", "screw", "jackass", "asshole", "douche", "prick", "bastard", "dumbass", "moron", "idiot", "jerk", "tool", "pussy", "chicken", "coward", "weakling", "spineless", "scaredy-cat", "dick", "dickhead", "bitch", "cunt", "motherfucker", "fucker", "crap", "freaking", "frick", "heck", "darn")

    //START of FUNCTION: writeGreeting
    fun writeGreeting(userKey: String, messageKey: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        userReference.get().addOnSuccessListener{ userSnapshot ->
            val user = userSnapshot.getValue(User::class.java)

            messageReference.addListenerForSingleValueEvent(object : ValueEventListener{
                //START of FUNCTION: onDataChange
                override fun onDataChange(snapshot: DataSnapshot){
                    var index = 1
                    var key = "message$index"

                    //START of WHILE-LOOP:
                    while (snapshot.hasChild(key)){
                        index++
                        key = "message$index"
                    }//END of WHILE-LOOP

                    val greeting = "You're getting fucking old ${user?.username}"

                    messageReference.child(key).setValue(Greeting(aiKey, date, time, censor(greeting), "true"))
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError) {}
                //END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION: writeGreeting

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
}//END of CLASS: Greeting