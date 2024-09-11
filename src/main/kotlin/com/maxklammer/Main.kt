@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.maxklammer

object Server {
    @JvmStatic
    fun main(args: Array<String>) {
        server()
    }
}

object Client1 {
    @JvmStatic
    fun main(args: Array<String>) {
        client()
    }
}

object Client2 {
    @JvmStatic
    fun main(args: Array<String>) {
        client()
    }
}
