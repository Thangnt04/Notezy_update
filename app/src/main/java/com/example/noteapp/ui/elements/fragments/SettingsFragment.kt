package com.example.noteapp.ui.elements.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.noteapp.R
import com.example.noteapp.databinding.FragmentSettingsBinding
import com.example.noteapp.ui.elements.activity.AuthenticationActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // hien thi thong tin nguoi dung
        val user = Firebase.auth.currentUser
        binding.apply {


            back.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_noteListFragment)
            }
            textViewUserName.text = user?.displayName ?: "Unknown"
            textViewUserEmail.text = user?.email ?:"No email"

            buttonSignOut.setOnClickListener{
                signOut()
                navigateToAuthentication()
            }
            // Xử lý tab bar
            tabHome.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_noteListFragment)
            }

            tabFinished.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_finishedNotesFragment)
            }

            iconAdd.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_newNoteFragment)
            }


            tabSearch.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_noteListFragment)
            }

            tabSettings.setOnClickListener {
                // Ở lại fragment hiện tại
            }
        }

    }

    private fun signOut() {
        val GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(requireContext().getString(R.string.webClientId))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions)
        googleSignInClient.signOut()
        Firebase.auth.signOut()
    }

    private fun navigateToAuthentication() {
        val intent = Intent(requireContext(),AuthenticationActivity::class.java)
        requireActivity().startActivity(intent)
        requireActivity().finish()
    }

}