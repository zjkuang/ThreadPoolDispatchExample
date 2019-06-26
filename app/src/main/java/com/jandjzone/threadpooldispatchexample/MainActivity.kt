package com.jandjzone.threadpooldispatchexample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        testSimpleDispatch()
        testDispatchWithProressReport()
        testConcurrentDispatch(10)
    }

    private fun testSimpleDispatch() {

        Log.i(whoAndWhich(this), "testSimpleDispatch()")

        ThreadPoolDispatcher.dispatch(block = { resultReceiver ->

            // showing that this block is dispatched onto a background thread
            Log.i(whoAndWhich(this), "job started")

            // now on background thread, we can do some heavy job here

            // feedback the result (of type Any?)
            resultReceiver.result = "OK"

        }, onCompletion = { resultReceiver ->
            // onCompletion callback, dispatched back onto the main thread
            Log.i(whoAndWhich(this), "onCompletion: result = ${resultReceiver?.result}")
        })
    }

    private fun testDispatchWithProressReport() {

        Log.i(whoAndWhich(this), "testDispatchWithProressReport()")

        ThreadPoolDispatcher.dispatch(block = { resultReceiver ->

            // showing that this block is dispatched onto a background thread
            Log.i(whoAndWhich(this), "job started")

            // now on background thread, we can do some heavy job here
            // and report the progress during the heavy task
            for (i in 0 until 9) {
                resultReceiver.updateProgress(i * 10, onProgress = { progress ->
                    // showing that this block (onProgress) is dispatched back onto the main thread
                    Log.i(whoAndWhich(this), "progress ${progress}%")

                    // now on main thread, it is safe to update some UI element like progress bar

                })
            }

            // feedback the result (of type Any?)
            resultReceiver.result = "OK"

        }, onCompletion = { resultReceiver ->
            // onCompletion callback, dispatched back onto the main thread
            Log.i(whoAndWhich(this), "onCompletion: result = ${resultReceiver?.result}")
        })
    }

    private fun testConcurrentDispatch(times: Int) {

        Log.i(whoAndWhich(this), "testConcurrentDispatch()")

        // test for n times to show different cores are allocated alternately
        for (i in 0 until times) {

            ThreadPoolDispatcher.dispatch(block = { resultReceiver ->

                // showing that this block is dispatched onto a background thread
                Log.i(whoAndWhich(this), "concurrent job ${i} started")

                // now on background thread, we can do some heavy job here

                resultReceiver.result = "OK"

            }, onCompletion = { resultReceiver ->
                // onCompletion callback, dispatched back onto the main thread
                Log.i(whoAndWhich(this), "onCompletion: result = ${resultReceiver?.result}")
            })

        }
    }

}

