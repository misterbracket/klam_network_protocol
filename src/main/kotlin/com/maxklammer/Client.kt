@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.maxklammer

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun client() {
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).tcp().connect("127.0.0.1", 9002)

        // Get the input stream from the socket for receiving data
        val receiveChannel = socket.openReadChannel()
        val sendChannel = socket.openWriteChannel(autoFlush = true)

        // Coroutine for receiving data from the server
        launch(Dispatchers.IO) {
            while (true) {
                val byteArray = ByteArray(1024) // Buffer for receiving the data
                val bytesRead = receiveChannel.readAvailable(byteArray, 0, byteArray.size)

                if (bytesRead > 0) {
                    val message = byteArray.sliceArray(0 until bytesRead).toString(Charsets.UTF_8)
                    println("Received from server: $message")
                }
            }
        }

        // Coroutine for sending data to the server
        launch(Dispatchers.IO) {
            while (true) {
                val myMessage = readln() // Read input from the user
                sendChannel.writeFully(myMessage.toByteArray())
            }
        }
    }
}
