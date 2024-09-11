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

    // Sending raw bytes
    val message1 = "Hello Client, I am the Server!\n".toByteArray()
    val message2 = "What is your name?\n".toByteArray()

    // Send raw byte arrays
    sendChannel.writeFully(message1)
    sendChannel.writeFully(message2)

    sendChannel.writeFully("\n".toByteArray())

    try {
        while (true) {
            val byteArray = ByteArray(1024)
            val bytesRead = receiveChannel.readAvailable(byteArray, 0, byteArray.size)

            val message = byteArray.sliceArray(0 until bytesRead).toString(Charsets.UTF_8)

            if (bytesRead > 0) {
                println("Received $bytesRead bytes")
                println("Received name: $message")
                val response = "Hey, ".toByteArray()

                if (message.endsWith("!\n")) {
                    println("MEssage end detected")
                    sendChannel.flush()

                    sendChannel.writeFully(response)
                }

                sendChannel.writeFully(response)
            }
        }
    } catch (e: Throwable) {
        socket.close()
        semaphore.release()
    }
}
