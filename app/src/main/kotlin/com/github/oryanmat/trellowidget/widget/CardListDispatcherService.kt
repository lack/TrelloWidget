package com.github.oryanmat.trellowidget.widget

import android.app.IntentService
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.github.oryanmat.trellowidget.T_WIDGET
import com.github.oryanmat.trellowidget.activity.MoveCardActivity
import com.github.oryanmat.trellowidget.model.Card
import com.github.oryanmat.trellowidget.util.createViewCardIntent

/**
 * A Service to dispatch intents from CardRemoteViewFactory because a RemoweViewFactory cannot fully dispatch different intents for multiple widgets in a single list view item
 *
 * Created by jramsay on 9/27/2016.
 */

val ACTION_CARD_LIST = "com.github.oryanmat.trellowidget.CardListDispatcher"
val EXTRA_METHOD = "com.github.oryanmat.trellowidget.dispatchMethod"

class CardListDispatcherService: IntentService("CardListDispatcherService") {

    enum class Method { VIEW, MOVE }

    override fun onHandleIntent(intent: Intent) {
        val extras = intent.extras
        val card = Card.parse(extras.getString(EXTRA_CARD))
        val method = Method.valueOf(extras.getString(EXTRA_METHOD))
        val appWidgetId = extras.getInt(EXTRA_APPWIDGET_ID)

        Log.d(T_WIDGET, "Dispatching a $method request for card $card")
        val nextIntent = when(method) {
            Method.VIEW -> createViewCardIntent(card)
            Method.MOVE -> MoveCardActivity.createMoveCardIntent(this, card, appWidgetId)
        }
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(nextIntent)
    }

    companion object IntentFactory {
        fun generateIntent(context: Context, method: Method, appWidgetId: Int, card: Card): Intent {
            val intent = Intent(ACTION_CARD_LIST, Uri.EMPTY, context, CardListDispatcherService::class.java)
            intent.putExtra(EXTRA_METHOD, method.toString())
            intent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId)
            intent.putExtra(EXTRA_CARD, card.toJson())
            return intent
        }

        fun generateIntentTemplate(context: Context): PendingIntent {
            val intent = Intent(ACTION_CARD_LIST, Uri.EMPTY, context, CardListDispatcherService::class.java)
            return PendingIntent.getService(context, 0, intent, 0)
        }
    }
}