package com.example.a2ch.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.a2ch.R
import com.example.a2ch.ui.boards.BoardsFragment
import com.example.a2ch.ui.thread_list.ThreadsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.actvity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private val threadFragment = ThreadsFragment()
    private val boardsFragment = BoardsFragment()

    private var activeFragment: Fragment = boardsFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_main)

        if(savedInstanceState == null){
            initFragments()
        }
    }

    private fun initFragments(){
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, threadFragment, "1").hide(threadFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, boardsFragment, "2").commit()

    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
      when(item.itemId){
          R.id.nav_boards -> {
              supportFragmentManager.beginTransaction().hide(activeFragment).show(boardsFragment).commit()
              activeFragment = boardsFragment
          }
          R.id.nav_threads ->{
              supportFragmentManager.beginTransaction().hide(activeFragment).show(threadFragment).commit()
              activeFragment = threadFragment
          }
      }
        return true
    }
}
