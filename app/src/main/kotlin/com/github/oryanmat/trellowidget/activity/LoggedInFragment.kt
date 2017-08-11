package com.github.oryanmat.trellowidget.activity

import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.VolleyError
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.T_WIDGET
import com.github.oryanmat.trellowidget.model.User
import com.github.oryanmat.trellowidget.util.Json
import com.github.oryanmat.trellowidget.util.TrelloAPIUtil
import kotlinx.android.synthetic.main.fragment_logged_in.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class LoggedInFragment : android.support.v4.app.Fragment(), Response.Listener<String>, Response.ErrorListener {
    private val USER = "com.github.oryanmat.trellowidget.activity.user"
    private val VISIBILITY = "com.github.oryanmat.trellowidget.activity.visibility"
    private val MAX_LOGIN_FAIL = 3
    private val DELAY = TimeUnit.SECONDS.toMillis(1)

    private var user = User()
    private var loginAttempts = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_logged_in, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.i(T_WIDGET, "Activity created...")
        val visibility = savedInstanceState?.getInt(VISIBILITY, View.VISIBLE) ?: View.VISIBLE

        if (visibility == View.GONE) {
            Log.i(T_WIDGET, "Visibility = GONE")
            val userJson = savedInstanceState?.getString(USER) ?: ""
            setUser(Json.tryParseJson(userJson, User::class.java, User()))
        } else {
            Log.i(T_WIDGET, "Visibility = VISIBLE")
            val v = view
            v!!.loading_panel.visibility = View.VISIBLE
            v.signed_panel.visibility = View.INVISIBLE
            login()
        }
    }

    override fun onErrorResponse(error: VolleyError) {
        Log.e(T_WIDGET, error.toString())

        if (loginAttempts >= MAX_LOGIN_FAIL) {
            // logout after N failed get requests so we can try to login later
            logout(error)
        } else {
            // try again shortly. could be temp problem
            Timer().schedule(DELAY, { login() })
        }
    }

    private fun login() {
        Log.i(T_WIDGET, "Doing login")
        loginAttempts++
        TrelloAPIUtil.instance.getUserAsync(this, this)
    }

    private fun logout(error: VolleyError) {
        if (isAdded) {
            (activity as MainActivity).logout()
            val text = getString(R.string.login_fail).format(error)
            Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
        }
    }

    override fun onResponse(response: String) = setUser(Json.tryParseJson(response, User::class.java, User()))

    private fun setUser(user: User) {
        this.user = user
        val v = view
        v ?: return
        v.signed_text.text = getString(R.string.singed).format(user)
        v.loading_panel.visibility = View.GONE
        v.signed_panel.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val v = view
        v ?: return
        outState.putInt(VISIBILITY, v.loading_panel.visibility)
        outState.putString(USER, Json.toJson(user))
    }
}
