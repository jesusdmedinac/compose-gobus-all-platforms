package com.jesusdmedinac.gobus.data.exception

class StartStopTravelException(
    val listOfExceptions: List<Throwable> = emptyList(),
) : Throwable()
