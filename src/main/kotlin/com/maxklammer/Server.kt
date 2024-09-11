@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.maxklammer

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore

const val CONCURRENCY = 2

fun server() {
    runBlocking {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).tcp().bind("127.0.0.1", 9002)

        // semaphore to only accept a number of connections - the other connections will stay stalled
        val semaphore = Semaphore(CONCURRENCY)

        println("Server is listening at ${serverSocket.localAddress}")

        while (true) {
            val socket = serverSocket.accept()
            semaphore.acquire()
            println("Accepted $socket")
            launch {
                handleClient(socket, semaphore)
            }
        }
    }
}

suspend fun handleClient(
    socket: Socket,
    semaphore: Semaphore,
) {
    // Creating Sending and reading Channels
    val sendChannel = socket.openWriteChannel(autoFlush = true)
    val receiveChannel = socket.openReadChannel()

    // Sending initial messages
    val message1 = "Hello Client, I am the Server!\n".toByteArray()
    val message2 = "What is your name? End your name with '!' to send.\n".toByteArray()

    // Send raw byte arrays
    sendChannel.writeFully(message1)
    sendChannel.writeFully(message2)

    sendChannel.writeFully("\n".toByteArray())

    try {
        val byteArray = ByteArray(1024) // Buffer for reading incoming data
        val stringBuilder = StringBuilder() // Accumulate client message

        while (true) {
            val bytesRead = receiveChannel.readAvailable(byteArray, 0, byteArray.size)
            if (bytesRead > 0) {
                // Convert bytes to string and append to the message buffer
                val messagePart = byteArray.sliceArray(0 until bytesRead).toString(Charsets.UTF_8)
                stringBuilder.append(messagePart)

                println("Received $bytesRead bytes")
                println("Received messagePart: $messagePart")

                // Check if message contains the special character '!'
                if (stringBuilder.contains("!")) {
                    val receivedName = stringBuilder.toString().trim()
                    println("Received name: $receivedName")

                    // Respond with the same name
                    val response = "Hey, $receivedName".toByteArray()
                    sendChannel.writeFully(response)

                    // Reset the StringBuilder for the next message
                    stringBuilder.clear()
                }
            }
        }
    } catch (e: Throwable) {
        println("An error occurred: $e")
    } finally {
        // Always close the socket and release the semaphore
        socket.close()
        semaphore.release()
    }
}
