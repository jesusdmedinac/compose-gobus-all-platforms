package com.jesusdmedinac.gobus.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.QuerySnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

actual class GobusFirebaseBridge {
    private val firestore = Firebase.firestore
    private val android = firestore.android

    actual suspend fun getPaths(): Flow<QuerySnapshot> = callbackFlow {
        android.collection("paths").apply {
            val listener = addSnapshotListener { snapshot, exception ->
                snapshot?.let { trySend(QuerySnapshot(snapshot)) }
                exception?.let { close(exception) }
            }
            awaitClose { listener.remove() }
        }
    }
        .conflate()
}
