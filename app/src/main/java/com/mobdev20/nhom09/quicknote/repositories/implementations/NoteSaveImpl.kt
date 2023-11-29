package com.mobdev20.nhom09.quicknote.repositories.implementations

import com.mobdev20.nhom09.quicknote.datasources.NoteDataStore
import com.mobdev20.nhom09.quicknote.helpers.NoteJson
import com.mobdev20.nhom09.quicknote.repositories.NoteSave
import com.mobdev20.nhom09.quicknote.state.NoteOverview
import com.mobdev20.nhom09.quicknote.state.NoteState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteSaveImpl @Inject constructor() : NoteSave {
    @Inject
    lateinit var noteDataStore: NoteDataStore
    override suspend fun update(noteState: NoteState) {
        val model = NoteJson.convertModel(noteState)
        noteDataStore.writeTo(noteState.id, model)
    }

    override suspend fun delete(id: String) {
        noteDataStore.delete(id)
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

    override fun loadListNote(vararg criteria: String): Flow<List<NoteOverview?>> {
        val noteListFlow = noteDataStore.readAll()
        return noteListFlow.map { noteList ->
            if (noteList.isEmpty()) {
                emptyList()
            } else {
                noteList.map {
                    if (it != null && it.isNotEmpty()) {
                        NoteJson.convertPartialJson(it)
                    } else null
                }.toList()
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NoteSaveModule {

    @Binds
    abstract fun bindNoteSave(noteSaveImpl: NoteSaveImpl): NoteSave
}