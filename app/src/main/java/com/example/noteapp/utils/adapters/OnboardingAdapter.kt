package com.example.noteapp.utils.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.noteapp.R
import com.example.noteapp.ui.elements.activity.OnboardingFragment

class OnboardingAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3 // 3 màn hình onboarding

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragment.newInstance(R.drawable.img_gioi_thieu, "Jot Down anything you want to achieve, today or in the future")
            1 -> OnboardingFragment.newInstance(R.drawable.img_goal, "Different goals, different way to jot it down.")
            2 -> OnboardingFragment.newInstance(R.drawable.img_custom, "Text area, checklist, or some combination. Adapt with your needs")
            else -> OnboardingFragment.newInstance(R.drawable.img_gioi_thieu, "")
        }
    }
}