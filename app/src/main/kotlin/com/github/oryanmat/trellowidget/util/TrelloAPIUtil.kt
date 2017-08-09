package com.github.oryanmat.trellowidget.util

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.github.oryanmat.trellowidget.R
import com.github.oryanmat.trellowidget.T_WIDGET
import com.github.oryanmat.trellowidget.model.BoardList
import com.github.oryanmat.trellowidget.model.NewCard
import java.util.concurrent.ExecutionException

val TOKEN_PREF_KEY = "com.oryanmat.trellowidget.usertoken"
val APP_KEY = "b250ef70ccf79ea5e107279a91045e6e"
val BASE_URL = "https://api.trello.com/"
val API_VERSION = "1/"
val KEY = "key=$APP_KEY"
val AUTH_URL = "https://trello.com/1/authorize" +
        "?name=TrelloWidget" +
        "&" + KEY +
        "&scope=read,write" +
        "&expiration=never" +
        "&callback_method=fragment" +
        "&return_url=trello-widget://callback"

val USER = "members/me?fields=fullName,username&"
val BOARDS = "members/me/boards?filter=open&fields=id,name,url" + "&lists=open&list_fields=id,name&"
val LIST_CARDS = "lists/%s?cards=open&card_fields=name,badges,labels,url&"
val CARDS = "cards/?"

class TrelloAPIUtil private constructor(internal var context: Context) {
    internal val queue: RequestQueue by lazy { Volley.newRequestQueue(context) }
    internal var preferences = context.preferences()

    companion object {
        lateinit var instance: TrelloAPIUtil

        fun init(context: Context) {
            instance = TrelloAPIUtil(context)
        }
    }

    fun buildURL(query: String) = "$BASE_URL$API_VERSION$query$KEY&${preferences.getString(TOKEN_PREF_KEY, "")}"

    fun user() = buildURL(USER)

    fun boards() = buildURL(BOARDS)

    fun getCards(list: BoardList): BoardList {
        val json = get(buildURL(LIST_CARDS.format(list.id)))

        return Json.tryParseJson(json, BoardList::class.java, BoardList.error(json))
    }

    fun <L> addNewCard(newCard: NewCard, listener: L) where
            L : Response.Listener<String>,
            L : Response.ErrorListener {
        val json = Json.toJson(newCard)
        postAsync(buildURL(CARDS), json, listener)
    }

    fun getUserAsync(listener: Response.Listener<String>, errorListener: Response.ErrorListener) =
            getAsync(user(), listener, errorListener)

    fun getAsync(url: String, listener: Response.Listener<String>, errorListener: Response.ErrorListener) =
            requestAsync(url, null, Request.Method.GET, listener, errorListener)

    fun <L> getAsync(url: String, listener: L) where
            L : Response.Listener<String>,
            L : Response.ErrorListener =
            getAsync(url, listener, listener)

    fun <L> postAsync(url: String, data: String, listener: L) where
            L : Response.Listener<String>,
            L : Response.ErrorListener =
            requestAsync(url, data, Request.Method.POST, listener, listener)

    private fun get(url: String) = syncRequest(url, null, Request.Method.GET)

    private fun syncRequest(url: String, data: String?, method: Int): String {
        val future = RequestFuture.newFuture<String>()
        requestAsync(url, data, method, future, future)
        return get(future)
    }

    private fun get(future: RequestFuture<String>) = try {
        future.get()
    } catch (e: ExecutionException) {
        logException(e)
    } catch (e: InterruptedException) {
        logException(e)
    }

    private fun logException(e: Exception): String {
        val msg = context.getString(R.string.http_fail).format(e)
        Log.e(T_WIDGET, msg)
        return msg
    }

    private class OptionalDataStringRequest(method: Int, url: String, private val data: String?, listener: Response.Listener<String>, errorListener: Response.ErrorListener) :
            StringRequest(method, url, listener, errorListener) {
        override fun getBody(): ByteArray {
            if (data != null)
                return data.toByteArray()
            return ByteArray(0)
        }

        override fun getBodyContentType(): String {
            return "application/json; charset=utf-8"
        }
    }

    fun requestAsync(url: String, data: String?, method: Int, listener: Response.Listener<String>, errorListener: Response.ErrorListener) {
        val request = OptionalDataStringRequest(method, url, data, listener, errorListener)
        queue.add(request)
    }

}