package com.hisd3.utils.Sockets

import java.io.IOException
import java.util.concurrent.CountDownLatch

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket

@WebSocket
class WebsocketClient {

    var session: Session? = null

    var latch =  CountDownLatch(1)

    @OnWebSocketMessage
    @Throws(IOException::class)
    fun onText(session: Session?, message: String) {
        println("Message received from server:\n $message")
    }

    @OnWebSocketConnect
    fun onConnect(session: Session) {
        println("Connected to server")
        this.session = session
        latch.countDown()
    }

    fun sendMessage(str: String) {
        try {
            println("Message: \n $str")
            session?.remote?.sendString(str)

        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

    }
    fun getLatch(){
        return latch.await()
    }



}