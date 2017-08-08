package com.github.oryanmat.trellowidget.activity

import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.VolleyError
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.T_WIDGET
import com.github.oryanmat.trellowidget.model.NewCard
import com.github.oryanmat.trellowidget.util.TrelloAPIUtil
import com.github.oryanmat.trellowidget.util.getBoard
import com.github.oryanmat.trellowidget.util.getList
import com.github.oryanmat.trellowidget.util.color.tintDrawables
import com.github.oryanmat.trellowidget.util.createRefreshIntent
import kotlinx.android.synthetic.main.activity_add_card.*
import java.nio.charset.Charset

class AddCardActivity : WritableActivity() {
    private var appWidgetId = INVALID_APPWIDGET_ID
    internal var cardsAdded = 0
    internal var addSingleCard = false

    enum class Location {
        INSERT_AT_TOP,
        INSERT_AT_BOTTOM
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    inner class AddCardListener(private val description: String) : Response.Listener<String>, Response.ErrorListener {
        override fun onResponse(response: String) {
            // TODO: The 'response' is the json of the newly-created card - We could maybe inject this into the RemoteView without forcing a refresh?
            Log.i(T_WIDGET, "Added card to $description")
            cardsAdded++
            if (addSingleCard)
                close()
            else
                resetInput()
            Toast.makeText(this@AddCardActivity, getString(R.string.add_card_success), Toast.LENGTH_SHORT).show()
        }

        override fun onErrorResponse(error: VolleyError) {
            Log.e(T_WIDGET, "Add Card failed: ${error.networkResponse.data.toString(Charset.defaultCharset())}", error)
            val message = getString(when(error.networkResponse.statusCode) {
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
