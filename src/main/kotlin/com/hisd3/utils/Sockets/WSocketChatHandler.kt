package com.hisd3.utils.Sockets

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.*
import spark.Spark.*
import java.util.concurrent.atomic.AtomicLong

class User(val id: Long, val name: String)
class Message(val msgType: String, val data: Any)


@WebSocket
class WSocketChatHandler{

    val users = HashMap<Session, User>()
    var uids = AtomicLong(0)

    @OnWebSocketConnect
    fun connected(session: Session) {
        println("session connected")

    }
    @OnWebSocketMessage
    fun message(session: Session?, message: String) {
        println(message)
        val json = ObjectMapper().readTree(message)
        // {type: "join/say", data: "name/msg"}
        when (json.get("type").asText()) {
            "join" -> {
             val user = User(uids.getAndIncrement(), json.get("data").asText())
//                val user = User(uids.getAndIncrement(), session.remoteAddress.)
                users.put(session!!, user)
                // tell this user about all other users
                emit(session, Message("users", users.values))
                // tell all other users, about this user
                broadcastToOthers(session, Message("join", user))
            }
            "say" -> {
                broadcast(Message("say", json.get("data").asText()))
            }
            else ->{
                broadcast(Message("say",message))
            }
        }
        println("json msg ${message}")
    }


    @OnWebSocketClose
    fun disconnect(session: Session, code: Int, reason: String?) {
        // remove the user from our list
        val user = users.remove(session)
        // notify all other users this user has disconnected
        if (user != null) broadcast(Message("left", user))
    }


    fun emit(session: Session, message: Message) = session.remote.sendString(jacksonObjectMapper().writeValueAsString(message))
    fun broadcast(message: Message) = users.forEach() { emit(it.key, message) }
    fun broadcastToOthers(session: Session, message: Message) = users.filter { it.key != session }.forEach() { emit(it.key, message)}

}
//    private var sender: String? = null
//    private var msg: String? = null

//    @OnWebSocketConnect
//    @Throws(Exception::class)
//    fun onConnect(user: Session) {
//        val username = "User" + SocketMessenging().nextUserNumber++
//        SocketMessenging().Messenging(userUsernameMap.put(user, username))
//        SocketMessenging().broadcastMessage(sender = "Server", message = "$username joined the chat")
//    }
//
//    @OnWebSocketClose
//    fun onClose(user: Session, statusCode: Int, reason: String) {
//        val username = SocketMessenging().userUsernameMap.get(user)
//        SocketMessenging().userUsernameMap.  remove(user)
//        SocketMessenging().broadcastMessage(sender = "Server", message = username + " left the chat")
//    }
//
//    @OnWebSocketMessage
//    fun onMessage(user: Session, message: String) {
//        SocketMessenging().broadcastMessage(sender = SocketMessenging().userUsernameMap.get(user)!!, message = message)
//    }

