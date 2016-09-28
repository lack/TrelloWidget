package com.github.oryanmat.trellowidget.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.*
import android.net.Uri
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.model.BoardList
import com.github.oryanmat.trellowidget.model.Card
import com.github.oryanmat.trellowidget.model.Label
import com.github.oryanmat.trellowidget.util.*
import com.github.oryanmat.trellowidget.util.RemoteViews.*
import com.github.oryanmat.trellowidget.util.color.*
import java.util.*

class CardRemoteViewFactory(private val context: Context,
                            private val appWidgetId: Int) : RemoteViewsService.RemoteViewsFactory {
    private var cards: List<Card> = ArrayList()
    @ColorInt private var color = 0

    override fun onDataSetChanged() {
        var list = context.getList(appWidgetId)
        list = TrelloAPIUtil.instance.getCards(list)
        color = context.getForegroundColor()

        if (BoardList.ERROR != list.id) {
            cards = list.cards
        } else {
            color = color.dim()
        }
    }

    override fun getViewAt(position: Int): RemoteViews {
        val card = cards[position]
        val views = RemoteViews(context.packageName, R.layout.card)
        with (views) {
            setLabels(card)
            setTitle(card)
            setBadges(card)
            setDivider()
            setOnClickFillInIntent(card)
        }
        return views
    }

    private fun RemoteViews.setOnClickFillInIntent(card: Card) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(card.url))
        setOnClickFillInIntent(R.id.card, intent)
    }

    private fun RemoteViews.setBadges(card: Card) {
        setSubscribed(card)
        setVotes(card)
        setDescription(card)
        setComments(card)
        setChecklist(card)
        setDueDate(card)
        setAttachments(card)
    }

    private fun RemoteViews.setTitle(card: Card) =
            setTextView(context, R.id.card_title, card.name, color, R.dimen.card_badges_text)

    private fun RemoteViews.setSubscribed(card: Card) =
        setBadge(R.id.subscribed, R.drawable.ic_visibility_white_24dp, card.badges.subscribed)

    private fun RemoteViews.setVotes(card: Card) =
        setIntBadge(R.id.votes, R.id.vote_count,
                R.drawable.ic_thumb_up_white_24dp, card.badges.votes)

    private fun RemoteViews.setDescription(card: Card) =
        setBadge(R.id.desc, R.drawable.ic_subject_white_24dp, card.badges.description)

    private fun RemoteViews.setDueDate(card: Card) {
        val visible = card.badges.due != null
        val text = if (visible) DateTimeUtil.parseDate(card.badges.due!!) else ""
        setBadge(R.id.due, R.id.due_string,
                R.drawable.ic_access_time_white_24dp, text, visible)
    }

    private fun RemoteViews.setChecklist(card: Card) {
        val text = "${card.badges.checkItemsChecked}/${card.badges.checkItems}"
        val visible = card.badges.checkItems > 0
        setBadge(R.id.checklist, R.id.checklist_count,
                R.drawable.ic_check_box_white_24dp, text, visible)
    }

    private fun RemoteViews.setComments(card: Card) =
        setIntBadge(R.id.comments, R.id.comment_count,
                R.drawable.ic_chat_bubble_outline_white_24dp, card.badges.comments)

    private fun RemoteViews.setAttachments(card: Card) =
        setIntBadge(R.id.attachment, R.id.attachment_count,
                R.drawable.ic_attachment_white_24dp, card.badges.attachments)

    private fun RemoteViews.setIntBadge(@IdRes view: Int, @IdRes textView: Int,
                            @DrawableRes image: Int, value: Int) =
        setBadge(view, textView, image, value.toString(), value > 0)

    private fun RemoteViews.setBadge(@IdRes view: Int, @IdRes textView: Int,
                         @DrawableRes image: Int, text: String, visible: Boolean) {
        setTextView(context, textView, text, color, R.dimen.card_badges_text)
        setViewVisibility(textView, if (visible) View.VISIBLE else View.GONE)
        setBadge(view, image, visible)
    }

    private fun RemoteViews.setBadge(@IdRes view: Int, @DrawableRes image: Int, visible: Boolean) {
        setViewVisibility(view, if (visible) View.VISIBLE else View.GONE)
        setImageViewColor(view, color)
        setImage(context, view, image)
    }

    private fun RemoteViews.setLabels(card: Card) {
        removeAllViews(R.id.labels_layout)
        card.labels.forEach { setLabel(it) }
    }

    private fun RemoteViews.setLabel(label: Label) {
        var labelColor: Int = colors[label.color] ?: Color.TRANSPARENT
        labelColor = Color.argb(alpha(color), red(labelColor), green(labelColor), blue(labelColor))
        val innerView = RemoteViews(context.packageName, R.layout.label)
        innerView.setImageViewColor(R.id.label, labelColor)
        innerView.setImage(context, R.id.label, R.drawable.label)
        addView(R.id.labels_layout, innerView)
    }

    private fun RemoteViews.setDivider() = setImageViewColor(R.id.list_item_divider, color)

    override fun onCreate() {
    }

    override fun onDestroy() {
    }

    override fun getCount(): Int = cards.size

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds() = true
}