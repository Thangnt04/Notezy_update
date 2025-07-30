package com.example.noteapp.ui.elements.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.noteapp.R
import com.example.noteapp.databinding.ActivityAuthenticationBinding
import com.example.noteapp.ui.view_models.AuthenticationViewModel
import com.example.noteapp.ui.view_models.UserViewModel
import com.example.noteapp.utils.response.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val authenticationViewModel: AuthenticationViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private val signInIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d("AuthActivity", "Sign-in intent result: resultCode=${result.resultCode}, data=${result.data?.extras}")
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleGoogleSignInTask(task)
            } catch (e: Exception) {
                Log.e("AuthActivity", "Error processing sign-in intent: ${e.message}", e)
                showToast("Error: ${e.message}")
//                binding.signInProgressBar.visibility = View.GONE
            }
        } else {
            Log.e("AuthActivity", "Sign-in intent failed: resultCode=${result.resultCode}, data=${result.data?.extras}")
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    Log.d("AuthActivity", "Task successful but resultCode not OK: ${task.result}")
                } else {
                    val errorCode = (task.exception as? com.google.android.gms.common.api.ApiException)?.statusCode
                    val errorMessage = task.exception?.message ?: "Unknown error"
                    Log.e("AuthActivity", "Task failed: $errorMessage, errorCode=$errorCode")
                    showToast("Sign-in failed: $errorMessage (errorCode=$errorCode)")
                }
            } catch (e: Exception) {
                Log.e("AuthActivity", "Error checking task: ${e.message}", e)
                showToast("Sign-in canceled or failed (resultCode=${result.resultCode})")
            }
//            binding.signInProgressBar.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Kiểm tra người dùng hiện tại
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Log.d("AuthActivity", "User already signed in: UID=${currentUser.uid}, Email=${currentUser.email}")
            navigateToMain()
        } else {
            Log.d("AuthActivity", "No user signed in")
        }

        binding.apply {
            buttonSignIn.setOnClickListener {
                Log.d("AuthActivity", "Sign-in button clicked")
                sendSignInWithGoogleIntent()
            }
        }

        authenticationViewModel.userAuthResult.observe(this) { authResult ->
            Log.d("AuthActivity", "AuthResult received: $authResult")
            when (authResult) {
                is AuthResult.Success -> {
                    val user = authResult.data ?: return@observe
                    Log.d("AuthActivity", "Sign-in success: UID=${user.uid}, Name=${user.name}, Email=${user.email}, IsNewUser=${authResult.isNewUser}")
                    if (authResult.isNewUser) userViewModel.addUser(user)
                    navigateToMain()
                }
                is AuthResult.Error -> {
                    Log.e("AuthActivity", "Sign-in error: ${authResult.message}")
                    showToast(authResult.message.toString())
                }
                else -> {
                    Log.e("AuthActivity", "Unknown error in AuthResult")
                    showToast(getString(R.string.something_wont_wrong))
                }
            }
        }
    }

    private fun sendSignInWithGoogleIntent() {
        Log.d("AuthActivity", "Sending Google Sign-In intent")
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.webClientId))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        val signInIntent = googleSignInClient.signInIntent
        signInIntentLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInTask(task: Task<GoogleSignInAccount>) {
        Log.d("AuthActivity", "Handling Google Sign-In task: isSuccessful=${task.isSuccessful}")
        if (task.isSuccessful) {
            val account = task.result
            Log.d("AuthActivity", "Google Sign-In success: Account=${account?.email}")
            applySignInRequest(account)
        } else {
            Log.e("AuthActivity", "Google Sign-In failed: ${task.exception?.message}")
            showToast(task.exception?.message ?: "Sign-in failed")
        }
    }

    private fun applySignInRequest(account: GoogleSignInAccount?) {
        if (account != null) {
            Log.d("AuthActivity", "Applying sign-in request for account: ${account.email}")
            authenticationViewModel.signInWithGoogle(account)
        } else {
            Log.e("AuthActivity", "GoogleSignInAccount is null")
            showToast("Failed to get Google account")
        }
    }

    private fun navigateToMain() {
        Log.d("AuthActivity", "Navigating to MainActivity")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}