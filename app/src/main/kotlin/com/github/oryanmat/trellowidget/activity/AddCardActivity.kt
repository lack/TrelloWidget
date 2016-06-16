package com.github.oryanmat.trellowidget.activity

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.VolleyError
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.T_WIDGET
import com.github.oryanmat.trellowidget.TrelloWidget
import com.github.oryanmat.trellowidget.model.Card
import com.github.oryanmat.trellowidget.model.NewCard
import com.github.oryanmat.trellowidget.util.*
import com.github.oryanmat.trellowidget.util.color.tintDrawables
import kotlinx.android.synthetic.main.activity_add_card.*

class AddCardActivity : Activity() {
    private var appWidgetId = INVALID_APPWIDGET_ID
    internal var cardsAdded = 0

    enum class Location {
        INSERT_AT_TOP,
        INSERT_AT_BOTTOM
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as TrelloWidget
        setTheme(app.dialogTheme)

        if (!sessionCanWrite()) {
            AlertDialog.Builder(this)
                    .setIcon(R.mipmap.logo)
                    .setTitle(getString(R.string.add_card_relogin_required_title))
                    .setMessage(getString(R.string.add_card_relogin_required_details))
                    .setPositiveButton(android.R.string.ok, { _, _ -> relogin() })
                    .setNegativeButton(android.R.string.cancel, { _, _ -> finish() })
                    .show()
            return
        }
        setContentView(R.layout.activity_add_card)
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        }
        if (appWidgetId == INVALID_APPWIDGET_ID) {
            Log.e(T_WIDGET, "Invalid widget ID: Cannot add a new card")
            finish()
        }

        val list = getList(appWidgetId)
        val board = getBoard(appWidgetId)

        add_card_board_name.text = "${board.name} / ${list.name}"
        addCloseButton.setOnClickListener { close() }
        topButton.setOnClickListener { addNewCard(Location.INSERT_AT_TOP) }
        bottomButton.setOnClickListener { addNewCard(Location.INSERT_AT_BOTTOM) }
        topButton.tintDrawables(theme, android.R.attr.colorForeground)
        bottomButton.tintDrawables(theme, android.R.attr.colorForeground)
        setButtonsEnabled(true)
        addMultiples.setOnCheckedChangeListener { _, b -> openAfterAdd.isEnabled = !b }
    }

    private fun relogin() {
        clearToken()
        finish()
        startActivity(createMainActivityIntent())
    }

    private fun addNewCard(where: Location) {
        val newTitle = add_card_title.text.toString()
        if (newTitle.isEmpty()) {
            return
        }
        setButtonsEnabled(false)

        val list = getList(appWidgetId)
        val board = getBoard(appWidgetId)

        val newCard = NewCard(list.id, newTitle, when(where) {
          Location.INSERT_AT_TOP -> NewCard.POS_TOP
          Location.INSERT_AT_BOTTOM -> NewCard.POS_BOTTOM
        })

        val description = "${board.name} / ${list.name}"
        Log.d(T_WIDGET, "Adding new card to ${if (where == Location.INSERT_AT_TOP) "top" else "bottom"} of $description: $newTitle")
        val listener = AddCardListener(description)
        TrelloAPIUtil.instance.addNewCard(newCard, listener)
        // TODO: Start a spinner or something?
    }

    inner class AddCardListener(private val description: String) : TrelloAPIUtil.CardResponseListener() {
        override fun onResponse(card: Card) {
            // TODO: We could maybe inject this into the RemoteView without forcing a refresh?
            cardsAdded++
            if (card.url.isEmpty()) {
                val message = getString(R.string.add_card_parse_failure)
                Log.w(T_WIDGET, message)
                Toast.makeText(this@AddCardActivity, message, Toast.LENGTH_LONG).show()
                close()
                return
            }

            Log.i(T_WIDGET, "Added card ${card.id} (${card.url}) to $description")
            if (!addMultiples.isChecked) {
                if (openAfterAdd.isEnabled && openAfterAdd.isChecked) {
                    val openCardIntent = createViewCardIntent(card)
                    startActivity(openCardIntent)
                }
                close()
            } else {
                resetInput()
            }
            Toast.makeText(this@AddCardActivity, getString(R.string.add_card_success), Toast.LENGTH_SHORT).show()
        }

        override fun onErrorResponse(error: VolleyError) {
            TrelloAPIUtil.instance.logError("Add request failed", error)
            val message = getString(when(error.networkResponse.statusCode) {
                // TODO: Maybe actually open the login dialog for error 401?
                401 -> R.string.add_card_permission_failure
                else -> R.string.add_card_failure
            })
            Toast.makeText(this@AddCardActivity, message, Toast.LENGTH_LONG).show()
            setButtonsEnabled(true)
        }
    }

    private fun resetInput() {
        add_card_title.setText("")
        setButtonsEnabled(true)
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        for (button in arrayOf(addCloseButton, topButton, bottomButton))
            button.isEnabled = enabled
    }

    private fun close() {
        finish()
        if (cardsAdded > 0) {
            sendBroadcast(createRefreshIntent(appWidgetId))
        }
    }
}
