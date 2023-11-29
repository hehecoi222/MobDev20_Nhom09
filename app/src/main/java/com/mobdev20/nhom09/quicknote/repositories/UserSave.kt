package com.mobdev20.nhom09.quicknote.repositories

import com.google.firebase.auth.FirebaseAuth
import com.mobdev20.nhom09.quicknote.state.UserState
import kotlinx.coroutines.flow.Flow

interface UserSave {
    fun loadInfo(auth: FirebaseAuth) : UserState
}