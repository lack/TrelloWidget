package com.github.oryanmat.trellowidget.activity

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.T_WIDGET
import com.github.oryanmat.trellowidget.TrelloWidget
import com.github.oryanmat.trellowidget.util.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as TrelloWidget
        setTheme(app.appTheme)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            replaceFragment(if (hasToken()) LoggedInFragment() else LoginFragment())
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
}
