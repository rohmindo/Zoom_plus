package com.example.zoom

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class FragmentAdapter(fm: FragmentManager, behavior:Int): FragmentPagerAdapter(fm,behavior) {

    var fragmentList=listOf<Fragment>()
    //var fragmentList=listOf(Fragment_home(),Fragment_room(),Fragment_subject(),Fragment_info())
    override fun getItem(position: Int): Fragment {
        return fragmentList.get(position)
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position){
            0 -> "A"
            1 -> "B"
            2 -> "C"
            else -> "D"
        }
    }
}