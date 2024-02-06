package com.jesusdmedinac.gobus.data

import dev.gitlive.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow

actual class GobusFirebaseBridge {
    actual suspend fun getPaths(): Flow<QuerySnapshot> = TODO()
}
