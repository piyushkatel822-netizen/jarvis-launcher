package com.jarvis.launcher

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class JarvisWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val intent = Intent(context, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_layout
            )

            views.setOnClickPendingIntent(
                R.id.widget_button,
                pendingIntent
            )

            appWidgetManager.updateAppWidget(
                appWidgetId,
                views
            )
        }
    }
}
