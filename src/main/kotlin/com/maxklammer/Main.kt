package com.maxklammer

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlin.system.exitProcess


const val CONCURRENCY = 1


object Main1 {
    @JvmStatic
    fun main(args: Array<String>) {
    server()
    }
}

object Main2 {
    @JvmStatic
    fun main(args: Array<String>) {
        client()
    }
}

object Main3 {
    @JvmStatic
    fun main(args: Array<String>) {
        client()
    }
}


fun server() {
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).tcp().bind("127.0.0.1", 9002)

        //semaphore to only accept a number of connections - the other connections will stay stalled
        val semaphore = Semaphore(CONCURRENCY)

        println("Server is listening at ${serverSocket.localAddress}")

        while (true) {
            val socket = serverSocket.accept()
            semaphore.acquire()
            println("Accepted $socket")
            launch {
                val receiveChannel = socket.openReadChannel()
                val sendChannel = socket.openWriteChannel(autoFlush = true)
                sendChannel.writeStringUtf8("Please enter your name\n")
                try {
                    while (true) {
                        val name = receiveChannel.readUTF8Line()

                        sendChannel.writeStringUtf8("Hello, $name!\n")
                    }
                } catch (e: Throwable) {
                    socket.close()
                    semaphore.release()
                }
            }
        }
    }
}


fun client() {
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).tcp().connect("127.0.0.1", 9002)

        val receiveChannel = socket.openReadChannel()
        val sendChannel = socket.openWriteChannel(autoFlush = true)

        launch(Dispatchers.IO) {
            while (true) {
                val greeting = receiveChannel.readUTF8Line()
                if (greeting != null) {
                    println(greeting)
                } else {
                    println("Server closed a connection")
                    socket.close()
                    selectorManager.close()
                    exitProcess(0)
                }
            }
        }

        while (true) {
            val myMessage = readln()
            sendChannel.writeStringUtf8("$myMessage\n")
        }
    }
}

