package com.example.palma.ai.palma

import android.util.Log
import com.example.palma.ai.index.Index
import com.example.palma.ai.mid.Mid
import com.example.palma.ai.pinky.Pinky
import com.example.palma.ai.rin.Rin
import com.example.palma.ai.tom.Tom
import com.example.palma.models.Contact
import com.example.palma.models.Member
import com.example.palma.models.Message
import com.example.palma.models.User
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//START of CLASS: Contact
class Contact{
    private val database = Firebase.database
    private val aiKey = "AI - 1"
    private val listAI = arrayOf("Palma", "Tom", "Index", "Mid", "Rin", "Pinky")

    //START of FUNCTION: writeContact
    fun writeContact(userKey: String, messageKey: String, message: String){
        val list = message.lowercase().trim().split(" ")

        //START of IF-STATEMENT:
        if(list[1] == "write"){
            //START of IF-STATEMENT:
            if(list[2] == "ai"){
                writeAI(userKey, messageKey, message)
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(list[2] == "user"){
                writeUser(userKey, messageKey, message)
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(list[2] == "group"){
                writeGroup(userKey, messageKey, message)
            }//END of IF-STATEMENT
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(list[1] == "delete"){
            //START of IF-STATEMENT:
            if(list[2] == "ai"){
                deleteAI(userKey, messageKey, message)
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(list[2] == "user"){
                deleteUser(userKey, messageKey, message)
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(list[2] == "group"){
                deleteGroup(userKey, messageKey, message)
            }//END of IF-STATEMENT
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(list[1] == "add"){
            //START of IF-STATEMENT:
            if(list[2] == "ai"){
                addAI(userKey, messageKey, message)
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(list[2] == "user"){
                addUser(userKey, messageKey, message)
            }//END of IF-STATEMENT
        }//END of IF-STATEMENT

        //START of IF-STATEMENT:
        if(list[1] == "remove"){
            //START of IF-STATEMENT:
            if(list[2] == "ai"){
                removeAI(userKey, messageKey, message)
            }//END of IF-STATEMENT

            //START of IF-STATEMENT:
            if(list[2] == "user"){
                removeUser(userKey, messageKey, message)
            }//END of IF-STATEMENT
        }//END of IF-STATEMENT
    }//END of FUNCTION: writeContact

    //START of FUNCTION: writeAI
    private fun writeAI(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey")
        val list = message.trim().split(" ")

        userReference.get().addOnSuccessListener{ userSnapshot ->
            val user = userSnapshot.getValue(User::class.java)

            //START of IF-STATEMENT:
            if(list[3] in listAI){
                //START of IF-STATEMENT:
                if(list[3] == "Palma"){
                    Palma().writePalma(userKey, user?.username.toString()).addOnSuccessListener{success(userKey, "write", messageKey,"Palma")}
                }//END of IF-STATEMENT

                //START of IF-STATEMENT:
                if(list[3] == "Tom"){
                    Tom().writeTom(userKey, user?.username.toString()).addOnSuccessListener{success(userKey, "write", messageKey, "Tom")}
                }//END of IF-STATEMENT

                //START of IF-STATEMENT:
                if(list[3] == "Index"){
                    Index().writeIndex(userKey, user?.username.toString()).addOnSuccessListener{success(userKey, "write", messageKey, "Index")}
                }//END of IF-STATEMENT

                //START of IF-STATEMENT:
                if(list[3] == "Mid"){
                    Mid().writeMid(userKey, user?.username.toString()).addOnSuccessListener{success(userKey, "write", messageKey, "Mid")}
                }//END of IF-STATEMENT

                //START of IF-STATEMENT:
                if(list[3] == "Rin"){
                    Rin().writeRin(userKey, user?.username.toString()).addOnSuccessListener{success(userKey, "write", messageKey, "Rin")}
                }//END of IF-STATEMENT

                //START of IF-STATEMENT:
                if(list[3] == "Pinky"){
                    Pinky().writePinky(userKey, user?.username.toString()).addOnSuccessListener{success(userKey, "write", messageKey, "Pinky")}
                }//END of IF-STATEMENT
            }//END of IF-STATEMENT

            //START of ELSE-STATEMENT:
            else{
                error(userKey, "ai", messageKey, message)
            }//END of ELSE-STATEMENT
        }
    }//END of FUNCTION: writeAI

    //START of FUNCTION: writeUser
    private fun writeUser(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val contactReference = database.getReference("Palma/User/$userKey/Contact")
        val messageReference = database.getReference("Palma/Message")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val list = message.trim().split(" ")
        val email = list[3].trim()

        userReference.get().addOnSuccessListener{ userSnapshot ->
            val user = userSnapshot.getValue(User::class.java)

            database.getReference("Palma/User").get().addOnSuccessListener{ userSnapshot ->
                var userIndex = 0

                //START of FOR-LOOP:
                for(foundUser in userSnapshot.children){
                    val foundUsername = foundUser.child("Personal Information/username").getValue(String::class.java).toString()
                    val foundMobile = foundUser.child("Personal Information/mobile").getValue(String::class.java).toString()
                    val foundEmail = foundUser.child("Personal Information/email").getValue(String::class.java)
                    userIndex++

                    Log.d("found user", foundUsername)

                    //START of IF-STATEMENT:
                    if(email == foundEmail){
                        val foundContactReference = database.getReference("Palma/User/${foundUser.key}/Contact")

                        messageReference.get().addOnSuccessListener{ messageSnapshot ->
                            var messageIndex = 1
                            var foundMessageKey = "Message - $messageIndex"

                            //START of WHILE-LOOP:
                            while(messageSnapshot.hasChild(foundMessageKey)){
                                messageIndex++
                                foundMessageKey = "Message - $messageIndex"
                            }//END of WHILE-LOOP

                            foundContactReference.get().addOnSuccessListener{ foundContactSnapshot ->
                                var contactIndex = 1
                                var foundContactKey = "Contact - $contactIndex"

                                //START of WHILE-LOOP:
                                while(foundContactSnapshot.hasChild(foundContactKey)){
                                    contactIndex++
                                    foundContactKey = "Contact - $contactIndex"
                                }//END of WHILE-LOOP

                                foundContactReference.child(foundContactKey).setValue(Contact(foundMessageKey, user?.username.toString(), user?.mobile.toString(), user?.email.toString(), "user"))
                                messageReference.child("$foundMessageKey/message1").setValue(Message(aiKey, date, time, "${user?.username.toString()} would like to have a word"))

                                contactReference.get().addOnSuccessListener{ contactSnapshot ->
                                    contactIndex = 1
                                    var newContactKey = "Contact - $contactIndex"

                                    //START of WHILE-LOOP:
                                    while(contactSnapshot.hasChild(newContactKey)){
                                        contactIndex++
                                        newContactKey = "Contact - $contactIndex"
                                    }//END of WHILE-LOOP

                                    contactReference.child(newContactKey).setValue(Contact(foundMessageKey, foundUsername, foundMobile, foundEmail, "user"))

                                    success(userKey, "write", messageKey, foundUsername)
                                }
                            }
                        }

                        break
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if((email != foundEmail) && (userIndex == userSnapshot.childrenCount.toInt())){
                        error(userKey, "email", messageKey, message)
                    }//END of IF-STATEMENT
                }//END of FOR-LOOP
            }
        }
    }//END of FUNCTION: writeUser

    //START of FUNCTION: writeGroup
    private fun writeGroup(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val contactReference = database.getReference("Palma/User/$userKey/Contact")
        val messageReference = database.getReference("Palma/Message")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val list = message.trim().split(" ")
        val group = list[3].trim()

        userReference.get().addOnSuccessListener{ userSnapshot ->
            val user = userSnapshot.getValue(User::class.java)
            val username = user?.username.toString()
            val mobile = user?.mobile.toString()
            val email = user?.email.toString()

            messageReference.get().addOnSuccessListener{ messageSnapshot ->
                var messageIndex = 1
                var newMessageKey = "Message - $messageIndex"

                //START of WHILE-LOOP:
                while(messageSnapshot.hasChild(newMessageKey)){
                    messageIndex++
                    newMessageKey = "Message - $messageIndex"
                }//END of WHILE-LOOP

                contactReference.get().addOnSuccessListener{ contactSnapshot ->
                    var contactIndex = 1
                    var newContactKey = "Contact - $contactIndex"

                    //START of WHILE-LOOP:
                    while(contactSnapshot.hasChild(newContactKey)){
                        contactIndex++
                        newContactKey = "Contact - $contactIndex"
                    }//END of WHILE-LOOP

                    contactReference.child(newContactKey).setValue(Contact(newMessageKey, group, "", "", "group"))
                    contactReference.child("$newContactKey/Member/Member - 1").setValue(Member(username, mobile, email, "user"))
                    contactReference.child("$newContactKey/Member/Member - 2").setValue(Member("Palma", "00000", "palma@ai.com", "ai"))
                    messageReference.child("$newMessageKey/message1").setValue(Message(aiKey, date, time, "Welcome to $group"))

                    success(userKey, "write", messageKey, group)
                }
            }
        }
    }//END of FUNCTION: writeGroup

    //START of FUNCTION: deleteAI
    private fun deleteAI(userKey: String, messageKey: String, message: String){
        val contactReference = database.getReference("Palma/User/$userKey/Contact")
        val list = message.trim().split(" ")
        val delete = list[3].trim()

        //START of IF-STATEMENT:
        if((delete in listAI) && (delete != "Palma")){
            contactReference.get().addOnSuccessListener{ contactSnapshot ->
                var contactIndex = 0

                //START of FOR-LOOP:
                for(contact in contactSnapshot.children){
                    val foundUsername = contact.child("username").getValue(String::class.java).toString()
                    val foundType = contact.child("type").getValue(String::class.java)
                    contactIndex++
                    Log.d("found contact", foundUsername)

                    //START of IF-STATEMENT:
                    if((foundType == "ai") && (delete == foundUsername)){
                        val foundMessageKey = contact.child("messageKey").getValue(String::class.java)

                        database.getReference("Palma/Message/$foundMessageKey").removeValue()
                        contactReference.child(contact.key.toString()).removeValue().addOnSuccessListener{success(userKey, "delete", messageKey, delete)}

                        break
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(((foundType != "ai") && (delete != foundUsername)) && (contactIndex == contactSnapshot.childrenCount.toInt())){
                        error(userKey, "delete", messageKey, message)
                    }//END of IF-STATEMENT
                }//END of FOR-LOOP
            }
        }//END of IF-STATEMENT

        //START of ELSE-STATEMENT:
        else{
            error(userKey, "ai", messageKey, message)
        }//END of ELSE-STATEMENT
    }//END of FUNCTION: deleteAI

    //START of FUNCTION: deleteUser
    private fun deleteUser(userKey: String, messageKey: String, message: String) {
        val userReference = database.getReference("Palma/User/$userKey")
        val contactReference = database.getReference("Palma/User/$userKey/Contact")
        val messageReference = database.getReference("Palma/Message")
        val list = message.trim().split(" ")
        val delete = list[3].trim()

        userReference.get().addOnSuccessListener{ userSnapshot ->
            contactReference.get().addOnSuccessListener{ contactSnapshot ->
                var contactIndex = 0

                //START of FOR-LOOP:
                for(contact in contactSnapshot.children){
                    val foundEmail = contact.child("email").getValue(String::class.java).toString()
                    val foundType = contact.child("type").getValue(String::class.java)
                    val foundMessageKey = contact.child("messageKey").getValue(String::class.java)
                    contactIndex++

                    Log.d("found contact", foundEmail)

                    //START of IF-STATEMENT:
                    if((foundType == "user") && (foundEmail == delete)){
                        database.getReference("Palma/User").get().addOnSuccessListener{ foundUserSnapshot ->
                            var userIndex = 0

                            //START of FOR-LOOP:
                            for(foundUser in foundUserSnapshot.children){
                                val foundUsername = foundUser.child("Personal Information/username").getValue(String::class.java).toString()
                                val email = foundUser.child("Personal Information/email").getValue(String::class.java).toString()
                                userIndex++

                                //START of IF-STATEMENT:
                                if(email == delete){
                                    database.getReference("Palma/User/${foundUser.key}/Contact").get().addOnSuccessListener{ foundContactSnapshot ->
                                        contactIndex = 0

                                        //START of FOR-LOOP:
                                        for(foundContact in foundContactSnapshot.children){
                                            contactIndex = contactIndex++

                                            //START of IF-STATEMENT:
                                            if(foundMessageKey == foundContact.child("messageKey").getValue(String::class.java).toString()){
                                                database.getReference("Palma/User/${foundUser.key}/Contact/${foundContact.key}").removeValue()
                                                contactReference.child(contact.key.toString()).removeValue()
                                                messageReference.child(foundMessageKey).removeValue()

                                                success(userKey, "delete", messageKey, foundUsername)
                                                break
                                            }//END of IF-STATEMENT

                                            //START of IF-STATEMENT:
                                            if((foundMessageKey == foundContact.child("messageKey").getValue(String::class.java).toString()) && (contactIndex == foundContactSnapshot.childrenCount.toInt())){
                                                error(userKey, "email", messageKey, message)
                                            }//END of IF-STATEMENT
                                        }//END of FOR-LOOP
                                    }

                                    break
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if((email != delete) && (userIndex == foundUserSnapshot.childrenCount.toInt())){
                                    error(userKey, "email", messageKey, message)
                                }//END of IF-STATEMENT
                            }//END of FOR-LOOP
                        }

                        break
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if((foundEmail != delete) && (contactIndex == contactSnapshot.childrenCount.toInt())){
                        error(userKey, "email", messageKey, message)
                    }//END of IF-STATEMENT
                }//END of FOR-LOOP
            }
        }
    }//END of FUNCTION: deleteUser

    //START of FUNCTION: deleteGroup
    private fun deleteGroup(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User/$userKey/Personal Information")
        val contactReference = database.getReference("Palma/User/$userKey/Contact")
        val messageReference = database.getReference("Palma/Message")
        val list = message.trim().split(" ")
        val delete = list[3].trim()

        userReference.get().addOnSuccessListener{ userSnapshot ->
            val user = userSnapshot.getValue(User::class.java)
            val userEmail = user?.email

            contactReference.get().addOnSuccessListener{ contactSnapshot ->
                var contactIndex = 0

                //START of FOR-LOOP:
                for(contact in contactSnapshot.children){
                    val contactUsername = contact.child("username").getValue(String::class.java).toString()
                    val contactType = contact.child("type").getValue(String::class.java)
                    contactIndex++

                    Log.d("found contact", contactUsername)

                    //START of IF-STATEMENT:
                    if(contactType == "group"){
                        val contactMessageKey = contact.child("messageKey").getValue(String::class.java)

                        contactReference.child("${contact.key}/Member").get().addOnSuccessListener{ memberSnapshot ->
                            var memberIndex = 0

                            //START of FOR-LOOP:
                            for(member in memberSnapshot.children){
                                val memberEmail = member.child("email").getValue(String::class.java)
                                val memberType = member.child("type").getValue(String::class.java)
                                memberIndex++

                                //START of IF-STATEMENT:
                                if((memberType == "user") && (memberEmail != userEmail)){
                                    database.getReference("Palma/User").get().addOnSuccessListener{ foundUserSnapshot ->
                                        //START of FOR-LOOP:
                                        for(foundUser in foundUserSnapshot.children){
                                            val foundEmail = foundUser.child("Personal Information/email").getValue(String::class.java)

                                            //START of IF-STATEMENT:
                                            if(memberEmail == foundEmail){
                                                database.getReference("Palma/User/${foundUser.key}/Contact").get().addOnSuccessListener{ foundContactSnapshot ->
                                                    //START of FOR-LOOP:
                                                    for(foundContact in foundContactSnapshot.children){
                                                        val foundMessageKey = foundContact.child("messageKey").getValue(String::class.java)

                                                        //START of IF-STATEMENT:
                                                        if(contactMessageKey == foundMessageKey){
                                                            database.getReference("Palma/User/${foundUser.key}/Contact/${foundContact.key}").removeValue()
                                                        }//END of IF-STATEMENT
                                                    }//END of FOR-LOOP
                                                }
                                            }//END of IF-STATEMENT
                                        }//END of FOR-LOOP
                                    }
                                }//END of IF-STATEMENT

                                //START of IF-STATEMENT:
                                if(memberIndex == memberSnapshot.childrenCount.toInt()){
                                    messageReference.child(contactMessageKey.toString()).removeValue()
                                    contactReference.child(contact.key.toString()).removeValue()
                                    success(userKey, "delete", messageKey, delete)
                                }//END of IF-STATEMENT
                            }//END of FOR-LOOP
                        }

                        break
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(((contactType != "group") || (contactUsername != delete)) && (contactIndex == contactSnapshot.childrenCount.toInt())){
                        error(userKey, "group", messageKey, message)
                    }//END of IF-STATEMENT
                }//END of FOR-LOOP
            }
        }
    }//END of FUNCTION: deleteGroup

    //START of FUNCTION: addAI
    private fun addAI(userKey: String, messageKey: String, message: String){
        val contactReference = database.getReference("Palma/User/$userKey/Contact")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val list = message.trim().split(" ")
        val add = list[3].trim()

        contactReference.get().addOnSuccessListener{ snapshot ->
            //START of FOR-LOOP:
            for(child in snapshot.children){
                val foundMessage = child.child("messageKey").getValue(String::class.java)
                val foundType = child.child("type").getValue(String::class.java)

                //START of IF-STATEMENT:
                if(messageKey == foundMessage){

                    //START of IF-STATEMENT:
                    if(foundType == "group"){
                        val contactKey = child.key

                        database.getReference("Palma/User/$userKey/Contact/$contactKey/Member").get().addOnSuccessListener{ memberSnapshot ->
                            //START of FOR-LOOP:
                            for(member in memberSnapshot.children){
                                val type = member.child("type").getValue(String::class.java)
                                val email = member.child("email").getValue(String::class.java)

                                //START of IF-STATEMENT:
                                if(type == "user"){
                                    database.getReference("Palma/User").get().addOnSuccessListener{ userSnapshot ->
                                        var foundUserKey: String? = null

                                        //START of FOR-LOOP:
                                        for(user in userSnapshot.children){
                                            val personal = user.child("Personal Information")
                                            val foundEmail = personal.child("email").getValue(String::class.java)?.trim()

                                            //START of IF-STATEMENT:
                                            if(email == foundEmail){
                                                foundUserKey = user.key

                                                database.getReference("Palma/User/$foundUserKey/Contact").get().addOnSuccessListener { contactSnapshot ->
                                                    //START of FOR-LOOP:
                                                    for(contact in contactSnapshot.children){
                                                        val foundMessageKey = contact.child("messageKey").getValue(String::class.java)

                                                        //START of IF-STATEMENT:
                                                        if(messageKey == foundMessageKey){
                                                            var index = 1
                                                            var newMemberKey = "Member - $index"
                                                            val foundContactKey = contact.key.toString()
                                                            val group = contact.child("username").getValue(String::class.java)

                                                            //START of WHILE-LOOP:
                                                            while(memberSnapshot.hasChild(newMemberKey)){
                                                                index++
                                                                newMemberKey = "Member - $index"
                                                            }//END of WHILE-LOOP

                                                            //START of IF-STATEMENT:
                                                            if(add == "Palma"){
                                                                database.getReference("Palma/User/$foundUserKey/Contact/$foundContactKey/Member/$newMemberKey").setValue(Member("Palma", "00000", "palma@ai.com", "ai"))
                                                            }//END of IF-STATEMENT

                                                            //START of IF-STATEMENT:
                                                            if(add == "Tom"){
                                                                database.getReference("Palma/User/$foundUserKey/Contact/$foundContactKey/Member/$newMemberKey").setValue(Member("Tom", "11111", "tom@ai.com", "ai"))
                                                            }//END of IF-STATEMENT

                                                            //START of IF-STATEMENT:
                                                            if(add == "Index"){
                                                                database.getReference("Palma/User/$foundUserKey/Contact/$foundContactKey/Member/$newMemberKey").setValue(Member("Index", "33333", "index@ai.com", "ai"))
                                                            }//END of IF-STATEMENT

                                                            //START of IF-STATEMENT:
                                                            if(add == "Mid"){
                                                                database.getReference("Palma/User/$foundUserKey/Contact/$foundContactKey/Member/$newMemberKey").setValue(Member("Mid", "44444", "mid@ai.com", "ai"))
                                                            }//END of IF-STATEMENT

                                                            //START of IF-STATEMENT:
                                                            if(add == "Rin"){
                                                                database.getReference("Palma/User/$foundUserKey/Contact/$foundContactKey/Member/$newMemberKey").setValue(Member("Rin", "55555", "rin@ai.com", "ai"))
                                                            }//END of IF-STATEMENT

                                                            //START of IF-STATEMENT:
                                                            if(add == "Pinky"){
                                                                database.getReference("Palma/User/$foundUserKey/Contact/$foundContactKey/Member/$newMemberKey").setValue(Member("Pinky", "66666", "pinky@ai.com", "ai"))
                                                            }//END of IF-STATEMENT

                                                            messageReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                                                //START of FUNCTION: onDataChange
                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                    var index = 1
                                                                    var key = "message$index"

                                                                    //START of WHILE-LOOP:
                                                                    while(snapshot.hasChild(key)){
                                                                        index++
                                                                        key = "message$index"
                                                                    }//END of WHILE-LOOP

                                                                    val message = Message(aiKey, date, time, "I have successfully added $add to $group...")
                                                                    messageReference.child(key).setValue(message)
                                                                }//END of FUNCTION: onDataChange

                                                                //START of FUNCTION: onCancelled
                                                                override fun onCancelled(error: DatabaseError){
                                                                }//END of FUNCTION: onCancelled
                                                            })
                                                        }//END of IF-STATEMENT
                                                    }//END of FOR-LOOP
                                                }
                                            }//END of IF-STATEMENT
                                        }//END of FOR-LOOP
                                    }
                                }//END of IF-STATEMENT
                            }//END of FOR-LOOP
                        }
                    }//END of IF-STATEMENT
                }//END of IF-STATEMENT
            }//END of FOR-LOOP
        }
    }//END of FUNCTION: addAI

    //START of FUNCTION: addUser
    private fun addUser(userKey: String, messageKey: String, message: String){
        val contactReference = database.getReference("Palma/User/$userKey/Contact")
        val list = message.trim().split(" ")
        val add = list[3].trim()
        var addUsername = ""
        var isAdded = false

        database.getReference("Palma/User").get().addOnSuccessListener{ snapshot ->
            //START of FOR-LOOP:
            for(child in snapshot.children){
                val personal = child.child("Personal Information")
                val foundEmail = personal.child("email").getValue(String::class.java)
                addUsername = personal.child("username").getValue(String::class.java).toString()

                //START of IF-STATEMENT:
                if(add == foundEmail){
                    contactReference.get().addOnSuccessListener{ contactSnapshot ->
                        //START of FOR-LOOP:
                        for(contact in contactSnapshot.children){
                            //START of IF-STATEMENT:
                            if(contact.child("messageKey").getValue(String::class.java) == messageKey){

                                //START of IF-STATEMENT:
                                if(contact.child("type").getValue(String::class.java) == "group"){
                                    contactReference.child("${contact.key}/Member").get().addOnSuccessListener{ memberSnapshot ->
                                        var index = 1
                                        var newMemberKey = "Member - $index"

                                        //START of WHILE-LOOP:
                                        while(memberSnapshot.hasChild(newMemberKey)){
                                            index++
                                            newMemberKey = "Member - $index"
                                        }//END of WHILE-LOOP

                                        contactReference.child("${contact.key}/Member/$newMemberKey").setValue(Member(personal.child("username").getValue(String::class.java).toString(), personal.child("mobile").getValue(String::class.java).toString(), personal.child("email").getValue(String::class.java).toString(), "user"))

                                        database.getReference("Palma/User/${child.key}/Contact").get().addOnSuccessListener{ childSnapshot ->
                                            index = 1
                                            var foundContactKey = "Contact - $index"
                                            //START of WHILE-LOOP:
                                            while(childSnapshot.hasChild(foundContactKey)){
                                                index++
                                                foundContactKey = "Contact - $index"
                                            }//END of WHILE-LOOP

                                            val contactData = mapOf(
                                                "username" to contact.child("username").getValue(String::class.java),
                                                "messageKey" to messageKey,
                                                "mobile" to contact.child("mobile").getValue(String::class.java),
                                                "email" to contact.child("email").getValue(String::class.java),
                                                "type" to contact.child("type").getValue(String::class.java)
                                            )

                                            val newContactRef = database.getReference("Palma/User/${child.key}/Contact/$foundContactKey")
                                            newContactRef.setValue(contactData).addOnSuccessListener{
                                                contact.child("Member").children.forEachIndexed { i, member ->
                                                    val memberKey = "Member - ${i + 1}"
                                                    val memberData = member.getValue(Member::class.java)
                                                    newContactRef.child("Member/$memberKey").setValue(memberData)
                                                }
                                            }.addOnSuccessListener{isAdded = true}

                                            newContactRef.child("Member/$newMemberKey").setValue(Member(personal.child("username").getValue(String::class.java).toString(), personal.child("mobile").getValue(String::class.java).toString(), personal.child("email").getValue(String::class.java).toString(), "user"))
                                        }

                                        //START of FOR-LOOP:
                                        for(member in memberSnapshot.children){
                                            //START of FOR-LOOP:
                                            for(user in snapshot.children){
                                                val memberPersonal = user.child("Personal Information")
                                                val email = memberPersonal.child("email").getValue(String::class.java)

                                                //START of IF-STATEMENT:
                                                if(member.child("email").getValue(String::class.java) == email){
                                                    val memberKey = user.key

                                                    database.getReference("Palma/User/$memberKey/Contact").get().addOnSuccessListener{ newContactSnapshot ->
                                                        //START of FOR-LOOP:
                                                        for(newContact in newContactSnapshot.children){
                                                            //START of IF-STATEMENT:
                                                            if(newContact.child("messageKey").getValue(String::class.java) == messageKey){
                                                                val newContactKey = newContact.key

                                                                database.getReference("Palma/User/$memberKey/Contact/$newContactKey/Member/$newMemberKey").setValue(Member(personal.child("username").getValue(String::class.java).toString(), personal.child("mobile").getValue(String::class.java).toString(), personal.child("email").getValue(String::class.java).toString(), "user"))
                                                            }//END of IF-STATEMENT
                                                        }//END of FOR-LOOP
                                                    }

                                                    break
                                                }//END of IF-STATEMENT
                                            }//END of FOR-LOOP
                                        }//END of FOR-LOOP
                                    }
                                }//END of IF-STATEMENT

                                break
                            }//END of IF-STATEMENT
                        }//END of FOR-LOOP
                    }

                    break
                }//END of IF-STATEMENT
            }//END of FOR-LOOP

            //START of IF-STATEMENT:
            if(isAdded){
                success(userKey, "add", messageKey, addUsername)
            }//END of IF-STATEMENT

            //START of ELSE-STATEMENT:
            else{
                error(userKey, "email", messageKey, message)
            }//END of ELSE-STATEMENT
        }
    }//END of FUNCTION: addUser//ignore

    //START of FUNCTION: removeAI
    private fun removeAI(userKey: String, messageKey: String, message: String){
        val contactReference = database.getReference("Palma/User/$userKey/Contact")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val list = message.trim().split(" ")
        val remove = list[3].trim()

        //START of IF-STATEMENT:
        if(remove != "Palma"){
            contactReference.get().addOnSuccessListener{ snapshot ->
                //START of FOR-LOOP:
                for(child in snapshot.children){
                    val foundMessageKey = child.child("messageKey").getValue(String::class.java)

                    //START of IF-STATEMENT:
                    if(messageKey == foundMessageKey){
                        val contactKey = child.key
                        val type = child.child("type").getValue(String::class.java)

                        //START of IF-STATEMENT:
                        if(type == "group"){
                            database.getReference("Palma/User/$userKey/Contact/$contactKey/Member").get().addOnSuccessListener{ memberSnapshot ->
                                //START of FOR-LOOP:
                                for(member in memberSnapshot.children){
                                    val type = member.child("type").getValue(String::class.java)
                                    val email = member.child("email").getValue(String::class.java)

                                    //START of IF-STATEMENT:
                                    if(type == "user"){
                                        database.getReference("Palma/User").get().addOnSuccessListener{ userSnapshot ->
                                            //START of FOR-LOOP:
                                            for(user in userSnapshot.children){
                                                val personal = user.child("Personal Information")
                                                val foundEmail = personal.child("email").getValue(String::class.java)?.trim()

                                                //START of IF-STATEMENT:
                                                if(email == foundEmail){
                                                    val foundUserKey = user.key

                                                    database.getReference("Palma/User/$foundUserKey/Contact").get().addOnSuccessListener{ contactSnapshot ->
                                                        //START of FOR-LOOP:
                                                        for(contact in contactSnapshot.children){
                                                            val foundMessage = contact.child("messageKey").getValue(String::class.java)

                                                            //START of IF-STATEMENT:
                                                            if(messageKey == foundMessage){
                                                                val foundContactKey = contact.key.toString()
                                                                val group = contact.child("username").getValue(String::class.java)

                                                                database.getReference("Palma/User/$foundUserKey/Contact/$foundContactKey/Member").get().addOnSuccessListener{ snapshot ->
                                                                    //START of FOR-LOOP:
                                                                    for(child in snapshot.children){
                                                                        //START of IF-STATEMENT:
                                                                        if(child.child("username").getValue(String::class.java) == remove && child.child("type").getValue(String::class.java) == "ai"){
                                                                            database.getReference("Palma/User/$foundUserKey/Contact/$foundContactKey/Member/${child.key}").removeValue()

                                                                            messageReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                                                                //START of FUNCTION: onDataChange
                                                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                                                    var index = 1
                                                                                    var key = "message$index"

                                                                                    //START of WHILE-LOOP:
                                                                                    while(snapshot.hasChild(key)){
                                                                                        index++
                                                                                        key = "message$index"
                                                                                    }//END of WHILE-LOOP

                                                                                    val message = Message(aiKey, date, time, "I have successfully removed $remove from $group...")
                                                                                    messageReference.child(key).setValue(message)
                                                                                }//END of FUNCTION: onDataChange

                                                                                //START of FUNCTION: onCancelled
                                                                                override fun onCancelled(error: DatabaseError){
                                                                                }//END of FUNCTION: onCancelled
                                                                            })

                                                                            break
                                                                        }//END of IF-STATEMENT
                                                                    }//END of FOR-LOOP
                                                                }
                                                            }//END of IF-STATEMENT
                                                        }//END of FOR-LOOP
                                                    }
                                                }//END of IF-STATEMENT
                                            }//END of FOR-LOOP
                                        }
                                    }//END of IF-STATEMENT
                                }//END of FOR-LOOP
                            }
                        }//END of IF-STATEMENT
                    }//END of IF-STATEMENT
                }//END of FOR-LOOP
            }
        }//END of IF-STATEMENT
    }//END of FUNCTION: removeAI

    //START of FUNCTION: removeUser
    private fun removeUser(userKey: String, messageKey: String, message: String){
        val userReference = database.getReference("Palma/User")
        val contactReference = database.getReference("Palma/User/$userKey/Contact")
        val list = message.trim().split(" ")
        val remove = list[3].trim()
        var removeUsername = ""

        contactReference.get().addOnSuccessListener{ contactSnapshot ->
            //START of FOR-LOOP:
            for(contact in contactSnapshot.children){
                val foundMessageKey = contact.child("messageKey").getValue(String::class.java)
                val foundType = contact.child("type").getValue(String::class.java)
                Log.d("found contact", contact.child("username").getValue(String::class.java).toString())

                //START of IF-STATEMENT:
                if((foundType == "group") && (foundMessageKey == messageKey)){
                    Log.d("found group", contact.child("username").getValue(String::class.java).toString())

                    contactReference.child("${contact.key}/Member").get().addOnSuccessListener{ memberSnapshot ->
                        //START of FOR-LOOP:
                        for(member in memberSnapshot.children){
                            val memberUsername = member.child("username").getValue(String::class.java).toString()
                            val memberEmail = member.child("email").getValue(String::class.java).toString()
                            val memberType = member.child("type").getValue(String::class.java).toString()
                            Log.d("found member", memberUsername)

                            //START of IF-STATEMENT:
                            if(memberType == "user"){
                                userReference.get().addOnSuccessListener{ userSnapshot ->
                                    //START of FOR-LOOP:
                                    for(foundUser in userSnapshot.children){
                                        val foundUsername = foundUser.child("Personal Information/username").getValue(String::class.java).toString()
                                        val foundEmail = foundUser.child("Personal Information/email").getValue(String::class.java).toString()

                                        //START of IF-STATEMENT:
                                        if(foundEmail == memberEmail){
                                            Log.d("found user", foundUsername)

                                            database.getReference("Palma/User/${foundUser.key}/Contact").get().addOnSuccessListener{ foundSnapshot ->
                                                //START of FOR-LOOP:
                                                for(foundContact in foundSnapshot.children){
                                                    val foundKey = foundContact.child("messageKey").getValue(String::class.java).toString()

                                                    //START of IF-STATEMENT:
                                                    if(foundKey == foundMessageKey){
                                                        database.getReference("Palma/User/${foundUser.key}/Contact/${foundContact.key}/Member").get().addOnSuccessListener{ removeSnapshot ->
                                                            //START of FOR-LOOP:
                                                            for(found in removeSnapshot.children){
                                                                val email = found.child("email").getValue(String::class.java).toString()
                                                                val username = found.child("username").getValue(String::class.java).toString()

                                                                //START of IF-STATEMENT:
                                                                if(email == remove){
                                                                    database.getReference("Palma/User/${foundUser.key}/Contact/${foundContact.key}/Member/${found.key}").removeValue()
                                                                    Log.d("remove user", username)

                                                                    //START of IF-STATEMENT:
                                                                    if(memberEmail == remove){
                                                                        removeUsername = username
                                                                        database.getReference("Palma/User/${foundUser.key}/Contact/${foundContact.key}").removeValue()
                                                                    }//END of IF-STATEMENT

                                                                    break
                                                                }//END of IF-STATEMENT
                                                            }//END of FOR-LOOP
                                                        }
                                                    }//END of IF-STATEMENT
                                                }//END of FOR-LOOP
                                            }

                                            break
                                        }//END of IF-STATEMENT
                                    }//END of FOR-LOOP
                                }
                            }//END of IF-STATEMENT
                        }

                        //START of IF-STATEMENT:
                        if(removeUsername.isNotBlank()){
                            success(userKey, "remove", messageKey, removeUsername)
                        }//END of IF-STATEMENT

                        //START of ELSE-STATEMENT:
                        else{
                            error(userKey, "email", messageKey, message)
                        }//END of ELSE-STATEMENT
                    }

                    break
                }//END of IF-STATEMENT
            }//END of FOR-LOOP
        }
    }//END of FUNCTION: removeUser

    //START of FUNCTION: success
    private fun success(userKey: String, command: String, messageKey: String, name: String){
        val userReference = database.getReference("Palma/User/$userKey")
        val messageReference = database.getReference("Palma/Message/$messageKey")
        val current = LocalDateTime.now()
        val date = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = current.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        userReference.get().addOnSuccessListener{ userSnapshot ->
            messageReference.addListenerForSingleValueEvent(object: ValueEventListener{
                //START of FUNCTION: onDataChange
                override fun onDataChange(snapshot: DataSnapshot){
                    var index = 1
                    var message = ""
                    var key = "message$index"

                    //START of WHILE-LOOP:
                    while(snapshot.hasChild(key)){
                        index++
                        key = "message$index"
                    }//END of WHILE-LOOP

                    //START of IF-STATEMENT:
                    if(command == "write"){
                        message = "I have successfully added $name to your contact..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(command == "delete"){
                        message = "I have successfully removed $name from your contacts..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(command == "add"){
                        message = "I have successfully added $name to the group..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(command == "remove"){
                        message = "I have successfully removed $name to the group..."
                    }//END of IF-STATEMENT

                    messageReference.child(key).setValue(Message(aiKey, date, time, message))
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError){
                }//END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION: success

    //START of FUNCTION: error
    private fun error(userKey: String, type: String, messageKey: String, message: String){
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
                    var response = ""
                    var key = "message$index"

                    //START of WHILE-LOOP:
                    while(snapshot.hasChild(key)){
                        index++
                        key = "message$index"
                    }//END of WHILE-LOOP

                    //START of IF-STATEMENT:
                    if(type == "ai"){
                        response = "Unfortunately the ai you have inputted from [$message] is invalid..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(type == "group"){
                        response = "Unfortunately the group you have inputted from [$message] is invalid..."
                    }//END of IF-STATEMENT

                    //START of IF-STATEMENT:
                    if(type == "email"){
                        response = "Unfortunately the email you have inputted from [$message] is invalid..."
                    }//END of IF-STATEMENT

                    messageReference.child(key).setValue(Message(aiKey, date, time, response))
                }//END of FUNCTION: onDataChange

                //START of FUNCTION: onCancelled
                override fun onCancelled(error: DatabaseError){
                }//END of FUNCTION: onCancelled
            })
        }
    }//END of FUNCTION: error
}//END of CLASS: Contact