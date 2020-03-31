package com.example.a2ch.ui

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.a2ch.R
import com.example.a2ch.ui.boards.BoardsFragment

import com.example.a2ch.util.toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.actvity_main.*

class MainActivity : AppCompatActivity() {
    private val boardsFragment = BoardsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_main)


        if (savedInstanceState == null) {
            initFragments()
        }
        checkPermissions()
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
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, boardsFragment, "2")
            .commit()

    }

}
