package com.example.palma.ai.mid

import com.example.palma.models.Message
import com.example.palma.models.WeatherResponse
import com.example.palma.api.WeatherApiClient
import com.example.palma.models.FutureWeatherResponse
import com.example.palma.models.PastWeatherResponse
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//START of CLASS: Forecast
class Forecast{
    private val database = Firebase.database
    private val aiKey = "AI - 4"
    private val curseKey = setOf("fuck", "fucking", "fucked", "shit", "bullshit", "damn", "hell", "piss", "pissed", "screw", "jackass", "asshole", "douche", "prick", "bastard", "dumbass", "moron", "idiot", "jerk", "tool", "pussy", "chicken", "coward", "weakling", "spineless", "scaredy-cat", "dick", "dickhead", "bitch", "cunt", "motherfucker", "fucker", "crap", "freaking", "frick", "heck", "darn")

    //START of FUNCTION: writeForecast
    fun writeForecast(userKey: String, messageKey: String, message: String){
        val list = message.lowercase().replace(Regex("[^a-z0-9\\s]"), "").trim().split(Regex("\\s+"))
        val currentKey = setOf("current", "now", "today")
        val pastKey = setOf("past", "yesterday", "before", "ago")
        val futureKey = setOf("future", "tomorrow", "later")

        //START of IF-STATEMENT:
        if(list.any {it in currentKey}){
            currentForecast(userKey, messageKey, message)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        else if(list.any {it in pastKey}){
            pastForecast(userKey, messageKey, message)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        else if(list.any {it in futureKey}){
            futureForecast(userKey, messageKey, message)
        }//END of IF-STATEMENT

        //START of ELSE-STATEMENT:
        else{
            Query().writeQuery(userKey, messageKey, message)
        }//END of ELSE-STATEMENT
    }//END of FUNCTION: writeForecast

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

    //START of FUNCTION: currentForecast
    private fun currentForecast(userKey: String, messageKey: String, message: String){
        val list = message.lowercase().replace(Regex("[^a-z0-9\\s]"), "").trim().split(Regex("\\s+"))
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val now = LocalDateTime.now()
        val date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val lat = 14.5995
        val lon = 120.9842
        var forecast = ""

        //START of IF-STATEMENT:
        if(lat !in -90.0..90.0 || lon !in -180.0..180.0){
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

                        val message = Message(aiKey, date, time, "Invalid coordinates.")
                        messageReference.child(key).setValue(message)
                    }//END of FUNCTION: onDataChange

                    //START of FUNCTION: onCancelled
                    override fun onCancelled(error: DatabaseError){
                    }//END of FUNCTION: onCancelled
                })
            }
            return
        }//END of IF-STATEMENT

        userReference.get().addOnSuccessListener{
            messageReference.addListenerForSingleValueEvent(object : ValueEventListener{
                //START of FUNCTION: onDataChange
                override fun onDataChange(snapshot: DataSnapshot) {
                    var index = 1
                    var key = "message$index"

                    //START of WHILE-LOOP:
                    while (snapshot.hasChild(key)) {
                        index++
                        key = "message$index"
                    }//END of WHILE-LOOP

                    WeatherApiClient.retrofitService.getCurrentWeather(lat, lon, true).enqueue(object : Callback<WeatherResponse> {
                        //START of FUNCTION: onResponse
                        override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>){
                            //START of IF-STATEMENT:
                            if(response.isSuccessful && response.body() != null){
                                val body = response.body()!!
                                val temp = body.current_weather.temperature
                                val code = body.current_weather.weathercode

                                //START of IF-STATEMENT:
                                if("current" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "Its literally fucking ${weatherCode(code)}."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "Its literally fucking ${temp}°C."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "Its literally fucking ${weatherCode(code)} in ${temp}°C."
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("now" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "Its literally fucking ${weatherCode(code)} right now."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "Its literally fucking ${temp}°C right now."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "Its literally fucking ${weatherCode(code)} in ${temp}°C right now."
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("today" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "Its literally fucking ${weatherCode(code)} today."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "Its literally fucking ${temp}°C today."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "Its literally fucking ${weatherCode(code)} in ${temp}°C today."
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT
                            }//END of IF-STATEMENT

                            //START of ELSE-STATEMENT:
                            else {
                                forecast = "Unable to fetch current weather."
                            }//END of ELSE-STATEMENT

                            val message = Message(aiKey, date, time, censor(forecast))
                            messageReference.child(key).setValue(message)
                        }//END of FUNCTION: onResponse

                        //START of FUNCTION: onFailure
                        override fun onFailure(call: Call<WeatherResponse>, t: Throwable){
                            val message = Message(aiKey, date, time, "Failed to contact weather server.")
                            messageReference.child(key).setValue(message)
                        }//END of FUNCTION: onFailure
                    })
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError) {
                }//END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION: currentForecast

    //START of FUNCTION: pastForecast
    private fun pastForecast(userKey: String, messageKey: String, message: String){
        val list = message.lowercase().replace(Regex("[^a-z0-9\\s]"), "").trim().split(Regex("\\s+"))
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val now = LocalDateTime.now()
        val date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val words = message.lowercase().trim().split(" ")
        var days = words.firstOrNull { it.toIntOrNull() != null }?.toInt() ?: 1
        var forecast = ""

        //START of FOR-LOOP:
        for(word in words){
            days = when (word) {
                "yesterday" -> 1
                "past" -> 2
                "before" -> 3
                "ago" -> days
                else -> days
            }
        }//END of FOR-LOOP

        //START of IF-STATEMENT:
        if(days !in 1..5){
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

                        val message = Message(aiKey, date, time, "Sorry, I can only check up to 5 days in the past.")
                        messageReference.child(key).setValue(message)
                    }//END of FUNCTION: onDataChange

                    //START of FUNCTION: onCancelled
                    override fun onCancelled(error: DatabaseError){
                    }//END of FUNCTION: onCancelled
                })
            }
            return
        }//END of IF-STATEMENT

        val targetDate = now.minusDays(days.toLong())
        val lat = 14.5995
        val lon = 120.9842

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

                    WeatherApiClient.retrofitService.getPastWeather(lat, lon, targetDate.format(DateTimeFormatter.ISO_DATE), targetDate.format(DateTimeFormatter.ISO_DATE)).enqueue(object : Callback<PastWeatherResponse> {
                        //START of FUNCTION: onResponse
                        override fun onResponse(call: Call<PastWeatherResponse>, response: Response<PastWeatherResponse>){
                            //START of IF-STATEMENT:
                            if(response.isSuccessful && response.body() != null){
                                val daily = response.body()!!.daily
                                val temp = daily.temperature_2m_max.firstOrNull() ?: 0.0
                                val code = daily.weathercode.firstOrNull() ?: 0

                                //START of IF-STATEMENT:
                                if("past" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "There were fucking ${weatherCode(code)} in the past."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "There were fucking ${temp}°C in the past."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "There were fucking ${weatherCode(code)} at ${temp}°C in the past."
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("yesterday" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "There were fucking ${weatherCode(code)} yesterday."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "There were fucking ${temp}°C yesterday."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "There were fucking ${weatherCode(code)}, ${temp}°C yesterday."
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("before" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "There were fucking ${weatherCode(code)} before."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "There were fucking ${temp}°C before."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "There were fucking ${weatherCode(code)} at ${temp}°C before."
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("ago" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "There were fucking ${weatherCode(code)} $days days ago."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "There were fucking ${temp}°C $days days ago."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "There were fucking ${weatherCode(code)} at ${temp}°C $days days ago."
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT
                            }//END of IF-STATEMENT

                            //START of ELSE-STATEMENT:
                            else{
                                forecast = "Unable to fetch past weather."
                            }//END of ELSE-STATEMENT:

                            val message = Message(aiKey, date, time, censor(forecast))
                            messageReference.child(key).setValue(message)
                        }//END of FUNCTION: onResponse

                        //START of FUNCTION: onFailure
                        override fun onFailure(call: Call<PastWeatherResponse>, t: Throwable){
                            val message = Message(aiKey, date, time, "Failed to contact weather server.")
                            messageReference.child(key).setValue(message)
                        }//END of FUNCTION: onFailure
                    })
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError){
                }//END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION: pastForecast

    //START of FUNCTION: futureForecast
    private fun futureForecast(userKey: String, messageKey: String, message: String){
        val list = message.lowercase().replace(Regex("[^a-z0-9\\s]"), "").trim().split(Regex("\\s+"))
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val now = LocalDateTime.now()
        val date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val words = message.lowercase().trim().split(" ")
        var days = words.firstOrNull { it.toIntOrNull() != null }?.toInt() ?: 1
        var forecast = ""

        //START of FOR-LOOP:
        for(word in words){
            days = when (word){
                "tomorrow" -> 1
                "future" -> 2
                "later" -> 3
                else -> days
            }
        }//END of FOR-LOOP

        //START of IF-STATEMENT:
        if(days !in 1..5){
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

                        val message = Message(aiKey, date, time, "Sorry, I can only check up to 5 days in the future.")
                        messageReference.child(key).setValue(message)
                    }//END of FUNCTION: onDataChange

                    //START of FUNCTION: onCancelled
                    override fun onCancelled(error: DatabaseError){
                    }//END of FUNCTION: onCancelled
                })
            }
            return
        }//END of IF-STATEMENT

        val targetDate = now.plusDays(days.toLong())
        val lat = 14.5995
        val lon = 120.9842

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

                    WeatherApiClient.retrofitService.getFutureWeather(lat, lon, targetDate.format(DateTimeFormatter.ISO_DATE), targetDate.format(DateTimeFormatter.ISO_DATE)).enqueue(object : Callback<FutureWeatherResponse>{
                        //START of FUNCTION: onResponse
                        override fun onResponse(call: Call<FutureWeatherResponse>, response: Response<FutureWeatherResponse>){
                            //START of IF-STATEMENT:
                            if(response.isSuccessful && response.body() != null){
                                val daily = response.body()!!.daily
                                val temp = daily.temperature_2m_max.firstOrNull() ?: 0.0
                                val code = daily.weathercode.firstOrNull() ?: 0

                                //START of IF-STATEMENT:
                                if("future" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "Will be fucking ${weatherCode(code)} in  the future."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "Will be fucking ${temp}°C in  the future."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "Will be fucking ${weatherCode(code)} at ${temp}°C in  the future."
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("tomorrow" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "Will be fucking ${weatherCode(code)} tomorrow."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "Will be fucking ${temp}°C tomorrow."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "Will be fucking ${weatherCode(code)} at ${temp}°C tomorrow."
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("later" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "Will be fucking ${weatherCode(code)} later."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "Will be fucking ${temp}°C later."
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "Will be fucking ${weatherCode(code)} at ${temp}°C later."
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT
                            }//END of IF-STATEMENT

                            //START of ELSE-STATEMENT:
                            else{
                                forecast = "Unable to fetch future weather."
                            }//END of ELSE-STATEMENT

                            val message = Message(aiKey, date, time, censor(forecast))
                            messageReference.child(key).setValue(message)
                        }//END of FUNCTION: onResponse

                        //START of FUNCTION: onFailure
                        override fun onFailure(call: Call<FutureWeatherResponse>, t: Throwable){
                            val message = Message(aiKey, date, time, "Failed to contact weather server.")
                            messageReference.child(key).setValue(message)
                        }//END of FUNCTION: onFailure
                    })
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError){
                }//END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION: futureForecast

    //START of FUNCTION: weatherCode
    private fun weatherCode(code: Int): String {
        return when(code){
            0 -> "clear sky"
            1, 2, 3 -> "partly cloudy"
            45, 48 -> "fog"
            51, 53, 55 -> "drizzle"
            61, 63, 65 -> "rain"
            71, 73, 75 -> "snow"
            80, 81, 82 -> "rain showers"
            95, 96, 99 -> "thunderstorm"
            else -> "unknown"
        }
    }//END of FUNCTION: weatherCode
}//END of CLASS: Forecast