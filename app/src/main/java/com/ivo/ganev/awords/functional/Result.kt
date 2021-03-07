package com.ivo.ganev.awords.functional

sealed class Result<out T, out U> {
    data class Success<out T>(val result: T) : Result<T, Nothing>()
    data class Failure<out U>(val failure: U) : Result<Nothing, U>()
}