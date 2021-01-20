package com.ivo.ganev.awords.platform

/**
 * Whenever you get the [content], the event will become unhandled
 * */
class SingleEvent<out T>(val content: T) {
    var handled: Boolean = false

    /**
     * Returns the [content] if its unhandled.
     * */
    fun getUnhandled() : T?  {
        if(handled) return null
        handled = true
        return content
    }
}