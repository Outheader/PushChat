package ru.outheader.testpush.domain

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class UseCase<P, T> {

    private var parentJob = Job()
    private val jobForeground: CoroutineContext get() = parentJob + Dispatchers.Main
    private val jobBackground: CoroutineContext get() = parentJob + Dispatchers.IO

    abstract suspend fun executeOnBackground(params: P): T

    fun execute(
        params: P,
        onComplete: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        parentJob.cancel()
        parentJob = Job()


        CoroutineScope(jobForeground).launch {
            try {
                val result = withContext(jobBackground) {
                    executeOnBackground(params)
                }
                onComplete.invoke(result)
            } catch (e: Exception) {
                onError.invoke(e)
            }
        }
    }
}