package com.github.oryanmat.trellowidget.activity

import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Toast
import com.android.volley.VolleyError
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.T_WIDGET
import com.github.oryanmat.trellowidget.model.BoardList
import com.github.oryanmat.trellowidget.model.Card
import com.github.oryanmat.trellowidget.util.color.tintDrawables
import com.github.oryanmat.trellowidget.util.*
import com.github.oryanmat.trellowidget.widget.EXTRA_CARD
import com.github.oryanmat.trellowidget.widget.MOVE_CARD_ACTION
import kotlinx.android.synthetic.main.activity_move_card.*

val EXTRA_NEXTPOS = "com.github.oryanmat.trellowidget.nextPos"
val EXTRA_PREVPOS = "com.github.oryanmat.trellowidget.prevPos"

/**
 * This activity pops up a dialog to move a given card

 * Created by jramsay on 6/7/2016.
 */
class MoveCardActivity : WritableActivity() {
    private var card: Card = Card()
    private var appWidgetId: Int = 0
    private var nextPos = ""
    private var prevPos = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        card = Card.parse(extras?.getString(EXTRA_CARD) ?: "")
        appWidgetId = extras?.getInt(EXTRA_APPWIDGET_ID) ?: 0
        nextPos = extras?.getString(EXTRA_NEXTPOS) ?: ""
        prevPos = extras?.getString(EXTRA_PREVPOS) ?: ""

        setContentView(R.layout.activity_move_card)
        setupTitle(card.name)
        setupButtons()
    }

    private fun setupTitle(cardName: String) {
        move_card_card_name.text = cardName
    }

    private fun setupButtons() {
        Log.i(T_WIDGET, "Prev:$prevPos Next:$nextPos")
        with(move_card_top_button) {
            isEnabled = !prevPos.isEmpty()
            setOnClickListener { moveTo(CARDS_POSITION_TOP) }
            tintDrawables(theme, android.R.attr.colorForeground)
        }
        with (move_card_up_button) {
            isEnabled = (!prevPos.isEmpty() && prevPos != CARDS_POSITION_TOP)
            setOnClickListener { moveTo(prevPos) }
            tintDrawables(theme, android.R.attr.colorForeground)
        }
        with (move_card_down_button) {
            isEnabled = (!nextPos.isEmpty() && nextPos != CARDS_POSITION_BOTTOM)
            setOnClickListener { moveTo(nextPos) }
            tintDrawables(theme, android.R.attr.colorForeground)
        }
        with (move_card_bottom_button) {
            isEnabled = !nextPos.isEmpty()
            setOnClickListener { moveTo(CARDS_POSITION_BOTTOM) }
            tintDrawables(theme, android.R.attr.colorForeground)
        }

        with (move_card_list_button) {
            isEnabled = false
            setOnClickListener { moveToList() }
        }
        with (move_card_list_selection) {
            // TODO: Consider fetching these in case the cached values are out of date
            val board = getBoard(appWidgetId)
            val list = getList(appWidgetId)
            val adapter = ArrayAdapter(this@MoveCardActivity, android.R.layout.simple_spinner_item, board.lists)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            setAdapter(adapter)
            val selectedIndex = board.lists.indexOf(list)
            setSelection(if (selectedIndex > -1) selectedIndex else 0)
            onItemSelectedListener = ListSelectionListener(move_card_list_button, selectedIndex)
        }
    }

    private fun moveTo(position: String) {
        if (position.isEmpty()) {
            Log.w(T_WIDGET, "Not moving card $card: No position given")
            close(false)
        } else {
            Log.d(T_WIDGET, "Moving card $card to $position")
            TrelloAPIUtil.instance.repositionCard(card, position, MoveListener())
        }
    }

    private fun moveToList() {
        val destination = move_card_list_selection.selectedItem as BoardList
        if (destination.id == "-1") {
            Log.w(T_WIDGET, "No destination board: Not moving")
            close(false)
        } else {
            Log.d(T_WIDGET, "Moving card $card to ${destination.name} (${destination.id})")
            TrelloAPIUtil.instance.moveCardToList(card, destination, MoveListener())
        }
    }

    private fun close(needsReload: Boolean) {
        finish()
        if (needsReload) {
            val refreshIntent = createRefreshIntent(appWidgetId)
            sendBroadcast(refreshIntent)
        }
    }

    companion object {
        fun createMoveCardIntent(context: Context, extras: Bundle): Intent {
            val moveCardIntent = Intent(MOVE_CARD_ACTION, Uri.EMPTY, context, MoveCardActivity::class.java)
            for (key in arrayOf(EXTRA_CARD, EXTRA_APPWIDGET_ID, EXTRA_NEXTPOS, EXTRA_PREVPOS)) {
                val value = extras.get(key)
                when (value) {
                    is String -> moveCardIntent.putExtra(key, value)
                    is Int -> moveCardIntent.putExtra(key, value)
                }
            }
            return moveCardIntent
        }
    }

    inner class MoveListener: TrelloAPIUtil.CardResponseListener() {
        override fun onResponse(card: Card) {
            Log.d(T_WIDGET, "Move request succeeded")
            Toast.makeText(this@MoveCardActivity, getString(R.string.move_card_success), Toast.LENGTH_SHORT).show()
            close(true)
        }
        override fun onErrorResponse(error: VolleyError) {
            TrelloAPIUtil.instance.logError("Move request failed", error)
            val message = getString(when(error.networkResponse.statusCode) {
                401 -> R.string.move_card_permission_failure
                else -> R.string.move_card_failure
            })
            Toast.makeText(this@MoveCardActivity, message, Toast.LENGTH_LONG).show()
        }
    }

    inner class ListSelectionListener(
            private val goButton: ImageButton,
            private val currentSelection: Int
    ): AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            goButton.isEnabled = false
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            goButton.isEnabled = position != currentSelection
        }

    }
}
