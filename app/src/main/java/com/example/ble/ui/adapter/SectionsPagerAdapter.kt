package com.example.ble.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.ble.ui.activity.characteristics.CharacteristicFragment
import com.example.ble.ui.activity.devices.DevicesFragment
import com.example.ble.ui.activity.services.ServicesFragment

private val TAB_TITLES = arrayOf(
    "Devices",
    "Services",
    "Characteristics"
)

class SectionsPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
       return when (position) {
            0 -> DevicesFragment.newInstance()
            1 -> ServicesFragment.newInstance()
            else -> CharacteristicFragment.newInstance()
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return TAB_TITLES[position]
    }

    override fun getCount(): Int = TAB_TITLES.size
}