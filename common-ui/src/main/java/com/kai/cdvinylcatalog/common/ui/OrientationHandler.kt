package com.kai.cdvinylcatalog.common.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/**
 * Управляет ориентацией экрана в зависимости от размера устройства.
 *
 * На телефонах (< 600dp) — принудительно портретная ориентация.
 * На планшетах и складных (>= 600dp) — полная свобода ориентации.
 *
 * Это соответствует рекомендациям Google для Android 16+: не фиксировать
 * ориентацию на больших экранах, потому что они всё равно игнорируют
 * атрибут screenOrientation из манифеста.
 */
@Composable
fun OrientationHandler() {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    LaunchedEffect(configuration.screenWidthDp, configuration.orientation) {
        activity ?: return@LaunchedEffect

        val isCompact = configuration.screenWidthDp < 600
        val desiredOrientation = if (isCompact) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        }

        if (activity.requestedOrientation != desiredOrientation) {
            activity.requestedOrientation = desiredOrientation
        }
    }
}

/**
 * Находит Activity из Context.
 * Нужно, потому что LocalContext может быть ContextWrapper.
 */
private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}