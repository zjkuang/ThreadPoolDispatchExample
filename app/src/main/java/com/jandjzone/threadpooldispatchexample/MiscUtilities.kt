package com.jandjzone.threadpooldispatchexample

fun whoAndWhich(who: Any): String {
    return who.javaClass.name + " @" + Thread.currentThread().name
}
