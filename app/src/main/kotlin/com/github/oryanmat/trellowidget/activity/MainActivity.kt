package com.github.oryanmat.trellowidget.activity

import android.app.ActionBar
import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.T_WIDGET
import com.github.oryanmat.trellowidget.TrelloWidget
import com.github.oryanmat.trellowidget.util.*
import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.android.synthetic.main.notification_template_lines_media.view.*

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as TrelloWidget
        setTheme(app.appTheme)
        setContentView(R.layout.activity_main)

        tabs.setupWithViewPager(pager)

        val x = supportFragmentManager
        pager.adapter = object : FragmentPagerAdapter(x) {
            override fun getItem(position: Int): android.support.v4.app.Fragment = when(position) {
                0 -> if (hasToken()) LoggedInFragment() else LoginFragment()
                else -> GeneralPreferenceFragment()
            }

            override fun getPageTitle(position: Int): CharSequence = when(position) {
                0-> "Session"
                else -> "Settings"
            }

            override fun getCount(): Int = 2
        }
    }

    fun startBrowserWithAuthURL(view: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AUTH_URL))

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_VIEW) {
            saveUserToken(intent)
        }
    }

    private fun saveUserToken(intent: Intent) {
        saveToken(intent.data.fragment)

        //replaceFragment(LoggedInFragment())
    }

    @JvmOverloads fun logout(view: View? = null) {
        clearToken()

        //replaceFragment(LoginFragment())
    }

    /*
    private fun saveUserToken(intent: Intent) {
        saveToken(intent.data.fragment)

        replaceFragment(LoggedInFragment())
    }

    @JvmOverloads fun logout(view: View? = null) {
        clearToken()

        replaceFragment(LoginFragment())
    }

    private fun replaceFragment(fragment: Fragment) = fragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
*/

}
