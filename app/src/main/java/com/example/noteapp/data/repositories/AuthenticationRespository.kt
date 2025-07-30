package com.example.noteapp.data.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.noteapp.data.models.User
import com.example.noteapp.utils.response.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 *
 */
class AuthenticationRepository {
    private val firebaseAuth: FirebaseAuth by lazy {
        Firebase.auth
    }

    /**
     *
     */
    suspend fun signInWithGoogle(account: GoogleSignInAccount): MutableLiveData<AuthResult<User>> =
        withContext(Dispatchers.IO) {
            val result = MutableLiveData<AuthResult<User>>()
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Log.d("AuthRepository", "Attempting sign-in with Google credential: ${account.email}")
            try {
                val authResult = firebaseAuth.signInWithCredential(credential).await()
                val user = authResult.user
                if (user != null) {
                    val uid = user.uid
                    val name = user.displayName
                    val email = user.email
                    val userObj = User(uid, name, email)
                    val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                    Log.d("AuthRepository", "Sign-in success: UID=$uid, Email=$email, IsNewUser=$isNewUser")
                    result.postValue(AuthResult.Success(userObj, isNewUser = isNewUser)) // ✅ sửa
                } else {
                    Log.e("AuthRepository", "Firebase user is null")
                    result.postValue(AuthResult.Error("Firebase user is null")) // ✅ sửa
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Sign-in failed: ${e.message}", e)
                result.postValue(AuthResult.Error(e.message ?: "Unknown error")) // ✅ sửa
            }
            result
        }

}