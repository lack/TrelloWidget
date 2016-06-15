package com.github.oryanmat.trellowidget.activity

import android.app.Activity
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.T_WIDGET
import com.github.oryanmat.trellowidget.TrelloWidget
import com.github.oryanmat.trellowidget.model.Card
import com.github.oryanmat.trellowidget.util.color.tintDrawables
import com.github.oryanmat.trellowidget.util.createRefreshIntent
import com.github.oryanmat.trellowidget.widget.EXTRA_CARD
import com.github.oryanmat.trellowidget.widget.MOVE_CARD_ACTION

/**
 * This activity pops up a dialog to move a given card

 * Created by jramsay on 6/7/2016.
 */
class MoveCardActivity : Activity() {
    private var card: Card = Card()
    private var appWidgetId: Int = 0
    private val cardMoved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as TrelloWidget
        setTheme(app.dialogTheme)

        val extras = intent.extras
        card = Card.parse(extras.getString(EXTRA_CARD))
        appWidgetId = extras.getInt(EXTRA_APPWIDGET_ID)

        setContentView(R.layout.activity_move_card)
        setupTitle(card.name)
        setupButtons()
    }

    private fun setupTitle(cardName: String) {
        val text = findViewById(R.id.move_card_card_name) as TextView
        text.text = cardName
    }

    private fun setupButton(key: Int, listener: (v: View) -> Unit) {
        val button = findViewById(key) as Button
        button.setOnClickListener(listener)
        button.tintDrawables(theme, android.R.attr.colorForeground)
    }

    private fun setupButtons() {
        setupButton(R.id.move_card_top_button)    { moveToTop() }
        setupButton(R.id.move_card_up_button)     { moveUp() }
        setupButton(R.id.move_card_list_button)   { moveToList() }
        setupButton(R.id.move_card_down_button)   { moveDown() }
        setupButton(R.id.move_card_bottom_button) { moveToBottom() }
    }

    private fun moveToTop() {
        Log.d(T_WIDGET, "Would move card $card to top")
        close()
    }

    private fun moveUp() {
        Log.d(T_WIDGET, "Would move card $card up by one")
        close()
    }

    private fun moveDown() {
        Log.d(T_WIDGET, "Would move card $card down by one")
        close()
    }

    private fun moveToBottom() {
        Log.d(T_WIDGET, "Would move card $card to bottom")
        close()
    }

    private fun moveToList() {
        Log.d(T_WIDGET, "Would move card $card to ... some other list")
        close()
    }

    private fun close() {
        finish()
        if (cardMoved) {
            val refreshIntent = createRefreshIntent(appWidgetId)
            sendBroadcast(refreshIntent)
        }
    }

    companion object {
        fun createMoveCardIntent(context: Context, card: Card, appWidgetId: Int): Intent {
            val moveCardIntent = Intent(MOVE_CARD_ACTION, Uri.EMPTY, context, MoveCardActivity::class.java)
            moveCardIntent.putExtra(EXTRA_CARD, card.toJson())
            moveCardIntent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId)
            return moveCardIntent
        }
    }
}
