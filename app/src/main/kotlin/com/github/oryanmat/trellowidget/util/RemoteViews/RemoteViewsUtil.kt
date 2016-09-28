package com.github.oryanmat.trellowidget.util.RemoteViews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.*
import android.graphics.drawable.BitmapDrawable
import android.support.annotation.ColorInt
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.widget.RemoteViews
import com.github.oryanmat.trellowidget.util.getPrefTextScale

val METHOD_SET_ALPHA = "setAlpha"
val METHOD_SET_COLOR_FILTER = "setColorFilter"
internal val IMAGE_SCALE = 1.5

fun RemoteViews.setTextView(context: Context,
                            @IdRes textView: Int, text: String,
                            @ColorInt color: Int, @DimenRes dimen: Int) {
    setTextView(textView, text, color)
    setTextViewTextSize(textView, TypedValue.COMPLEX_UNIT_SP,
            getScaledValue(context, dimen))
}

fun RemoteViews.setTextView(@IdRes textView: Int,
                            text: String, @ColorInt color: Int) {
    setTextViewText(textView, text)
    setTextColor(textView, color)
}

fun RemoteViews.setImage(context: Context,
                         @IdRes view: Int, @DrawableRes image: Int) {
    val drawable = ContextCompat.getDrawable(context, image)
    val bitmap = (drawable as BitmapDrawable).bitmap
    val density = context.resources.displayMetrics.density
    val prefTextScale = context.getPrefTextScale()
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap,
            (bitmap.width.toDouble() * IMAGE_SCALE * prefTextScale.toDouble() / density).toInt(),
            (bitmap.height.toDouble() * IMAGE_SCALE * prefTextScale.toDouble() / density).toInt(), true)
    setImageViewBitmap(view, scaledBitmap)
}

fun RemoteViews.setImageViewColor(@IdRes view: Int, @ColorInt color: Int) {
    val opaqueColor = Color.rgb(red(color), green(color), blue(color))
    setInt(view, METHOD_SET_COLOR_FILTER, opaqueColor)
    setInt(view, METHOD_SET_ALPHA, alpha(color))
}

fun getScaledValue(context: Context, @DimenRes dimen: Int): Float {
    val dimension = context.resources.getDimension(dimen)
    val density = context.resources.displayMetrics.density
    val prefTextScale = context.getPrefTextScale()
    return dimension * prefTextScale / density
}
