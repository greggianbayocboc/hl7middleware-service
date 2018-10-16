package com.hisd3.utils.Sockets

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.json.JSONObject
import jdk.nashorn.internal.objects.NativeArray.forEach



@WebSocket
class WSocketHandler {
    @OnWebSocketConnect
    fun connected(session: Session) = println("session connected")

    @OnWebSocketClose
    fun closed(session: Session, statusCode: Int, reason: String?) = println("closed sessions")

    @OnWebSocketMessage
    fun message(session: Session, message: String) = println("Got: $message")
}

