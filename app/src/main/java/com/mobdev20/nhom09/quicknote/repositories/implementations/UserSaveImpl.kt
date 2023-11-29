package com.mobdev20.nhom09.quicknote.repositories.implementations

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.mobdev20.nhom09.quicknote.repositories.UserSave
import com.mobdev20.nhom09.quicknote.state.UserState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserSaveImpl @Inject constructor() : UserSave {
    override fun loadInfo(auth: FirebaseAuth): UserState {
        val user = auth.currentUser
        if (user != null) {
            return UserState(
                    id = user.uid,
                    username = user.displayName!!
                )
            }

        return UserState()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UserSaveModule {
    @Binds
    abstract fun bindUserSave(userSaveImpl: UserSaveImpl): UserSave
}