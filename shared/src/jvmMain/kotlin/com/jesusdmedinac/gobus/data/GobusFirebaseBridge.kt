package com.jesusdmedinac.gobus.data

import com.jesusdmedinac.gobus.data.remote.model.Path
import dev.gitlive.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class GobusFirebaseBridge {
    actual suspend fun getPaths(): Flow<QuerySnapshot> = TODO()
}
