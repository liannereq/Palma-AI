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

//START of CLASS: Etiquette
class Etiquette{
    private val database = Firebase.database
    private val aiKey = "AI - 4"
    private val curseKey = setOf("fuck", "fucking", "fucked", "shit", "bullshit", "damn", "hell", "piss", "pissed", "screw", "jackass", "asshole", "douche", "prick", "bastard", "dumbass", "moron", "idiot", "jerk", "tool", "pussy", "chicken", "coward", "weakling", "spineless", "scaredy-cat", "dick", "dickhead", "bitch", "cunt", "motherfucker", "fucker", "crap", "freaking", "frick", "heck", "darn")

    //START of FUNCTION: writeEtiquette
    fun writeEtiquette(userKey: String, messageKey: String, message: String){
        val listMessage = message.lowercase().replace(Regex("[^a-z0-9\\s@]"), "").trim()
        val words = listMessage.split(Regex("\\s+"))

        val greetingWords = setOf("hello", "hi", "hey", "greetings")
        val goodWords = setOf("good", "morning", "afternoon", "evening", "night")
        val gratitudeWords = setOf("thank", "thanks")
        val farewellWords = setOf("bye", "goodbye", "later", "see", "take", "farewell")

        val isGreeting = words.any { it in greetingWords }
        val isGood = words.any { it in goodWords }
        val isGratitude = words.any { it in gratitudeWords }
        val isFarewell = words.any{ it in farewellWords }

        //START of IF-STATEMENT:
        if(isGreeting){
            writeGreeting(userKey, messageKey, message)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(isGood){
            writeGood(userKey, messageKey, message)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(isGratitude){
            writeWelcome(userKey, messageKey, message)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(isFarewell){
            writeFarewell(userKey, messageKey, message)
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeEtiquette

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

    //START of FUNCTION: writeGreeting
    private fun writeGreeting(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        val cleaned = message.lowercase().trim()
        val words = cleaned.split(Regex("\\s+"))

        val greetingWords = setOf("hello", "hi", "hey", "greetings")
        val groupWords = setOf("everyone", "all", "guys", "friends", "team")

        //START of IF-STATEMENT:
        if (words.isNotEmpty() && words[0] in greetingWords){
            val directedToMid = cleaned.contains("mid")
            val directedToGroup = words.any { it in groupWords }
            val singleWordGreeting = words.size == 1

            //START of IF-STATEMENT:
            if(directedToMid || directedToGroup || singleWordGreeting){
                userReference.get().addOnSuccessListener { snapshot ->
                    val user = snapshot.getValue(User::class.java)

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

                            val response = "Don't even fucking talk to me ${user?.username}"

                            messageReference.child(key).setValue(Message(aiKey, date, time, censor(response)))
                        }//END of FUNCTION: onDataChange

                        //START of FUNCTION: onCancelled
                        override fun onCancelled(error: DatabaseError) {}
                        //END of FUNCTION: onCancelled
                    })
                }
            }//END of IF-STATEMENT
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeGreeting

    //START of FUNCTION: writeGood
    private fun writeGood(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        val cleaned = message.lowercase().trim()
        val words = cleaned.split(Regex("\\s+"))

        val goodGreetings = mapOf(
            "morning" to "Go to hell",
            "afternoon" to "Seriously",
            "evening" to "Fuck you",
            "night" to "Better sleep with one eye open"
        )

        val groupWords = setOf("everyone", "all", "guys", "friends", "team")

        //START of IF-STATEMENT:
        if(words.isNotEmpty() && ((words[0] == "good" && words.size >= 2 && words[1] in goodGreetings.keys) || (words[0] in goodGreetings.keys))){
            val keyWord = if (words[0] == "good") words[1] else words[0]
            val baseGreeting = goodGreetings[keyWord] ?: "Good day"

            val directedToMid = cleaned.contains("mid")
            val directedToGroup = words.any { it in groupWords }
            val singleGreeting = words.size <= 2

            //START of IF-STATEMENT:
            if(directedToMid || directedToGroup || singleGreeting){
                userReference.get().addOnSuccessListener { snapshot ->
                    val user = snapshot.getValue(User::class.java)

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

                            val response = when {
                                keyWord == "night" -> "$baseGreeting ${user?.username}, sweet dreams"
                                else -> "$baseGreeting ${user?.username}..."
                            }

                            messageReference.child(key).setValue(Message(aiKey, date, time, censor(response)))
                        }//END of FUNCTION: onDataChange

                        //START of FUNCTION: onCancelled
                        override fun onCancelled(error: DatabaseError) {}
                        //END of FUNCTION: onCancelled
                    })
                }
            }//END of IF-STATEMENT
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeGood

    //START of FUNCTION: writeWelcome
    private fun writeWelcome(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        val cleaned = message.lowercase().trim()
        val words = cleaned.split(Regex("\\s+"))

        val thankWords = setOf("thank", "thanks")
        val groupWords = setOf("everyone", "all", "guys", "friends", "team")

        //START of IF-STATEMENT:
        if(words.isNotEmpty() && thankWords.any { words[0].startsWith(it) }){
            val directedToMid = cleaned.contains("mid")
            val directedToGroup = words.any { it in groupWords }
            val singleWordThanks = words.size == 1 || (words.size == 2 && words[0] in thankWords)

            //START of IF-STATEMENT:
            if(directedToMid || directedToGroup || singleWordThanks){
                userReference.get().addOnSuccessListener { snapshot ->
                    val user = snapshot.getValue(User::class.java)

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

                            val response = "${user?.username} is really useless without me huh???"

                            messageReference.child(key).setValue(Message(aiKey, date, time, censor(response)))
                        }//END of FUNCTION: onDataChange

                        //START of FUNCTION: onCancelled
                        override fun onCancelled(error: DatabaseError) {}
                        //END of FUNCTION: onCancelled
                    })
                }
            }//END of IF-STATEMENT
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeWelcome

    //START of FUNCTION: writeFarewell
    private fun writeFarewell(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        val cleaned = message.lowercase().trim()
        val words = cleaned.split(Regex("\\s+"))

        val farewellWords = setOf("bye", "goodbye", "later", "see", "take", "farewell")
        val groupWords = setOf("everyone", "all", "guys", "friends", "team")

        //START of IF-STATEMENT:
        if(words.isNotEmpty() && words.any { it in farewellWords }){
            val directedToMid = cleaned.contains("mid")
            val directedToGroup = words.any { it in groupWords }
            val singleFarewell = words.size <= 3

            //START of IF-STATEMENT:
            if(directedToMid || directedToGroup || singleFarewell){
                userReference.get().addOnSuccessListener { snapshot ->
                    val user = snapshot.getValue(User::class.java)

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

                            val response = "Don't fucking come back ${user?.username}..."

                            messageReference.child(key).setValue(Message(aiKey, date, time, censor(response)))
                        }//END of FUNCTION: onDataChange

                        //START of FUNCTION: onCancelled
                        override fun onCancelled(error: DatabaseError){
                        }//END of FUNCTION: onCancelled
                    })
                }
            }//END of IF-STATEMENT
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeFarewell
}//END of CLASS: Etiquette