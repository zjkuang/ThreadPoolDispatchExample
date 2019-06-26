# ThreadPoolDispatchExample
According to [Android Official Doc](https://developer.android.com/training/multiple-threads), dispatching a job to a background thread with ThreadPoolExecutor is not as straight forward as supposed to be. It is especially painful when the background job needs to communicate with main thread in order to update UI. (Consider the counterpart async-dispatch in iOS.)

I created a Kotlin object, ThreadPoolDispatcher, encapsulating all the stuffs in and around ThreadPoolExecutor, so that we can also enjoy the style of async-dispatch with callback(s) as we do in iOS.

The source code of Kotlin object ThreadPoolExecutor, together with a couple of easy examples, is included in the example project. The calling spot is in MainActivity's onStart(). Hope you would enjoy it. Any comments, advices, suggestions, corrections, and so on, would be appreciated.

Under the hood, the dispatcher creates a work queue using as many cores available to maximize the concurrency. ThreadPoolExecutor is employed to dispatch the block to the thread allocated from the pool.

Usage is very simple, as find in the project's MainActivity onStart(),

    ThreadPoolDispatcher.dispatch(block = { resultReceiver ->
    
        // some heavy job can be executed here on background thread
    
        // resultReceiver.result = ... // produce result through resultReceiver, which can be used in the onCompletion callback block on main thread
    
    }, onCompletion = { resultReceiver ->
    
        // now on main thread, safe to update UI
    
        // the result produced by the background block is available at resultReceiver.result
    
    })

Updating of progress is also supported,

    ThreadPoolDispatcher.dispatch(block = { resultReceiver ->
    
        // some heavy job can be executed here on background thread
    
        // update progress
        var progressPercentage = 50
        resultReceiver.updateProgress(progressPercentage, onProgress - { progress ->
    
            // now on main thread, safe to update UI such as progress bar according to the value of progress
    
        })
    
        // resultReceiver.result = ... // produce result through resultReceiver, which can be used in the onCompletion callback block on main thread
    
    }, onCompletion = { resultReceiver ->
    
        // now on main thread, safe to update UI
    
        // the result produced by the background block is available at resultReceiver.result
    
    })

