package com.mobdev20.nhom09.quicknote

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.color.DynamicColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mobdev20.nhom09.quicknote.databinding.ActivityMainBinding
import com.mobdev20.nhom09.quicknote.databinding.ActivityUserBinding
import com.mobdev20.nhom09.quicknote.ui.theme.MainAppTheme
import com.mobdev20.nhom09.quicknote.viewmodels.AccountViewModel
import com.mobdev20.nhom09.quicknote.views.ButtonColumn
import com.mobdev20.nhom09.quicknote.views.SignInButton
import com.mobdev20.nhom09.quicknote.views.TopSigninBar
import com.mobdev20.nhom09.quicknote.views.UserInfo
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserBinding
    private var darkMode = false

    @Inject
    lateinit var accountViewModel: AccountViewModel

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme ứng dụng
        setTheme(
            if (DynamicColors.isDynamicColorAvailable()) R.style.Theme_QuickNote_Dynamic
            else if ((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) R.style.Theme_QuickNote_Dark
            else R.style.Theme_QuickNote
        )

        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        val view = binding.root

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("46228750855-tlvk4t11su287qv3f9c5cm2fe5anknn0.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        binding.topSiginBar.apply {
            setContent {
                MainAppTheme {
                    darkMode = isSystemInDarkTheme()
                    TopSigninBar(
                        onBackClick = {
                            finish()
                        }
                    )
                }
            }
        }

        binding.userInfo.apply {
            setContent {
                MainAppTheme {
                    UserInfo(
                        id = accountViewModel.userState.collectAsState().value.id,
                        username = accountViewModel.userState.collectAsState().value.username,
                        visible = accountViewModel.isSignIn.value
                    )
                }
            }
        }

        binding.signinButton.apply {
            setContent {
                MainAppTheme {
                    SignInButton(visible = accountViewModel.isSignIn.value, onClickSignIn = {
                        signIn()
                    }, onClickCopy = {
                        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                            ClipData.newPlainText("UserId", accountViewModel.userState.value.id)
                        )
                    })
                }
            }
        }

        binding.functionCol.apply {
            setContent {
                MainAppTheme {
                    ButtonColumn(
                        onClickSignOut = {
                            if (accountViewModel.isSignIn.value) {
                                Firebase.auth.signOut()
                                accountViewModel.signOut()
                            } else {
                                Toast.makeText(context, "You need an account for this", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onClickkRecover = {
                            if (accountViewModel.isSignIn.value) {

                            } else {
                                Toast.makeText(context, "You need an account for this", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
        setContentView(view)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            accountViewModel.updateUser(currentUser)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    if (user != null) {
                        accountViewModel.updateUser(user)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}