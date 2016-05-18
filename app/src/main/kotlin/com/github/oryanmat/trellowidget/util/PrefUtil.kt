package com.github.oryanmat.trellowidget.util

import android.content.Context
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.support.annotation.ColorInt
import android.support.annotation.StringRes
import com.github.oryanmat.trellowidget.R

internal fun Context.getPrefTextScale() =
        java.lang.Float.parseFloat(getStringPref(R.string.pref_text_size_key))

internal fun Context.getInterval() =
        Integer.parseInt(getStringPref(R.string.pref_update_interval_key))

internal @ColorInt fun Context.getCardBackgroundColor() =
        getColorPref(R.string.pref_back_color_key)

internal @ColorInt fun Context.getCardForegroundColor() =
        getColorPref(R.string.pref_fore_color_key)

internal fun Context.displayBoardName() =
        isEnabled(R.string.pref_display_board_name_key)

internal fun Context.isTitleUniqueColor() =
        isEnabled(R.string.pref_title_use_unique_color_key)

internal fun Context.isTitleEnabled() =
        isEnabled(R.string.pref_title_onclick_key)

internal fun Context.showAddButton() =
        isEnabled(R.string.pref_add_button_key)

internal fun Context.showRefreshButton() =
        isEnabled(R.string.pref_refresh_button_key)

internal fun Context.showConfigButton() =
        isEnabled(R.string.pref_config_button_key)

internal @ColorInt fun Context.getTitleBackgroundColor(): Int = when {
    isTitleUniqueColor() -> getColorPref(R.string.pref_title_back_color_key)
    else -> getCardBackgroundColor()
}

internal @ColorInt fun Context.getTitleForegroundColor(): Int = when {
    isTitleUniqueColor() -> getColorPref(R.string.pref_title_fore_color_key)
    else -> getCardForegroundColor()
}

internal fun Context.isDarkThemeEnabled() =
        isEnabled(R.string.pref_ui_theme_dark_key)

private fun Context.sharedPreferences() = getDefaultSharedPreferences(this)

// Note: Because 'setDefaultValues' is called in TrelloWidget.onCreate we can rely on all values already being set
// IE the 'defValue' part of these get*() calls are never returned
private fun Context.isEnabled(@StringRes key: Int) =
        sharedPreferences().getBoolean(getString(key), true)

private @ColorInt fun Context.getColorPref(@StringRes key: Int) =
        sharedPreferences().getInt(getString(key), 0)

private fun Context.getStringPref(@StringRes key: Int) =
        sharedPreferences().getString(getString(key), "")
