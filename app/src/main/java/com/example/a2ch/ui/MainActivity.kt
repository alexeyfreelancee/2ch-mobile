package com.example.a2ch.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.a2ch.R
import com.example.a2ch.ui.boards.BoardsFragment
import com.example.a2ch.ui.favourite.FavouritesFragment
import com.example.a2ch.ui.history.HistoryFragment
import com.example.a2ch.util.gone
import com.example.a2ch.util.toast
import com.example.a2ch.util.visible
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.actvity_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val boardsFragment = BoardsFragment()
    private val favouritesFragment = FavouritesFragment()
    private val historyFragment = HistoryFragment()
    private var activeFragment: Fragment = boardsFragment
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_main)
        if (savedInstanceState == null) {
            initFragments()
        }
        setSupportActionBar(toolbar)
        checkPermissions()
        initNavigationDrawer()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_option, menu)

        val searchItem = menu?.findItem(R.id.opt_search)
         searchView = searchItem?.actionView as SearchView

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    boardsFragment.filter(it)
                }

                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_boards -> {
                supportFragmentManager.beginTransaction().hide(activeFragment).show(boardsFragment).commit()
                activeFragment = boardsFragment
                searchView.visible()
            }
            R.id.nav_favourites ->{
                supportFragmentManager.beginTransaction().hide(activeFragment).show(favouritesFragment).commit()
                activeFragment = favouritesFragment
                searchView.gone()
            }
            R.id.nav_history -> {
                supportFragmentManager.beginTransaction().hide(activeFragment).show(historyFragment).commit()
                activeFragment = historyFragment
                searchView.gone()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun initNavigationDrawer() {
        val drawerToggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.open,
            R.string.close
        )


        nav_view.setCheckedItem(R.id.nav_boards)
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            Navigation.findNavController(
                this,
                R.id.fragment_container
            ),
            drawer_layout
        )
    }
    private fun checkPermissions() {
        if (
            ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1234
                )
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1234){
            if(grantResults.isEmpty()
                && grantResults[0] != PackageManager.PERMISSION_GRANTED
                && grantResults[1] != PackageManager.PERMISSION_GRANTED){
                toast("Вы не сможете загружать фотокарточки")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun initFragments() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, favouritesFragment, "favouritesFragment").hide(favouritesFragment)
            .commit()
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, historyFragment, "historyFragment").hide(historyFragment)
            .commit()
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, boardsFragment, "boardsFragment")
            .commit()
    }

    override fun onBackPressed() {
        val view: View? = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        if (!searchView.isIconified) {
            searchView.isIconified = true
        } else {
            super.onBackPressed()
        }

    }
}
