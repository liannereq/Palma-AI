package com.example.palma.ai.TensorFlow

import java.time.LocalTime
import java.time.format.DateTimeFormatter

//START of CLASS: Build
class Build{
    private var command = "#"
    private val listType = setOf("command", "contact", "reminder", "new", "load", "delete", "add", "remove")
    private val reminderType = setOf("set", "delete")
    private val reminderSub = setOf("daily", "weekly", "monthly", "annually")
    private val reminderDay = setOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")
    private val reminderMonth = setOf("january", "february", "march", "april", "june", "july", "august", "september", "october", "november", "december")
    private val inputFormatter = DateTimeFormatter.ofPattern("h:m a")
    private val outputFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val contactType = setOf("write", "delete", "add", "remove")
    private val contactSub = setOf("ai", "user", "group")
    private val contactAI = setOf("palma", "tom", "index", "mid", "rin", "pinky")

    // START of FUNCTION: command
    fun command(classification: String, prompt: String): String{
        val list = prompt.lowercase().split(" ")
        command += classification

        //START of IF-STATEMENT:
        if(classification == "list"){
            val listIndex = list.indexOf("list")
            val name = if(listIndex > 0) list[listIndex - 1] else "Unknown"

            command += " ${list.filter{it in listType}.joinToString(" ")} $name"

            //START of IF-STATEMENT:
            if(list.contains("new")){
                //START of IF-STATEMENT:
                if(list.contains("private")){
                    command += " private"
                }//END of IF-STATEMENT

                //START of ELSE-STATEMENT:
                else{
                    command += " public"
                }//END of IF-STATEMENT
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if((list.contains("add")) || (list.contains("remove"))){
                command += " ${list.filter{it in name}.joinToString(" ")}"
            }//END of IF-STATEMENT
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(classification == "reminder"){
            command += " ${list.filter{it in reminderType}.joinToString(" ")} ${list.filter{it in reminderSub}.joinToString(" ")}"

            //START of IF-STATEMENT:
            if(list.contains("daily")){
                val foundTime = list.windowed(2, 1).firstOrNull{(_, second) -> second.equals("AM", true) || second.equals("PM", true)}?.joinToString(" ") ?: "00:00 AM"
                val time = LocalTime.parse(foundTime.uppercase(), inputFormatter).format(outputFormatter)

                command += " $time"
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(list.contains("weekly")){
                val foundTime = list.windowed(2, 1).firstOrNull{(_, second) -> second.equals("AM", true) || second.equals("PM", true)}?.joinToString(" ") ?: "00:00 AM"
                val time = LocalTime.parse(foundTime.uppercase(), inputFormatter).format(outputFormatter)

                command += " ${list.filter{it in reminderDay}.joinToString(" ")} $time"
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(list.contains("monthly")){
                val day = list.firstOrNull{it.toIntOrNull() in 1..31}?.toInt()?.let {String.format("%02d", it)} ?: "00"
                val foundTime = list.windowed(2, 1).firstOrNull{(_, second) -> second.equals("AM", true) || second.equals("PM", true)}?.joinToString(" ") ?: "00:00 AM"
                val time = LocalTime.parse(foundTime.uppercase(), inputFormatter).format(outputFormatter)

                command += " $day $time"
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(list.contains("annually")){
                val month = list.firstOrNull{it in reminderMonth}?.let{String.format("%02d", reminderMonth.indexOf(it) + 1)} ?: "00"
                val day = list.firstOrNull{it.toIntOrNull() in 1..31}?.toInt()?.let {String.format("%02d", it)} ?: "00"
                val foundTime = list.windowed(2, 1).firstOrNull{(_, second) -> second.equals("AM", true) || second.equals("PM", true)}?.joinToString(" ") ?: "00:00 AM"
                val time = LocalTime.parse(foundTime.uppercase(), inputFormatter).format(outputFormatter)

                command += " $month-$day $time"
            }//END of IF-STATEMENT
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(classification == "contact"){
            command += " ${list.filter{it in contactType}.joinToString(" ")} ${list.filter{it in contactSub}.joinToString(" ")}"

            //START of IF-STATEMENT:
            if(list.contains("ai")){
                command += " ${list.filter{it in contactAI}.joinToString(" ")}"
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(list.contains("user")){
                val emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
                val email = list.firstOrNull{it.matches(emailRegex)}

                command += " $email"
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(list.contains("group")){
                command += " ${list.filter{it in contactType}.filter{it in contactSub}.joinToString(" ")}"
            }//END of IF-STATEMENT
        }//END of IF-STATEMENT

        return command
    }//END of FUNCTION: command
}//END of CLASS: Build