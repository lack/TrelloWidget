package com.github.oryanmat.trellowidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.support.annotation.ColorInt
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RemoteViews
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.T_WIDGET
import com.github.oryanmat.trellowidget.model.Board
import com.github.oryanmat.trellowidget.util.*
import com.github.oryanmat.trellowidget.util.RemoteViewsUtil.setBackgroundColor
import com.github.oryanmat.trellowidget.util.RemoteViewsUtil.setImage
import com.github.oryanmat.trellowidget.util.RemoteViewsUtil.setImageViewColor
import com.github.oryanmat.trellowidget.util.RemoteViewsUtil.setTextView
import com.github.oryanmat.trellowidget.util.color.lightDim
import com.github.oryanmat.trellowidget.util.RemoteViewsUtil.optionallyHideView

val ADD_ACTION = "com.github.oryanmat.trellowidget.addAction"
val REFRESH_ACTION = "com.github.oryanmat.trellowidget.refreshAction"
val MOVE_CARD_ACTION = "com.github.oryanmat.trellowidget.moveCardAction"
val EXTRA_CARD = "com.github.oryanmat.trellowidget.card"
private val TRELLO_PACKAGE_NAME = "com.trello"
private val TRELLO_URL = "https://www.trello.com"

class TrelloWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            REFRESH_ACTION -> context.notifyDataChanged(intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0))
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        // TODO: We should update both the BoardList and Board on a refresh
        val views = RemoteViews(context.packageName, R.layout.trello_widget)
        updateTitleBar(appWidgetId, context, views)
        updateCardList(appWidgetId, context, views)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun updateTitleBar(appWidgetId: Int, context: Context, views: RemoteViews) {
        val board = context.getBoard(appWidgetId)
        val list = context.getList(appWidgetId)
        @ColorInt val foregroundColor = context.getTitleForegroundColor()

        setBackgroundColor(views, R.id.title_bar, context.getTitleBackgroundColor())
        var imageScale = RemoteViewsUtil.IMAGE_SCALE
        if (context.isTitleTwoline() && context.displayBoardName()) {
            views.setViewVisibility(R.id.board_name, View.VISIBLE)
            setTextView(context, views, R.id.board_name, board.name, foregroundColor, R.dimen.widget_subtitle_text)
            setTextView(context, views, R.id.list_name, list.name, foregroundColor, R.dimen.widget_title_text)
            imageScale = 1.0
        } else {
            views.setViewVisibility(R.id.board_name, View.GONE)

            val titleString = SpannableString(if (context.displayBoardName())
                board.name + " / " + list.name
            else
                list.name)
            val styleStart = if (context.displayBoardName()) board.name.length + 3 else 0
            titleString.setSpan(StyleSpan(Typeface.BOLD), styleStart, titleString.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            setTextView(context, views, R.id.list_name, titleString, foregroundColor, R.dimen.widget_title_text)
        }
        views.setOnClickPendingIntent(R.id.list_title, getTitleIntent(context, board))

        setImage(context, views, R.id.addButton, R.drawable.ic_add_box_white_24dp, imageScale)
        setImage(context, views, R.id.refreshButt, R.drawable.ic_refresh_white_24dp, imageScale)
        setImage(context, views, R.id.configButt, R.drawable.ic_settings_white_24dp, imageScale)
        setImageViewColor(views, R.id.addButton, foregroundColor.lightDim())
        setImageViewColor(views, R.id.refreshButt, foregroundColor.lightDim())
        setImageViewColor(views, R.id.configButt, foregroundColor.lightDim())
        views.setOnClickPendingIntent(R.id.addButton, getAddPendingIntent(context, appWidgetId))
        views.setOnClickPendingIntent(R.id.refreshButt, getRefreshPendingIntent(context, appWidgetId))
        views.setOnClickPendingIntent(R.id.configButt, getReconfigPendingIntent(context, appWidgetId))
        optionallyHideView(views, R.id.addButton, context.showAddButton())
        optionallyHideView(views, R.id.refreshButt, context.showRefreshButton())
        optionallyHideView(views, R.id.configButt, context.showConfigButton())

        setImageViewColor(views, R.id.divider, foregroundColor)
    }

    private fun updateCardList(appWidgetId: Int, context: Context, views: RemoteViews) {
        setBackgroundColor(views, R.id.card_frame, context.getCardBackgroundColor())
        views.setTextColor(R.id.empty_card_list, context.getCardForegroundColor())
        views.setEmptyView(R.id.card_list, R.id.empty_card_list)
        views.setPendingIntentTemplate(R.id.card_list, CardListDispatcherService.generateIntentTemplate(context))
        views.setRemoteAdapter(R.id.card_list, getRemoteAdapterIntent(context, appWidgetId))
    }

    private fun getRemoteAdapterIntent(context: Context, appWidgetId: Int): Intent {
        val intent = Intent(context, WidgetService::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
        return intent
    }

    private fun getAddPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val addIntent = context.createAddCardIntent(appWidgetId)
        return PendingIntent.getActivity(context, appWidgetId, addIntent, 0)
    }

    private fun getRefreshPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val refreshIntent = context.createRefreshIntent(appWidgetId)
        return PendingIntent.getBroadcast(context, appWidgetId, refreshIntent, 0)
    }

    private fun getReconfigPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val reconfigIntent = context.createReconfigureIntent(appWidgetId)
        return PendingIntent.getActivity(context, appWidgetId, reconfigIntent, 0)
    }

    private fun getTitleIntent(context: Context, board: Board): PendingIntent {
        val intent = if (context.isTitleEnabled()) getBoardIntent(context, board) else Intent()
        return PendingIntent.getActivity(context, 0, intent, 0)
    }

    private fun getBoardIntent(context: Context, board: Board) = if (!board.url.isEmpty()) {
        Intent(Intent.ACTION_VIEW, Uri.parse(board.url))
    } else {
        getTrelloIntent(context)
    }

    private fun getTrelloIntent(context: Context): Intent {
        // try to find trello's app if installed. otherwise just open the website.
        val intent = context.packageManager.getLaunchIntentForPackage(TRELLO_PACKAGE_NAME)
        return intent ?: Intent(Intent.ACTION_VIEW, Uri.parse(TRELLO_URL))
    }
}
