package com.github.oryanmat.trellowidget.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager.*
import android.content.Context
import android.content.Intent
import android.net.Uri

import com.github.oryanmat.trellowidget.activity.AddCardActivity
import com.github.oryanmat.trellowidget.activity.ConfigActivity
import com.github.oryanmat.trellowidget.activity.MainActivity
import com.github.oryanmat.trellowidget.model.Card
import com.github.oryanmat.trellowidget.widget.TrelloWidgetProvider

import com.github.oryanmat.trellowidget.widget.ADD_ACTION
import com.github.oryanmat.trellowidget.widget.REFRESH_ACTION

internal fun Context.createRefreshIntent(appWidgetId: Int): Intent {
    val refreshIntent = Intent(this, TrelloWidgetProvider::class.java)
    refreshIntent.action = REFRESH_ACTION
    refreshIntent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId)
    return refreshIntent
}

internal fun Context.createReconfigureIntent(appWidgetId: Int): Intent {
    val configIntent = Intent(this, ConfigActivity::class.java)
    configIntent.action = ACTION_APPWIDGET_CONFIGURE
    configIntent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId)
    return configIntent
}

internal fun Context.createAddCardIntent(appWidgetId: Int): Intent {
    val addIntent = Intent(this, AddCardActivity::class.java)
    addIntent.action = ADD_ACTION
    addIntent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId)
    return addIntent
}

internal fun Context.createViewCardIntentTemplate(): PendingIntent {
    val viewIntentTemplate = Intent(Intent.ACTION_VIEW)
    return PendingIntent.getActivity(this, 0, viewIntentTemplate, 0)
}

internal fun createViewCardIntent(card: Card): Intent {
    val viewCardIntent = Intent(Intent.ACTION_VIEW, Uri.parse(card.url))
    return viewCardIntent
}

internal fun Context.createMainActivityIntent() =
        Intent(this, MainActivity::class.java)
