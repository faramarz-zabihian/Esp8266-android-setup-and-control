package com.farashahr.esp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.farashahr.esp.BedLights


class DeviceCollectionAdapter(
    fragment: FragmentActivity
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return  if(position == 0) Lights.newInstance() else BedLights.newInstance()
    }
}