package com.hisd3.utils.Sockets

import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory
import org.eclipse.jetty.websocket.servlet.WebSocketServlet
import javax.servlet.annotation.WebServlet


@WebServlet(urlPatterns = arrayOf("/socketmessenging"))
class WebSocketServerlet : WebSocketServlet() {

    override fun configure(factory: WebSocketServletFactory) {

        factory.register(WebSocketServer::class.java)

    }

}