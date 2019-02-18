package com.hisd3.utils.Sockets

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import java.io.IOException
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage



@WebSocket
class WebSocketServer {

    @OnWebSocketMessage
    @Throws(IOException::class)
    fun onText(session: Session, message: String) {
        println("Message received:$message")
        if (session.isOpen()) {
            val response = message.toUpperCase()
            session.getRemote().sendString(response)
        }
    }

    @OnWebSocketConnect
    @Throws(IOException::class)
    fun onConnect(session: Session) {
        println(session.getRemoteAddress().getHostString() + " connected!")
    }

    @OnWebSocketClose
    fun onClose(session: Session, status: Int, reason: String) {
        println(session.getRemoteAddress().getHostString() + " closed!")
    }
}