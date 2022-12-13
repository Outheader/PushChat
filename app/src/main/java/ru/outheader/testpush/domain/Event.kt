package ru.outheader.testpush.domain

import ru.outheader.testpush.data.Status

class Event<out T>(val status: Status, val data: T?, val exception: Throwable?) {

    companion object {

        fun <T> loading(): Event<T> =
            Event(
                Status.LOADING,
                null,
                null
            )

        fun <T> success(data: T?): Event<T> =
            Event(
                Status.SUCCESS,
                data,
                null
            )

        fun <T>error(exception: Throwable?): Event<T> =
            Event(
                Status.ERROR,
                null,
                exception
            )
    }
}