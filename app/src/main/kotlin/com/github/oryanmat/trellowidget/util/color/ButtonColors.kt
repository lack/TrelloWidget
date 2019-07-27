package com.github.oryanmat.trellowidget.util.color

import android.content.res.Resources
import android.graphics.PorterDuff
import android.support.annotation.AttrRes
import android.support.annotation.ColorInt
import android.util.TypedValue
import android.widget.Button

fun Button.tintDrawables(@ColorInt color: Int) {
    for (drawable in this.compoundDrawables.filterNotNull()) {
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }
}

fun Button.tintDrawables(theme: Resources.Theme, @AttrRes themeResource: Int) {
    val themeAttribute = TypedValue()
    if (theme.resolveAttribute(themeResource, themeAttribute, true)) {
        this.tintDrawables(themeAttribute.data)
    }
}

