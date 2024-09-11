@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.maxklammer

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun client() {
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).tcp().connect("127.0.0.1", 9002)

        // Get the input stream from the socket for receiving data
        val receiveChannel = socket.openReadChannel()
        val sendChannel = socket.openWriteChannel(autoFlush = true)

        launch(Dispatchers.IO) {
            while (true) {
                // Buffer for receiving the data as a ByteArray
                val byteArray = ByteArray(1024) // Define the size of the buffer as needed

                // Read from the input stream into the byteArray
                val bytesRead = receiveChannel.readAvailable(byteArray, 0, byteArray.size)
                println(byteArray)

                // Check if data was read
                if (bytesRead > 0) {
                    println("Received $bytesRead bytes")
                    val message = byteArray.sliceArray(0 until bytesRead).toString(Charsets.UTF_8)
                    println(message)
                    if (message.endsWith("!")) {
                        println("Message end detected")
                    }
                } else {
                    println("Server closed a connection")
                    socket.close()
                    selectorManager.close()
                    exitProcess(0)
                }

                while (true) {
                    val myMessage = readln()
                    sendChannel.writeFully("$myMessage\n".toByteArray())
                }
            }
        }
    }
}
