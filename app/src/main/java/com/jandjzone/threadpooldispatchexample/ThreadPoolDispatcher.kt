package com.jandjzone.threadpooldispatchexample

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object ThreadPoolDispatcher {

    private val workQueue: BlockingQueue<Runnable> = LinkedBlockingQueue<Runnable>()

    private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
    private val KEEP_ALIVE_TIME = 1L
    private val KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS
    private val threadPool = ThreadPoolExecutor(
        NUMBER_OF_CORES,
        NUMBER_OF_CORES,
        KEEP_ALIVE_TIME,
        KEEP_ALIVE_TIME_UNIT,
        workQueue
    )

    fun dispatch(
        block: (resultReceiver: RunnableResultReceiver) -> Unit,
        onCompletion: (resultReceiver: RunnableResultReceiver?) -> Unit
    ) {
        Log.i(whoAndWhich(this), "dispatch() with ${NUMBER_OF_CORES} core(s) available")
        threadPool.execute(MyRunnable(this, block, onCompletion))
    }

    private val handler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message?) {
            when (msg?.what) {

                RunnableState.IN_PROGRESS.type -> {
                    val dispatchMessage = msg.obj as DispatchMessage
                    val onProgress = dispatchMessage.onProgress
                    if (onProgress != null) {
                        onProgress(dispatchMessage.resultReceiver.progress)
                    }
                }

                RunnableState.FINISHED.type -> {
                    val dispatchMessage = msg.obj as DispatchMessage
                    val onCompletion = dispatchMessage.onCompletion
                    if (onCompletion != null) {
                        onCompletion(dispatchMessage.resultReceiver)
                    }
                }

                else -> super.handleMessage(msg)

            }
        }

    }

    private fun onRunnableCommunication(dispatchMsg: DispatchMessage) {
        handler.obtainMessage(dispatchMsg.state.type, dispatchMsg)?.apply {
            sendToTarget()
        }
    }

    fun cancelAll() {
        val arrRunnable : Array<Runnable> = workQueue.toTypedArray()
        synchronized(this) {
            arrRunnable.map { (it as? MyRunnable)?.thread }
                .forEach { thread ->
                    thread?.interrupt()
                }
        }
    }

    private class MyRunnable(
        dispatcher: ThreadPoolDispatcher,
        block: (resultReceiver: RunnableResultReceiver) -> Unit,
        onCompletion: (RunnableResultReceiver?) -> Unit
    ) : Runnable {

        lateinit var thread: Thread
        private val dispatcher = dispatcher
        private val block = block
        private val onCompletion = onCompletion

        override fun run() {

            if (Thread.interrupted()) return

            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
            thread = Thread.currentThread()

            // code to run on worker thread
            var resultReceiver = RunnableResultReceiver()
            block(resultReceiver)
            dispatcher.onRunnableCommunication(DispatchMessage(RunnableState.FINISHED, resultReceiver, null, onCompletion))

        }

    }

    private enum class RunnableState(val type: Int) {
        STARTED(1),
        FINISHED(100),
        IN_PROGRESS(2),
        PAUSED(3),
        RESUMED(4),
        STOPPED(5),
        RESTARTED(6),
        INTERRUPTED(7),
        CANCELLED(8)
    }

    private class DispatchMessage(
        state: RunnableState,
        resultReceiver: RunnableResultReceiver,
        onProgress: ((progress: Int) -> Unit)? = null,
        onCompletion: ((resultReceiver: RunnableResultReceiver) -> Unit)? = null
    ) {
        val state = state
        val resultReceiver = resultReceiver
        val onProgress = onProgress
        val onCompletion = onCompletion
    }

    class RunnableResultReceiver {
        var progress: Int = 0
        var result: Any? = null

        fun updateProgress(progress: Int, onProgress: (progress: Int) -> Unit) {
            this.progress = progress
            ThreadPoolDispatcher.onRunnableCommunication(DispatchMessage(RunnableState.IN_PROGRESS, this, onProgress, null))
        }
    }

}
