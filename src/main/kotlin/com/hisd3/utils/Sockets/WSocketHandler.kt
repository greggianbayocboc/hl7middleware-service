package com.hisd3.utils.Sockets

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.atomic.AtomicLong


@WebSocket
class WSocketHandler {

    val users = HashMap<Session, User>()
    var uids = AtomicLong(0)

    @OnWebSocketConnect
        fun connected(session: Session) = println("session connected")

    @OnWebSocketClose
        fun onClose(session: Session, statusCode: Int, reason: String?) = println("closed sessions")

    @OnWebSocketMessage
    fun message(session: Session?, message: String) {
    val json = ObjectMapper().readTree(message)
    // {type: "join/say", data: "name/msg"}
    when (json.get("type").asText()) {
        "join" -> {
            val user = User(uids.getAndIncrement(), json.get("data").asText())
            users.put(session!!, user)
            // tell this user about all other users
            emit(session, Message("users", users.values))
            // tell all other users, about this user
            broadcastToOthers(session, Message("join", user))
        }
        "say" -> {
            broadcast(Message("say", json.get("data").asText()))
        }
    }
    println("json msg ${message}")
}


    fun emit(session: Session, message: Message) = session.remote.sendString(jacksonObjectMapper().writeValueAsString(message))
    fun broadcast(message: Message) = users.forEach() { emit(it.key, message) }
    fun broadcastToOthers(session: Session, message: Message) = users.filter { it.key != session }.forEach() { emit(it.key, message)}
}

class User(val id: Long, val name: String)
class Message(val msgType: String, val data: Any)
