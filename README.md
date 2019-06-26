# ThreadPoolDispatchExample
In this example, a dispatcher is implemented to provide an iOS-like async-dispatch method.

Usage is very simple, as find in MainActivity's onStart(),

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

