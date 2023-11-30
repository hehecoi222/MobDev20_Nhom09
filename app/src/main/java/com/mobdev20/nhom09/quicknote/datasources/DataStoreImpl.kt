package com.mobdev20.nhom09.quicknote.datasources

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Qualifier

interface NoteDataStore {
    suspend fun writeTo(key: String, value: String)
    fun readFrom(key: String): Flow<String?>

    fun readAll(): Flow<List<String?>>

    suspend fun delete(key: String)
}

class NoteDataStoreImpl @Inject constructor(@ApplicationContext private val context: Context) :
    NoteDataStore {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "note_store")
    }
    override suspend fun writeTo(key: String, value: String) {
        val KEY = stringPreferencesKey(key)
        context.dataStore.edit { noteStore ->
            noteStore[KEY] = value
        }
    }

    override fun readFrom(key: String): Flow<String?> {
        return context.dataStore.data.map { noteStore ->
            noteStore[stringPreferencesKey(key)]
        }
    }

    override fun readAll(): Flow<List<String?>> {
        return context.dataStore.data.map { noteStore ->
            noteStore.asMap().values.toList() as List<String?>
        }
    }

    override suspend fun delete(key: String) {
        val KEY = stringPreferencesKey(key)
        context.dataStore.edit { noteStore ->
            noteStore.remove(KEY)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NoteDataStoreModule {
    @Binds
    abstract fun bindNoteDataStoreForNotes(noteDataStoreImpl: NoteDataStoreImpl) : NoteDataStore
}