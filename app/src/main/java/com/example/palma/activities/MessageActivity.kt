package com.example.palma.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.palma.R
import com.example.palma.adapters.ContactAdapter
import com.example.palma.adapters.MessageAdapter
import com.example.palma.ai.AI
import com.example.palma.ai.palma.Greeting
import com.example.palma.ai.palma.Reminder
import com.example.palma.databinding.ActivityMessageBinding
import com.example.palma.models.Contact
import com.example.palma.models.Message
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.tasks.await
import java.time.Duration
import java.time.LocalTime

//START of CLASS: MessageActivity
class MessageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageBinding
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var messageAdapter: MessageAdapter
    private var contactList = ArrayList<Contact>()
    private var messageList = ArrayList<Message>()
    private val database = Firebase.database

    //START of FUNCTION: onCreate
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userKey = intent.getStringExtra("userKey")
        val contactKey = intent.getStringExtra("contactKey")

        //START of IF-STATEMENT:
        if(userKey != null && contactKey != null){
            setupRecyclerView(userKey)
            loadContact(userKey)
            loadMessage(userKey, contactKey)
            writeGreeting(userKey, contactKey)

            lifecycleScope.launch{
                //START of WHILE-LOOP:
                while(true){
                    //START of TRY-STATEMENT:
                    try{
                        findReminder(userKey, contactKey)
                    }//END of TRY-STATEMENT

                    //START of CATCH-STATEMENT:
                    catch(e: Exception){
                    }//END of CATCH-STATEMENT

                    delay(90000)
                }//END of WHILE-LOOP
            }

            binding.ImageContact.setOnClickListener {
                val intent = Intent(this@MessageActivity, ContactActivity::class.java)
                intent.putExtra("userKey", userKey)
                intent.putExtra("contactKey", contactKey)
                startActivity(intent)
            }

            binding.ButtSend.setOnClickListener {
                writeMessage(userKey, contactKey)
            }
        }//END of IF-STATEMENT
    }//END of FUNCTION: onCreate

    //START of FUNCTION: setupRecyclerView
    private fun setupRecyclerView(userKey: String){
        contactAdapter = ContactAdapter(this@MessageActivity, userKey, contactList)
        binding.ListContact.adapter = contactAdapter
        binding.ListContact.layoutManager = LinearLayoutManager(this@MessageActivity)

        messageAdapter = MessageAdapter(this@MessageActivity, userKey, messageList)
        binding.ListMessage.adapter = messageAdapter
        binding.ListMessage.layoutManager = LinearLayoutManager(this@MessageActivity)
        binding.ListMessage.scrollToPosition(messageAdapter.itemCount - 1)
    }//END of FUNCTION: setupRecyclerView

    //START of FUNCTION: loadContact
    private fun loadContact(userKey: String){
        val reference = database.getReference("Palma/User/$userKey/Contact")

        reference.addValueEventListener(object: ValueEventListener{
            //START of FUNCTION: onDataChange
            override fun onDataChange(snapshot: DataSnapshot){
                contactList.clear()

                //START of FOR-LOOP:
                for(contactSnapshot in snapshot.children){
                    val contact = contactSnapshot.getValue(Contact::class.java)

                    //START of IF-STATEMENT:
                    if(contact != null){
                        contactList.add(contact)
                    }//END of IF-STATEMENT
                }//END of FOR-LOOP

                contactAdapter.notifyDataSetChanged()
            }//END of FUNCTION: onDataChange

            //START of FUNCTION: onCancelled
            override fun onCancelled(error: DatabaseError){
            }//END of FUNCTION: onCancelled
        })
    }//END of FUNCTION: loadContact

    //START of FUNCTION: loadMessage
    private fun loadMessage(userKey: String, contactKey: String) {
        val reference = database.getReference("Palma/User/$userKey/Contact/$contactKey")

        reference.get().addOnSuccessListener{ snapshot ->
            binding.OutputContact.text = snapshot.child("username").getValue(String::class.java).toString()
            val messageKey = snapshot.child("messageKey").getValue(String::class.java).toString()
            val messageReference = database.getReference("Palma/Message/$messageKey")

            //START of IF-STATEMENT:
            if(snapshot.child("type").getValue(String::class.java).toString() == "ai"){
                loadAI(snapshot.child("username").getValue(String::class.java).toString())
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(snapshot.child("type").getValue(String::class.java).toString() == "user"){
                binding.LayoutHeader.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.secondary))
                binding.LayoutSend.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.secondary))
                binding.InputMessage.setHintTextColor(ContextCompat.getColor(this@MessageActivity, R.color.secondary))
                binding.InputMessage.setTextColor(ContextCompat.getColor(this@MessageActivity, R.color.secondary))
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(snapshot.child("type").getValue(String::class.java).toString() == "group"){
                binding.ImageContact.setImageResource(R.drawable.ic_group)
                binding.LayoutHeader.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.secondary))
                binding.LayoutSend.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.secondary))
                binding.InputMessage.setHintTextColor(ContextCompat.getColor(this@MessageActivity, R.color.secondary))
                binding.InputMessage.setTextColor(ContextCompat.getColor(this@MessageActivity, R.color.secondary))
            }//END of IF-STATEMENT

            messageReference.addValueEventListener(object: ValueEventListener{
                //START of FUNCTION: onDataChange
                override fun onDataChange(snapshot: DataSnapshot){
                    messageList.clear()
                    var index = 1

                    //START of WHILE-LOOP:
                    while(snapshot.hasChild("message$index")){
                        val messageSnapshot = snapshot.child("message$index")
                        val message = messageSnapshot.getValue(Message::class.java)

                        //START of IF-STATEMENT:
                        if (message == null){
                            index++
                            continue
                        }//END of IF-STATEMENT

                        messageList.add(message)
                        index++
                    }//WHILE of FOR-LOOP

                    messageAdapter.notifyDataSetChanged()
                    binding.ListMessage.scrollToPosition(messageAdapter.itemCount - 1)
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError){
                }//END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION: loadMessage

    //START of FUNCTION: loadAI
    private fun loadAI(ai: String){
        //START of IF-STATEMENT:
        if(ai == "Palma"){
            binding.ImageContact.setImageResource(R.drawable.ic_palma)
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(ai == "Tom"){
            binding.ImageContact.setImageResource(R.drawable.ic_tom)
            binding.LayoutHeader.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.purple))
            binding.LayoutSend.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.purple))
            binding.InputMessage.setHintTextColor(ContextCompat.getColor(this@MessageActivity, R.color.purple))
            binding.InputMessage.setTextColor(ContextCompat.getColor(this@MessageActivity, R.color.purple))
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(ai == "Index"){
            binding.ImageContact.setImageResource(R.drawable.ic_index)
            binding.LayoutHeader.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.blue))
            binding.LayoutSend.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.blue))
            binding.InputMessage.setHintTextColor(ContextCompat.getColor(this@MessageActivity, R.color.blue))
            binding.InputMessage.setTextColor(ContextCompat.getColor(this@MessageActivity, R.color.blue))
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(ai == "Mid"){
            binding.ImageContact.setImageResource(R.drawable.ic_mid)
            binding.LayoutHeader.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.orange))
            binding.LayoutSend.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.orange))
            binding.InputMessage.setHintTextColor(ContextCompat.getColor(this@MessageActivity, R.color.orange))
            binding.InputMessage.setTextColor(ContextCompat.getColor(this@MessageActivity, R.color.orange))
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(ai == "Rin"){
            binding.ImageContact.setImageResource(R.drawable.ic_rin)
            binding.LayoutHeader.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.red))
            binding.LayoutSend.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.red))
            binding.InputMessage.setHintTextColor(ContextCompat.getColor(this@MessageActivity, R.color.red))
            binding.InputMessage.setTextColor(ContextCompat.getColor(this@MessageActivity, R.color.red))
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(ai == "Pinky"){
            binding.ImageContact.setImageResource(R.drawable.ic_pinky)
            binding.LayoutHeader.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.pink))
            binding.LayoutSend.setBackgroundColor(ContextCompat.getColor(this@MessageActivity, R.color.pink))
            binding.InputMessage.setHintTextColor(ContextCompat.getColor(this@MessageActivity, R.color.pink))
            binding.InputMessage.setTextColor(ContextCompat.getColor(this@MessageActivity, R.color.pink))
        }//END of IF-STATEMENT
    }//END of FUNCTION: loadAI

    //START of FUNCTION: writeGreeting
    private fun writeGreeting(userKey: String, contactKey: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val contactReference = database.getReference("Palma/User/$userKey/Contact/$contactKey")
        val current = LocalDateTime.now()
        val currentDate = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val date = current.format(DateTimeFormatter.ofPattern("MM-dd"))
        var greeted = false

        userReference.get().addOnSuccessListener{ userSnapshot ->
            val birthdate = userSnapshot.child("birthdate").getValue(String::class.java)?.takeLast(5)

            contactReference.get().addOnSuccessListener{ contactSnapshot ->
                val messageKey = contactSnapshot.child("messageKey").getValue(String::class.java).toString()
                val messageReference = database.getReference("Palma/Message/$messageKey")
                val type = contactSnapshot.child("type").getValue(String::class.java)

                //START of IF-STATEMENT:
                if(birthdate == date){
                    messageReference.get().addOnSuccessListener{ messageSnapshot ->
                        //START of FOR-LOOP:
                        for(message in messageSnapshot.children){
                            val foundGreeting = message.child("greeting").getValue(String::class.java)
                            val foundDate = message.child("date").getValue(String::class.java)

                            //START of IF-STATEMENT:
                            if((foundGreeting == "true") && (currentDate == foundDate)){
                                greeted = true
                                break
                            }//END of IF-STATEMENT
                        }//END of FOR-LOOP

                        //START of IF-STATEMENT:
                        if((type == "ai") && !greeted){
                            val username = contactSnapshot.child("username").getValue(String::class.java)

                            //START of IF-STATEMENT:
                            if(username == "Palma"){
                                Greeting().writeGreeting(userKey, messageKey)
                            }//END of IF-STATEMENT

                            //START of IF-STATEMENT:
                            if(username == "Tom"){
                                com.example.palma.ai.tom.Greeting().writeGreeting(userKey, messageKey)
                            }//END of IF-STATEMENT

                            //START of IF-STATEMENT:
                            if(username == "Index"){
                                com.example.palma.ai.index.Greeting().writeGreeting(userKey, messageKey)
                            }//END of IF-STATEMENT

                            //START of IF-STATEMENT:
                            if(username == "Mid"){
                                com.example.palma.ai.mid.Greeting().writeGreeting(userKey, messageKey)
                            }//END of IF-STATEMENT

                            //START of IF-STATEMENT:
                            if(username == "Rin"){
                                com.example.palma.ai.rin.Greeting().writeGreeting(userKey, messageKey)
                            }//END of IF-STATEMENT

                            //START of IF-STATEMENT:
                            if(username == "Pinky"){
                                com.example.palma.ai.pinky.Greeting().writeGreeting(userKey, messageKey)
                            }//END of IF-STATEMENT
                        }//END of IF-STATEMENT

                        //START of IF-STATEMENT:
                        if((type == "group") && !greeted){
                            val memberReference = contactReference.child("Member")

                            memberReference.get().addOnSuccessListener{ memberSnapshot ->
                                //START of FOR-LOOP:
                                for(member in memberSnapshot.children){
                                    val foundType = member.child("type").getValue(String::class.java)

                                    //START of IF-STATEMENT:
                                    if(foundType == "ai"){
                                        val username = member.child("username").getValue(String::class.java)

                                        //START of IF-STATEMENT:
                                        if(username == "Palma"){
                                            Greeting().writeGreeting(userKey, messageKey)
                                        }//END of IF-STATEMENT

                                        //START of IF-STATEMENT:
                                        if(username == "Tom"){
                                            com.example.palma.ai.tom.Greeting().writeGreeting(userKey, messageKey)
                                        }//END of IF-STATEMENT

                                        //START of IF-STATEMENT:
                                        if(username == "Index"){
                                            com.example.palma.ai.index.Greeting().writeGreeting(userKey, messageKey)
                                        }//END of IF-STATEMENT

                                        //START of IF-STATEMENT:
                                        if(username == "Mid"){
                                            com.example.palma.ai.mid.Greeting().writeGreeting(userKey, messageKey)
                                        }//END of IF-STATEMENT

                                        //START of IF-STATEMENT:
                                        if(username == "Rin"){
                                            com.example.palma.ai.rin.Greeting().writeGreeting(userKey, messageKey)
                                        }//END of IF-STATEMENT

                                        //START of IF-STATEMENT:
                                        if(username == "Pinky"){
                                            com.example.palma.ai.pinky.Greeting().writeGreeting(userKey, messageKey)
                                        }//END of IF-STATEMENT
                                    }//END of IF-STATEMENT
                                }//END of FOR-LOOP
                            }
                        }//END of IF-STATEMENT
                    }
                }//END of IF-STATEMENT
            }
        }
    }//END of FUNCTION: writeGreeting

    //START of FUNCTION: findReminder
    private suspend fun findReminder(userKey: String, contactKey: String){
        val contactReference = database.getReference("Palma/User/$userKey/Contact/$contactKey")
        val current = LocalDateTime.now()
        val currentTime = current.toLocalTime()
        val contactSnapshot = contactReference.get().await()

        val messageKey = contactSnapshot.child("messageKey").getValue(String::class.java) ?: return
        val reminderReference = database.getReference("Palma/Message/$messageKey/Reminder")
        val reminderSnapshot = reminderReference.get().await()
        if (!reminderSnapshot.exists() || !reminderSnapshot.hasChildren()) return

        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        //START of FOR-LOOP:
        for(reminder in reminderSnapshot.children){

            val foundType = reminder.child("type").getValue(String::class.java) ?: continue
            val foundTime = reminder.child("time").getValue(String::class.java)
                ?.substring(0, 5)?.trim() ?: continue

            val targetTime = try {
                LocalTime.parse(foundTime, formatter)
            }catch(e: Exception){
                continue
            }

            val minutesUntil = Duration.between(currentTime, targetTime).toMinutes()
            val minutesAbs = kotlin.math.abs(minutesUntil)

            var interval = ""

            //START of IF-STATEMENT:
            if(minutesAbs <= 1){
                interval = "now"
            }//END of IF-STATEMENT

            //START of IF-STATEMENT
            if(minutesUntil == 5L){
                interval = "soon"
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if (minutesUntil == 15L){
                interval = "quarter-hour"
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(minutesUntil == 30L){
                interval = "half-hour"
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(minutesUntil == 45L){
                interval = "three-quarter-hour"
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(minutesUntil == 60L){
                interval = "hour"
            }//END of IF-STATEMENT

            val withinTarget = minutesAbs <= 1

            //START of IF-STATEMENT:
            if(foundType == "daily"){
                //START of IF-STATEMENT:
                if(withinTarget || (interval != "")){
                    writeReminder(userKey, contactKey, reminder.key!!, messageKey, interval)
                }//END of IF-STATEMENT
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(foundType == "weekly"){
                val foundDay = reminder.child("day").getValue(String::class.java)?.lowercase() ?: continue
                val currentDay = current.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase()

                //START of IF-STATEMENT:
                if((currentDay == foundDay) && (withinTarget || (interval != ""))){
                    writeReminder(userKey, contactKey, reminder.key!!, messageKey, interval)
                }//END of IF-STATEMENT
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(foundType == "monthly"){
                val foundDate = reminder.child("date").getValue(String::class.java)
                    ?.padStart(2, '0') ?: continue
                val currentDate = current.format(DateTimeFormatter.ofPattern("dd"))

                //START of IF-STATEMENT:
                if((currentDate == foundDate) && (withinTarget || (interval != ""))){
                    writeReminder(userKey, contactKey, reminder.key!!, messageKey, interval)
                }//END of IF-STATEMENT
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(foundType == "annually"){
                val foundDate = reminder.child("date").getValue(String::class.java) ?: continue
                val currentDate = current.format(DateTimeFormatter.ofPattern("MM-dd"))

                //START of IF-STATEMENT:
                if((currentDate == foundDate) && (withinTarget || (interval != ""))){
                    writeReminder(userKey, contactKey, reminder.key!!, messageKey, interval)
                }//END of IF-STATEMENT
            }//END of IF-STATEMENT
        }//END of FOR-LOOP
    }//END of FUNCTION: findReminder

    //START of FUNCTION: writeReminder
    private fun writeReminder(userKey: String, contactKey: String, reminderKey: String, messageKey: String, interval: String){
        val contactReference = database.getReference("Palma/User/$userKey/Contact/$contactKey")
        val reminderReference = database.getReference("Palma/Message/$messageKey/Reminder/$reminderKey")

        contactReference.get().addOnSuccessListener{ contactSnapshot ->
            reminderReference.get().addOnSuccessListener{ reminderSnapshot ->

                val reminder = reminderSnapshot.child("reminder").getValue(String::class.java) ?: return@addOnSuccessListener
                val contactType = contactSnapshot.child("type").getValue(String::class.java)

                //START of IF-STATEMENT:
                if(contactType == "ai"){
                    val foundUsername = contactSnapshot.child("username").getValue(String::class.java)

                    //START of IF-STATEMENT:
                    if((foundUsername == "Palma") && (interval in listOf("hour", "half-hour", "soon", "now"))){
                        Reminder().alert(userKey, messageKey, interval, reminder)
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if((foundUsername == "Tom") && (interval in listOf("hour", "half-hour", "soon", "now"))){
                        com.example.palma.ai.tom.Reminder().alert(userKey, messageKey, interval, reminder)
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if((foundUsername == "Rin") && (interval in listOf("hour","three-quarter-hour","half-hour","quarter-hour","soon","now"))){
                        com.example.palma.ai.rin.Reminder().alert(userKey, messageKey, interval, reminder)
                    }//END of IF-STATEMENT
                }//END of IF-STATEMENT

                //START of IF-STATEMENT:
                if(contactType == "group"){
                    CoroutineScope(Dispatchers.IO).launch{
                        val memberSnapshot = contactReference.child("Member").get().await()

                        //START of FOR-LOOP:
                        for(member in memberSnapshot.children){
                            val foundType = member.child("type").getValue(String::class.java)
                            val foundUsername = member.child("username").getValue(String::class.java)

                            //START of IF-STATEMENT:
                            if (foundType != "ai") continue

                            delay(600)

                            //START of IF-STATEMENT:
                            if((foundUsername == "Palma") && (interval in listOf("hour", "half-hour", "soon", "now"))){
                                Reminder().alert(userKey, messageKey, interval, reminder)
                            }//END of IF-STATEMENT

                            //START of IF-STATEMENT:
                            if((foundUsername == "Tom") && (interval in listOf("hour", "half-hour", "soon", "now"))){
                                com.example.palma.ai.tom.Reminder().alert(userKey, messageKey, interval, reminder)
                            }//END of IF-STATEMENT

                            //START of IF-STATEMENT:
                            if((foundUsername == "Rin") && (interval in listOf("hour","three-quarter-hour","half-hour", "quarter-hour","soon","now"))){
                                com.example.palma.ai.rin.Reminder().alert(userKey, messageKey, interval, reminder)
                            }//END of IF-STATEMENT
                        }//END of FOR-LOOP
                    }
                }//END of IF-STATEMENT
            }
        }
    }//END of FUNCTION: writeReminder

    //START of FUNCTION: writeMessage
    private fun writeMessage(userKey: String, contactKey: String){
        val reference = database.getReference("Palma/User/$userKey/Contact/$contactKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        reference.get().addOnSuccessListener{ snapshot ->
            val messageKey = snapshot.child("messageKey").getValue(String::class.java).toString()
            val type = snapshot.child("type").getValue(String::class.java).toString()
            val username = snapshot.child("username").getValue(String::class.java).toString()
            val messageReference = database.getReference("Palma/Message/$messageKey")

            messageReference.addListenerForSingleValueEvent(object: ValueEventListener{
                //START of FUNCTION: onDataChange
                override fun onDataChange(snapshot: DataSnapshot){
                    var index = 1
                    var key = "message$index"
                    val message = binding.InputMessage.text.toString()

                    //START of WHILE-LOOP:
                    while(snapshot.hasChild(key)){
                        index++
                        key = "message$index"
                    }//END of WHILE-LOOP

                    messageReference.child(key).setValue(Message(userKey, date, time, message)).addOnSuccessListener{binding.InputMessage.text.clear()}

                    //START of IF-STATEMENT:
                    if(type == "ai"){
                        AI().writeAI(this@MessageActivity, userKey, messageKey, username, message)
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(type == "group"){
                        CoroutineScope(Dispatchers.IO).launch{
                            try{
                                val memberSnapshot = reference.child("Member").get().await()

                                //START of FOR-LOOP:
                                for(member in memberSnapshot.children){
                                    //START of IF-STATEMENT:
                                    if(member.child("type").getValue(String::class.java) == "ai"){
                                        delay(6000)
                                        AI().writeAI(this@MessageActivity, userKey, messageKey, member.child("username").getValue(String::class.java).toString(), message)
                                    }//END of IF-STATEMENT
                                }//END of FOR-LOOP
                            } catch (e: Exception){
                            }
                        }
                    }//END of IF-STATEMENT
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError){
                }//END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION: writeMessage
}//END of CLASS: MessageActivity