package com.jesusdmedinac.gobus.data

import dev.gitlive.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow

expect class GobusFirebaseBridge() {
    suspend fun getPaths(): Flow<QuerySnapshot>
}
