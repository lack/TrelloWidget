package com.github.oryanmat.trellowidget.activity

import android.app.AlertDialog
import android.os.Bundle
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.util.clearToken
import com.github.oryanmat.trellowidget.util.createMainActivityIntent
import com.github.oryanmat.trellowidget.util.sessionCanWrite

abstract class WritableActivity : ThemedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!sessionCanWrite()) {
            AlertDialog.Builder(this)
                    .setIcon(R.mipmap.logo)
                    .setTitle(getString(R.string.add_card_relogin_required_title))
                    .setMessage(getString(R.string.add_card_relogin_required_details))
                    .setPositiveButton(android.R.string.ok) { _, _ -> relogin() }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> finish() }
                    .show()
        }
    }

    private fun relogin() {
        clearToken()
        finish()
        startActivity(createMainActivityIntent())
    }
}