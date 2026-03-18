package com.example.palma.ai.pinky

import android.content.Context
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
    private val aiKey = "AI - 6"

    //START of FUNCTION: writeForecast
    fun writeForecast(context: Context, userKey: String, messageKey: String, message: String){
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
            Query().writeQuery(context, userKey, messageKey, message)
        }//END of ELSE-STATEMENT
    }//END of FUNCTION: writeForecast

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
                                        forecast = "It’s cuwwently ${weatherCode(code)}~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "It’s cuwwently ${temp}°C~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "It’s cuwwently ${weatherCode(code)} at ${temp}°C~"
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("now" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "It’s ${weatherCode(code)} wight now~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "It’s ${temp}°C wight now~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "It’s ${weatherCode(code)} at ${temp}°C wight now~"
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("today" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "It’s ${weatherCode(code)} today~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "It’s ${temp}°C today~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "It’s ${weatherCode(code)} at ${temp}°C today~"
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT
                            }//END of IF-STATEMENT

                            //START of ELSE-STATEMENT:
                            else {
                                forecast = "Unable to fetch current weather."
                            }//END of ELSE-STATEMENT

                            val message = Message(aiKey, date, time, forecast)
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
                                        forecast = "It was ${weatherCode(code)} in the past~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "It was ${temp}°C in the past~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "It was ${weatherCode(code)} at ${temp}°C in the past~"
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("yesterday" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "It was ${weatherCode(code)} yestewday~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "It was ${temp}°C yestewday~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "It was ${weatherCode(code)} at ${temp}°C yestewday~"
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("before" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "It was ${weatherCode(code)} befowe~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "It was ${temp}°C befowe~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "It was ${weatherCode(code)} at ${temp}°C befowe~"
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("ago" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "It was ${weatherCode(code)} ${days} days ago~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "It was ${temp}°C ${days} days ago~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "It was ${weatherCode(code)} at ${temp}°C ${days} days ago~"
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT
                            }//END of IF-STATEMENT

                            //START of ELSE-STATEMENT:
                            else{
                                forecast = "Unable to fetch past weather."
                            }//END of ELSE-STATEMENT:

                            val message = Message(aiKey, date, time, forecast)
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
                                        forecast = "It’ll be ${weatherCode(code)} in the futuwe~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "It’ll be ${temp}°C in the futuwe~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "It’ll be ${weatherCode(code)} at ${temp}°C in the futuwe~"
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("tomorrow" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "It’ll be ${weatherCode(code)} tomowwow~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "It’ll be ${temp}°C tomowwow~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "It’ll be ${weatherCode(code)} at ${temp}°C tomowwow~"
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if("later" in list){
                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" !in list)){
                                        forecast = "It’ll be ${weatherCode(code)} later~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if(("temperature" in list) && (("weather" !in list) && ("forecast" !in list))){
                                        forecast = "It’ll be ${temp}°C later~"
                                    }//END of IF-STATEMENT

                                    //START of IF-STATEMENT:
                                    if((("weather" in list) || ("forecast" in list)) && ("temperature" in list)){
                                        forecast = "It’ll be ${weatherCode(code)} at ${temp}°C later~"
                                    }//END of IF-STATEMENT
                                }//END of IF-STATEMENT
                            }//END of IF-STATEMENT

                            //START of ELSE-STATEMENT:
                            else{
                                forecast = "Unable to fetch future weather."
                            }//END of ELSE-STATEMENT

                            val message = Message(aiKey, date, time, forecast)
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
            0 -> "cweaw sky uwu"
            1, 2, 3 -> "pawtwy cwoudy"
            45, 48 -> "f-foggy >w<"
            51, 53, 55 -> "dwizzwe~"
            61, 63, 65 -> "wainy wain"
            71, 73, 75 -> "snuwu"
            80, 81, 82 -> "wain showews"
            95, 96, 99 -> "thundewstowm"
            else -> "unknowwn"
        }
    }//END of FUNCTION: weatherCode
}//END of CLASS: Forecast