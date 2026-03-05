package com.example.palma.ai.tom

import com.example.palma.models.Annually
import com.example.palma.models.Daily
import com.example.palma.models.Message
import com.example.palma.models.Monthly
import com.example.palma.models.Weekly
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

//START of CLASS: Reminder
class Reminder{
    private val database = Firebase.database
    private val aiKey = "AI - 2"

    //START of FUNCTION: writeReminder
    fun writeReminder(userKey: String, messageKey: String, message: String){
        val list = message.lowercase().trim().split(" ")
        val commands = arrayOf("set", "delete")

        //START of IF-STATEMENT:
        if(list[1] in commands){
            //START of IF-STATEMENT:
            if(list[1] == "set"){
                setReminder(userKey, messageKey, message)
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(list[1] == "delete"){
                removeReminder(userKey, messageKey, message)
            }//END of IF-STATEMENT
        }//END of IF-STATEMENT

        //START of ELSE-STATEMENT:
        else{
            error(userKey, "command", messageKey, message)
        }//END of ELSE-STATEMENT
    }//END of FUNCTION: writeReminder

    //START of FUNCTION: alert
    fun alert(userKey: String, messageKey: String, interval: String, reminder: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        var message = ""

        userReference.get().addOnSuccessListener{ userSnapshot ->
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

                    //START of IF-STATEMENT:
                    if(interval == "hour"){
                        message = "Hour before $reminder..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(interval == "half-hour"){
                        message = "Half-hour before $reminder..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(interval == "soon"){
                        message = "Almost time for $reminder..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(interval == "now"){
                        message = reminder
                    }//END of IF-STATEMENT

                    messageReference.child(key).setValue(Message(aiKey, date, time, message))
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError){
                }//END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION: alert

    //START of FUNCTION: setReminder
    private fun setReminder(userKey: String, messageKey: String, message: String){
        val contactReference = database.getReference("Palma/User/$userKey/Contact")
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val reminderReference = messageReference.child("Reminder")
        val list = message.lowercase().trim().split(" ")
        val types = arrayOf("daily", "weekly", "monthly", "annually")
        val days = arrayOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")

        contactReference.get().addOnSuccessListener{ contactSnapshot ->
            //START of FOR-LOOP:
            for(child in contactSnapshot.children){
                val foundMessage = child.child("messageKey").getValue(String::class.java)

                //START of IF-STATEMENT:
                if(messageKey == foundMessage){
                    val contactKey = child.key

                    database.getReference("Palma/User/$userKey/Contact/$contactKey/Member").get()
                        .addOnSuccessListener { memberSnapshot ->
                            var cancel = "false"

                            //START of FOR-LOOP:
                            for(member in memberSnapshot.children){
                                val username = member.child("username").getValue(String::class.java)

                                //START of IF-STATEMENT:
                                if(username == "Palma"){
                                    cancel = "true"
                                    break
                                }//END of IF-STATEMENT
                            }//END of FOR-LOOP

                            //START of IF-STATEMENT:
                            if(cancel == "false"){
                                userReference.get().addOnSuccessListener{ userSnapshot ->
                                    reminderReference.get().addOnSuccessListener{ reminderSnapshot ->
                                        val type = list[2]
                                        var index = 1
                                        var reminderKey = "Reminder - $index"

                                        //START of WHILE-LOOP:
                                        while(reminderSnapshot.hasChild(reminderKey)){
                                            index++
                                            reminderKey = "Reminder - $index"
                                        }//END of WHILE-LOOP

                                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                                        var validTime = true

                                        //START of IF-STATEMENT:
                                        if(type in types){
                                            //START of IF-STATEMENT:
                                            if(types[0] == type){
                                                val time = list[3]

                                                //START of TRY-STATEMENT:
                                                try{
                                                    LocalTime.parse(time, timeFormatter)
                                                }//END of TRY-STATEMENT

                                                //START of CATCH-STATEMENT:
                                                catch (e: DateTimeParseException){
                                                    validTime = false
                                                }//END of CATCH-STATEMENT

                                                //START of IF-STATEMENT:
                                                if(validTime){
                                                    val reminder = list.subList(4, list.size).joinToString(" ")

                                                    reminderReference.child(reminderKey).setValue(Daily(userKey, type, time, reminder))
                                                    success(userKey, "set", messageKey, type, reminder)
                                                }//END of IF-STATEMENT

                                                //START of ELSE-STATEMENT:
                                                else{
                                                    error(userKey, "time", messageKey, message)
                                                }//END of ELSE-STATEMENT
                                            }//END of IF-STATEMENT

                                            //START of IF-STATEMENT:
                                            if(types[1] == type){
                                                val day = list[3]

                                                //START of IF-STATEMENT:
                                                if(day in days){
                                                    val time = list[4]

                                                    //START of TRY-STATEMENT:
                                                    try{
                                                        LocalTime.parse(time, timeFormatter)
                                                    }//END of TRY-STATEMENT

                                                    //START of CATCH-STATEMENT:
                                                    catch (e: DateTimeParseException){
                                                        validTime = false
                                                    }//END of CATCH-STATEMENT

                                                    //START of IF-STATEMENT:
                                                    if(validTime){
                                                        val reminder = list.subList(5, list.size).joinToString(" ")

                                                        reminderReference.child(reminderKey).setValue(Weekly(userKey, type, day, time, reminder))
                                                        success(userKey, "set", messageKey, type, reminder)
                                                    }//END of IF-STATEMENT

                                                    //START of ELSE-STATEMENT:
                                                    else{
                                                        error(userKey, "time", messageKey, message)
                                                    }//END of ELSE-STATEMENT
                                                }//END of IF-STATEMENT

                                                //START of ELSE-STATEMENT:
                                                else{
                                                    error(userKey, "day", messageKey, message)
                                                }//END of ELSE-STATEMENT
                                            }//END of IF-STATEMENT

                                            //START of IF-STATEMENT:
                                            if(types[2] == type){
                                                val date = list[3]

                                                //START of IF-STATEMENT:
                                                if(date.toIntOrNull() in 1..31){
                                                    val time = list[4]

                                                    //START of TRY-STATEMENT:
                                                    try{
                                                        LocalTime.parse(time, timeFormatter)
                                                    }//END of TRY-STATEMENT

                                                    //START of CATCH-STATEMENT:
                                                    catch (e: DateTimeParseException){
                                                        validTime = false
                                                    }//END of CATCH-STATEMENT

                                                    //START of IF-STATEMENT:
                                                    if(validTime){
                                                        val reminder = list.subList(5, list.size).joinToString(" ")

                                                        reminderReference.child(reminderKey).setValue(Monthly(userKey, type, date, time, reminder))
                                                        success(userKey, "set", messageKey, type, reminder)
                                                    }//END of IF-STATEMENT

                                                    //START of ELSE-STATEMENT:
                                                    else{
                                                        error(userKey, "time", messageKey, message)
                                                    }//END of ELSE-STATEMENT
                                                }//END of IF-STATEMENT

                                                //START of ELSE-STATEMENT:
                                                else{
                                                    error(userKey, "date", messageKey, message)
                                                }//END of ELSE-STATEMENT
                                            }//END of IF-STATEMENT

                                            //START of IF-STATEMENT:
                                            if(types[3] == type){
                                                val date = list[3]
                                                val parts = date.split("-")

                                                //START of IF-STATEMENT:
                                                if(parts.size == 2){
                                                    val month = parts[0].toIntOrNull()
                                                    val day = parts[1].toIntOrNull()

                                                    //START of IF-STATEMENT:
                                                    if((month != null && month in 1..12) && (day != null && day in 1..31)){
                                                        val time = list[4]

                                                        //START of TRY-STATEMENT:
                                                        try{
                                                            LocalTime.parse(time, timeFormatter)
                                                        }//END of TRY-STATEMENT

                                                        //START of CATCH-STATEMENT:
                                                        catch (e: DateTimeParseException){
                                                            validTime = false
                                                        }//END of CATCH-STATEMENT

                                                        //START of IF-STATEMENT:
                                                        if(validTime){
                                                            val reminder = list.subList(5, list.size).joinToString(" ")

                                                            reminderReference.child(reminderKey).setValue(Annually(userKey, type, date, time, reminder))
                                                            success(userKey, "set", messageKey, type, reminder)
                                                        }//END of IF-STATEMENT

                                                        //START of ELSE-STATEMENT:
                                                        else{
                                                            error(userKey, "time", messageKey, message)
                                                        }//END of ELSE-STATEMENT
                                                    }//END of IF-STATEMENT

                                                    //START of ELSE-STATEMENT:
                                                    else{
                                                        error(userKey, "date", messageKey, message)
                                                    }//END of ELSE-STATEMENT
                                                }//END of IF-STATEMENT

                                                //START of ELSE-STATEMENT:
                                                else{
                                                    error(userKey, "date", messageKey, message)
                                                }//END of ELSE-STATEMENT
                                            }//END of IF-STATEMENT
                                        }//END of IF-STATEMENT

                                        //START of ELSE-STATEMENT:
                                        else{
                                            error(userKey, "type", messageKey, message)
                                        }//END of ELSE-STATEMENT
                                    }
                                }
                            }//END of IF-STATEMENT
                        }
                }//END of IF-STATEMENT
            }//END of FOR-LOOP
        }

    }//END of FUNCTION: setReminder

    //START of FUNCTION: removeReminder
    private fun removeReminder(userKey: String, messageKey: String, message: String){
        val contactReference = database.getReference("Palma/User/$userKey/Contact")
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val reminderReference = messageReference.child("Reminder")
        val list = message.lowercase().trim().split(" ")
        val types = arrayOf("daily", "weekly", "monthly", "annually")
        val days = arrayOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        var validTime = true
        var found = false

        contactReference.get().addOnSuccessListener{ snapshot ->
            //START of FOR-LOOP:
            for(child in snapshot.children){
                val foundMessage = child.child("messageKey").getValue(String::class.java)

                //START of IF-STATEMENT:
                if(messageKey == foundMessage){
                    val contactKey = child.key

                    database.getReference("Palma/User/$userKey/Contact/$contactKey/Member").get()
                        .addOnSuccessListener { memberSnapshot ->
                            var cancel = "false"

                            //START of FOR-LOOP:
                            for(member in memberSnapshot.children){
                                val username = member.child("username").getValue(String::class.java)

                                //START of IF-STATEMENT:
                                if(username == "Palma"){
                                    cancel = "true"
                                    break
                                }//END of IF-STATEMENT
                            }//END of FOR-LOOP

                            //START of IF-STATEMENT:
                            if(cancel == "false"){
                                userReference.get().addOnSuccessListener{ userSnapshot ->
                                    reminderReference.get().addOnSuccessListener{ reminderSnapshot ->
                                        val type = list[2]

                                        //START of FOR-LOOP:
                                        for(rem in reminderSnapshot.children){
                                            //START of IF-STATEMENT:
                                            if(type in types){
                                                //START of IF-STATEMENT:
                                                if((types[0] == type) && (type == rem.child("type").getValue(String::class.java))){
                                                    val time = list[3]

                                                    //START of TRY-STATEMENT:
                                                    try{
                                                        LocalTime.parse(time, timeFormatter)
                                                    }//END of TRY-STATEMENT

                                                    //START of CATCH-STATEMENT:
                                                    catch (e: DateTimeParseException){
                                                        validTime = false
                                                    }//END of CATCH-STATEMENT

                                                    //START of IF-STATEMENT:
                                                    if((validTime) && (time == rem.child("time").getValue(String::class.java))){
                                                        val reminder = list.subList(4, list.size).joinToString(" ")

                                                        //START of IF-STATEMENT:
                                                        if(reminder == rem.child("reminder").getValue(String::class.java)){
                                                            //START of IF-STATEMENT:
                                                            if(userKey == rem.child("userKey").getValue(String::class.java)){
                                                                reminderReference.child(rem.key.toString()).removeValue()
                                                                success(userKey, "remove", messageKey, message, type)
                                                                found = true
                                                            }//END of IF-STATEMENT

                                                            //START of ELSE-STATEMENT:
                                                            else{
                                                                error(userKey, "userKey", messageKey, message)
                                                            }//END of ELSE-STATEMENT
                                                        }//END of IF-STATEMENT
                                                    }//END of IF-STATEMENT

                                                    //START of ELSE-STATEMENT:
                                                    else{
                                                        error(userKey, "time", messageKey, message)
                                                    }//END of ELSE-STATEMENT
                                                }//END of IF-STATEMENT

                                                //START of IF-STATEMENT:
                                                if((types[1] == type) && (type == rem.child("type").getValue(String::class.java))){
                                                    val day = list[3]

                                                    //START of IF-STATEMENT:
                                                    if((day in days) && (day == rem.child("day").getValue(String::class.java))){
                                                        val time = list[4]

                                                        //START of TRY-STATEMENT:
                                                        try{
                                                            LocalTime.parse(time, timeFormatter)
                                                        }//END of TRY-STATEMENT

                                                        //START of CATCH-STATEMENT:
                                                        catch (e: DateTimeParseException){
                                                            validTime = false
                                                        }//END of CATCH-STATEMENT

                                                        //START of IF-STATEMENT:
                                                        if((validTime) && (time == rem.child("time").getValue(String::class.java))){
                                                            val reminder = list.subList(5, list.size).joinToString(" ")

                                                            //START of IF-STATEMENT:
                                                            if(reminder == rem.child("reminder").getValue(String::class.java)){
                                                                //START of IF-STATEMENT:
                                                                if(userKey == rem.child("userKey").getValue(String::class.java)){
                                                                    reminderReference.child(rem.key.toString()).removeValue()
                                                                    success(userKey, "remove", messageKey, message, type)
                                                                    found = true
                                                                }//END of IF-STATEMENT

                                                                //START of ELSE-STATEMENT:
                                                                else{
                                                                    error(userKey, "userKey", messageKey, message)
                                                                }//END of ELSE-STATEMENT
                                                            }//END of IF-STATEMENT
                                                        }//END of IF-STATEMENT

                                                        //START of ELSE-STATEMENT:
                                                        else{
                                                            error(userKey, "time", messageKey, message)
                                                        }//END of ELSE-STATEMENT
                                                    }//END of IF-STATEMENT

                                                    //START of ELSE-STATEMENT:
                                                    else{
                                                        error(userKey, "day", messageKey, message)
                                                    }//END of ELSE-STATEMENT
                                                }//END of IF-STATEMENT

                                                //START of IF-STATEMENT:
                                                if((types[2] == type) && (type == rem.child("type").getValue(String::class.java))){
                                                    val date = list[3]

                                                    //START of IF-STATEMENT:
                                                    if((date.toIntOrNull() in 1..31) && (date == rem.child("date").getValue(String::class.java))){
                                                        val time = list[4]

                                                        //START of TRY-STATEMENT:
                                                        try{
                                                            LocalTime.parse(time, timeFormatter)
                                                        }//END of TRY-STATEMENT

                                                        //START of CATCH-STATEMENT:
                                                        catch (e: DateTimeParseException){
                                                            validTime = false
                                                        }//END of CATCH-STATEMENT

                                                        //START of IF-STATEMENT:
                                                        if((validTime) && (time == rem.child("time").getValue(String::class.java))){
                                                            val reminder = list.subList(5, list.size).joinToString(" ")

                                                            //START of IF-STATEMENT:
                                                            if(reminder == rem.child("reminder").getValue(String::class.java)){
                                                                //START of IF-STATEMENT:
                                                                if(userKey == rem.child("userKey").getValue(String::class.java)){
                                                                    reminderReference.child(rem.key.toString()).removeValue()
                                                                    success(userKey, "remove", messageKey, message, type)
                                                                    found = true
                                                                }//END of IF-STATEMENT

                                                                //START of ELSE-STATEMENT:
                                                                else{
                                                                    error(userKey, "userKey", messageKey, message)
                                                                }//END of ELSE-STATEMENT
                                                            }//END of IF-STATEMENT
                                                        }//END of IF-STATEMENT

                                                        //START of ELSE-STATEMENT:
                                                        else{
                                                            error(userKey, "time", messageKey, message)
                                                        }//END of ELSE-STATEMENT
                                                    }//END of IF-STATEMENT
                                                }//END of IF-STATEMENT

                                                //START of IF-STATEMENT:
                                                if((types[3] == type) && (type == rem.child("type").getValue(String::class.java))){
                                                    val date = list[3]
                                                    val parts = date.split("-")

                                                    //START of IF-STATEMENT:
                                                    if(parts.size == 2){
                                                        val month = parts[0].toIntOrNull()
                                                        val day = parts[1].toIntOrNull()

                                                        //START of IF-STATEMENT:
                                                        if((month != null && month in 1..12) && (day != null && day in 1..31)){
                                                            //START of IF-STATEMENT:
                                                            if(date == rem.child("date").getValue(String::class.java)){
                                                                val time = list[4]

                                                                //START of TRY-STATEMENT:
                                                                try{
                                                                    LocalTime.parse(time, timeFormatter)
                                                                }//END of TRY-STATEMENT

                                                                //START of CATCH-STATEMENT:
                                                                catch (e: DateTimeParseException){
                                                                    validTime = false
                                                                }//END of CATCH-STATEMENT

                                                                //START of IF-STATEMENT:
                                                                if((validTime) && (time == rem.child("time").getValue(String::class.java))){
                                                                    val reminder = list.subList(5, list.size).joinToString(" ")

                                                                    //START of IF-STATEMENT:
                                                                    if(reminder == rem.child("reminder").getValue(String::class.java)){
                                                                        //START of IF-STATEMENT:
                                                                        if(userKey == rem.child("userKey").getValue(String::class.java)){
                                                                            reminderReference.child(rem.key.toString()).removeValue()
                                                                            success(userKey, "remove", messageKey, message, type)
                                                                            found = true
                                                                        }//END of IF-STATEMENT

                                                                        //START of ELSE-STATEMENT:
                                                                        else{
                                                                            error(userKey, "userKey", messageKey, message)
                                                                        }//END of ELSE-STATEMENT
                                                                    }//END of IF-STATEMENT
                                                                }//END of IF-STATEMENT

                                                                //START of ELSE-STATEMENT:
                                                                else{
                                                                    error(userKey, "time", messageKey, message)
                                                                }//END of ELSE-STATEMENT
                                                            }//END of IF-STATEMENT
                                                        }//END of IF-STATEMENT

                                                        //START of ELSE-STATEMENT:
                                                        else{
                                                            error(userKey, "date", messageKey, message)
                                                        }//END of ELSE-STATEMENT
                                                    }//END of IF-STATEMENT

                                                    //START of ELSE-STATEMENT:
                                                    else{
                                                        error(userKey, "date", messageKey, message)
                                                    }//END of ELSE-STATEMENT
                                                }//END of IF-STATEMENT
                                            }//END of IF-STATEMENT

                                            //START of ELSE-STATEMENT:
                                            else{
                                                error(userKey, "type", messageKey, message)
                                            }//END of ELSE-STATEMENT
                                        }//END of FOR-LOOP

                                        //START of IF-STATEMENT:
                                        if(!found){
                                            error(userKey, "found", messageKey, message)
                                        }//END of IF-STATEMENT
                                    }
                                }
                            }//END of IF-STATEMENT
                        }
                }//END of IF-STATEMENT
            }//END of FOR-LOOP
        }

    }//END of FUNCTION: removeReminder

    //START of FUNCTION: success
    private fun success(userKey: String, command: String, messageKey: String, type: String, reminder: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        userReference.get().addOnSuccessListener{ userSnapshot ->
            messageReference.addListenerForSingleValueEvent(object : ValueEventListener{
                //START of FUNCTION: onDataChange
                override fun onDataChange(snapshot: DataSnapshot) {
                    var index = 1
                    var response = ""
                    var key = "message$index"

                    //START of WHILE-LOOP:
                    while(snapshot.hasChild(key)){
                        index++
                        key = "message$index"
                    }//END of WHILE-LOOP

                    //START of IF-STATEMENT:
                    if(command == "set"){
                        response = "I have successfully set your $type reminder for $reminder..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(command == "remove"){
                        response = "I have successfully removed your $type reminder for $reminder..."
                    }//END of IF-STATEMENT

                    messageReference.child(key).setValue(Message(aiKey, date, time, response))
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError) {
                }//END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION: success

    //START of FUNCTION: error
    private fun error(userKey: String, type: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        userReference.get().addOnSuccessListener{ userSnapshot ->
            messageReference.addListenerForSingleValueEvent(object : ValueEventListener{
                //START of FUNCTION: onDataChange
                override fun onDataChange(snapshot: DataSnapshot){
                    var index = 1
                    var response = ""
                    var key = "message$index"

                    //START of WHILE-LOOP:
                    while(snapshot.hasChild(key)){
                        index++
                        key = "message$index"
                    }//END of WHILE-LOOP

                    //START of IF-STATEMENT:
                    if(type == "command"){
                        response = "Unfortunately the command you have inputted from [$message] is invalid..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(type == "type"){
                        response = "Unfortunately the type you have inputted from [$message] is invalid..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(type == "time"){
                        response = "Unfortunately the time you have inputted from [$message] is invalid..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(type == "day"){
                        response = "Unfortunately the day you have inputted from [$message] is invalid..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(type == "date"){
                        response = "Unfortunately the date you have inputted from [$message] is invalid..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(type == "userKey"){
                        response = "Unfortunately I am not allowed to remove the reminder with an invalid user..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(type == "found"){
                        response = "Unfortunately the reminder you have requested is not found..."
                    }//END of IF-STATEMENT

                    messageReference.child(key).setValue(Message(aiKey, date, time, response))
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError){
                }//END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION: error
}//END of CLASS: Reminder