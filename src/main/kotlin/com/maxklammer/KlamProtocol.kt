package com.maxklammer

class KlamProtocol {
    fun process(stringBuilder: StringBuilder): String? {
        // Check if message contains the special character '!'
        if (stringBuilder.contains("!")) {
            val receivedName = stringBuilder.toString().trim()
            println("Received name: $receivedName")

            // Respond with the same name
            return receivedName
        }

        return null
    }
}
