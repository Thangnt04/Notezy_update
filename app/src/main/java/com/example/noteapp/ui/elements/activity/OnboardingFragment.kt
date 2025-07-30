package com.example.noteapp.ui.elements.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.noteapp.databinding.FragmentOnboardingBinding // Thêm binding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_IMAGE = "image"
        private const val ARG_TEXT = "text"

        fun newInstance(imageRes: Int, text: String): OnboardingFragment {
            val fragment = OnboardingFragment()
            val args = Bundle().apply {
                putInt(ARG_IMAGE, imageRes)
                putString(ARG_TEXT, text)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            binding.illustration.setImageResource(it.getInt(ARG_IMAGE)) // Sử dụng binding
            binding.title.text = it.getString(ARG_TEXT) // Sử dụng binding
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Tránh rò rỉ bộ nhớ
    }
}