package com.mobdev20.nhom09.quicknote.repositories.implementations

import com.mobdev20.nhom09.quicknote.datasources.NoteDataStore
import com.mobdev20.nhom09.quicknote.helpers.Encoder
import com.mobdev20.nhom09.quicknote.helpers.NoteJson
import com.mobdev20.nhom09.quicknote.repositories.NoteSave
import com.mobdev20.nhom09.quicknote.state.HistoryType
import com.mobdev20.nhom09.quicknote.state.NoteHistory
import com.mobdev20.nhom09.quicknote.state.NoteState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.time.LocalDateTime
import javax.inject.Inject

class NoteSaveImpl @Inject constructor() : NoteSave {
    @Inject
    lateinit var noteDataStore: NoteDataStore
    override suspend fun update(noteState: NoteState) {
        val model = NoteJson.convertModel(noteState)
        noteDataStore.writeTo(noteState.id, model)
    }

    override fun loadNote(id: String): Flow<NoteState?> {
        val noteFlow = noteDataStore.readFrom(id)
        return noteFlow.map { value ->
            if (value == null || value.isEmpty()) {
                null
            } else {
                NoteJson.convertJson(value)
            }
        }
    }

    override fun loadListNote(vararg criteria: String): StateFlow<List<NoteState>> {
        TODO("Not yet implemented")
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NoteSaveModule {

    @Binds
    abstract fun bindNoteSave(noteSaveImpl: NoteSaveImpl): NoteSave
}