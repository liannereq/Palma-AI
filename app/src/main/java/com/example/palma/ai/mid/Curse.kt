package com.example.palma.ai.mid

import com.example.palma.models.Message
import com.example.palma.models.User
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//START of CLASS: Curse
class Curse{
    private val database = Firebase.database
    private val aiKey = "AI - 4"
    private val curseKey = setOf("fuck", "fucking", "fucked", "shit", "bullshit", "damn", "hell", "piss", "pissed", "screw", "jackass", "asshole", "douche", "prick", "bastard", "dumbass", "moron", "idiot", "jerk", "tool", "pussy", "chicken", "coward", "weakling", "spineless", "scaredy-cat", "dick", "dickhead", "bitch", "cunt", "motherfucker", "fucker", "crap", "freaking", "frick", "heck", "darn")

    //START of FUNCTION: writeCurse
    fun writeCurse(userKey: String, messageKey: String, message: String){
        val list = message.lowercase().replace(Regex("[^a-z0-9\\s@]"), "").trim().split(Regex("\\s+"))
        val anger = setOf("fuck", "fucking", "fucked", "shit", "bullshit", "damn", "hell", "piss", "pissed", "screw")
        val jerk = setOf("jackass", "asshole", "douche", "prick", "bastard", "dumbass", "moron", "idiot", "jerk", "tool")
        val coward = setOf("pussy", "chicken", "coward", "weakling", "spineless", "scaredy-cat")
        val crude = setOf("dick", "dickhead", "bitch", "cunt", "motherfucker", "fucker")
        val mild = setOf("crap", "freaking", "frick", "heck", "darn")

        //START of IF-STATEMENT:
        if(list.any{it in anger}){
            writeAnger(userKey, messageKey, message)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(list.any{it in jerk}){
            writeJerk(userKey, messageKey, message)
        }//END of IF-STATEMENT:

        //START of IF-STATEMENT:
        if(list.any{it in coward}){
            writeCoward(userKey, messageKey, message)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(list.any{it in crude}){
            writeCrude(userKey, messageKey, message)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(list.any{it in mild}){
            writeMild(userKey, messageKey, message)
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeCurse

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

    //START of FUNCTION: writeAnger
    private fun writeAnger(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val list = message.lowercase().replace(Regex("[^a-z0-9\\s@]"), "").trim().split(Regex("\\s+"))
        val anger = setOf("fuck", "fucking", "fucked", "shit", "bullshit", "damn", "hell", "piss", "pissed", "screw")
        val targeted = setOf("you", "mid", "@mid", "everyone", "all", "guys", "team", "folks")

        //START of IF-STATEMENT:
        if(list.any{it in targeted}){
            userReference.get().addOnSuccessListener{ userSnapshot ->
                val user = userSnapshot.getValue(User::class.java)
                val username = user?.username

                messageReference.addListenerForSingleValueEvent(object : ValueEventListener{
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
                        for(foundCurse in list){
                            //START of IF-STATEMENT:
                            if(foundCurse in anger){
                                val curse = setOf("$username, seriously $foundCurse!", "Wow $username, that was $foundCurse!", "I can't believe you just $foundCurse $username!", "Really, $username? $foundCurse!", "$username, only a $foundCurse would say that!")
                                val message = Message(aiKey, date, time, censor(curse.random()))
                                messageReference.child(key).setValue(message)
                                key = "message${index++}"
                            }//END of IF-STATEMENT
                        }//END of FOR-LOOP
                    }//END of FUNCTION: onDataChange

                    //START of FUNCTION: onCancelled
                    override fun onCancelled(error: DatabaseError){
                    }//END of FUNCTION: onCancelled
                })
            }
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeAnger

    //START of FUNCTION: writeJerk
    private fun writeJerk(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val list = message.lowercase().replace(Regex("[^a-z0-9\\s@]"), "").trim().split(Regex("\\s+"))
        val jerk = setOf("jackass", "asshole", "douche", "prick", "bastard", "dumbass", "moron", "idiot", "jerk", "tool")
        val targeted = setOf("you", "mid", "@mid", "everyone", "all", "guys", "team", "folks")

        //START of IF-STATEMENT:
        if((list.any{it in targeted}) || (message.lowercase().replace(Regex("[^a-z0-9\\s@]"), "").trim() in jerk)){
            userReference.get().addOnSuccessListener{ userSnapshot ->
                val user = userSnapshot.getValue(User::class.java)
                val username = user?.username

                messageReference.addListenerForSingleValueEvent(object : ValueEventListener{
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
                        for(foundCurse in list){
                            //START of IF-STATEMENT:
                            if(foundCurse in jerk){
                                val curse = setOf("$username you $foundCurse", "What a $foundCurse $username", "$username seriously $foundCurse", "Only a $foundCurse like you $username", "$username stop being such a $foundCurse", "You got some nerve calling me a $foundCurse $username")
                                val message = Message(aiKey, date, time, censor(curse.random()))
                                messageReference.child(key).setValue(message)
                                key = "message${index++}"
                            }//END of IF-STATEMENT
                        }//END of FOR-LOOP
                    }//END of FUNCTION: onDataChange

                    //START of FUNCTION: onCancelled
                    override fun onCancelled(error: DatabaseError){
                    }//END of FUNCTION: onCancelled
                })
            }
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeJerk

    //START of FUNCTION: writeCoward
    private fun writeCoward(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val list = message.lowercase().replace(Regex("[^a-z0-9\\s@]"), "").trim().split(Regex("\\s+"))
        val coward = setOf("pussy", "chicken", "coward", "weakling", "spineless", "scaredy-cat")
        val targeted = setOf("you", "mid", "@mid", "everyone", "all", "guys", "team", "folks")

        //START of IF-STATEMENT:
        if((list.any{it in targeted}) || (message.lowercase().replace(Regex("[^a-z0-9\\s@]"), "").trim() in coward)){
            userReference.get().addOnSuccessListener{ userSnapshot ->
                val user = userSnapshot.getValue(User::class.java)
                val username = user?.username

                messageReference.addListenerForSingleValueEvent(object : ValueEventListener{
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
                        for(foundCurse in list){
                            //START of IF-STATEMENT:
                            if(foundCurse in coward){
                                val curse = setOf("$username what a $foundCurse", "Stop being a $foundCurse $username", "$username always such a $foundCurse", "Look at you $username $foundCurse", "Don’t act like a $foundCurse $username", "Don't you dare call me a $foundCurse $username")
                                val message = Message(aiKey, date, time, censor(curse.random()))
                                messageReference.child(key).setValue(message)
                                key = "message${index++}"
                            }//END of IF-STATEMENT
                        }//END of FOR-LOOP
                    }//END of FUNCTION: onDataChange

                    //START of FUNCTION: onCancelled
                    override fun onCancelled(error: DatabaseError){
                    }//END of FUNCTION: onCancelled
                })
            }
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeCoward

    //START of FUNCTION: writeCrude
    private fun writeCrude(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val list = message.lowercase().replace(Regex("[^a-z0-9\\s@]"), "").trim().split(Regex("\\s+"))
        val crude = setOf("dick", "dickhead", "bitch", "cunt", "motherfucker", "fucker")
        val targeted = setOf("you", "mid", "@mid", "everyone", "all", "guys", "team", "folks")

        //START of IF-STATEMENT:
        if((list.any{it in targeted}) || (message.lowercase().replace(Regex("[^a-z0-9\\s@]"), "").trim() in crude)){
            userReference.get().addOnSuccessListener{ userSnapshot ->
                val user = userSnapshot.getValue(User::class.java)
                val username = user?.username

                messageReference.addListenerForSingleValueEvent(object : ValueEventListener{
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
                        for(foundCurse in list){
                            //START of IF-STATEMENT:
                            if(foundCurse in crude){
                                val curse = setOf("$username $foundCurse", "You $foundCurse $username", "$username that’s so $foundCurse", "Only a $foundCurse like $username would totally do that", "$username totally $foundCurse", "How dare you call me a $foundCurse $username")
                                val message = Message(aiKey, date, time, censor(curse.random()))
                                messageReference.child(key).setValue(message)
                                key = "message${index++}"
                            }//END of IF-STATEMENT
                        }//END of FOR-LOOP
                    }//END of FUNCTION: onDataChange

                    //START of FUNCTION: onCancelled
                    override fun onCancelled(error: DatabaseError){
                    }//END of FUNCTION: onCancelled
                })
            }
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeCrude

    //START of FUNCTION: writeMild
    private fun writeMild(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val list = message.lowercase().replace(Regex("[^a-z0-9\\s@]"), "").trim().split(Regex("\\s+"))
        val mild = setOf("crap", "freaking", "frick", "heck", "darn")
        val targeted = setOf("you", "mid", "@mid", "everyone", "all", "guys", "team", "folks")

        //START of IF-STATEMENT:
        if(list.any{it in targeted}){
            userReference.get().addOnSuccessListener{ userSnapshot ->
                val user = userSnapshot.getValue(User::class.java)
                val username = user?.username

                messageReference.addListenerForSingleValueEvent(object : ValueEventListener{
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
                        for(foundCurse in list){
                            //START of IF-STATEMENT:
                            if(foundCurse in mild){
                                val curse = setOf("$username $foundCurse", "$foundCurse $username", "$username what a $foundCurse mess", "Oh $foundCurse $username", "$username that’s $foundCurse ridiculous")
                                val message = Message(aiKey, date, time, censor(curse.random()))
                                messageReference.child(key).setValue(message)
                                key = "message${index++}"
                            }//END of IF-STATEMENT
                        }//END of FOR-LOOP
                    }//END of FUNCTION: onDataChange

                    //START of FUNCTION: onCancelled
                    override fun onCancelled(error: DatabaseError){
                    }//END of FUNCTION: onCancelled
                })
            }
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeMild
}//END of CLASS: Curse