package com.example.whatsup

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.whatsup.fragment.GroupFragment
import com.example.whatsup.fragment.MyAccountFragment
import com.example.whatsup.fragment.PeopleFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navigation_people ->{
                    replaceFragment(PeopleFragment())
                    true
                }
                R.id.navigation_my_account ->{
                    replaceFragment(GroupFragment())
                    true
                }
                else -> false
            }
        }


    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_layout, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId


        when (itemId) {
            R.id.profile ->{
                replaceFragment(MyAccountFragment())
            }

            R.id.group ->{
                startActivity<CreerGroupActivity>()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
