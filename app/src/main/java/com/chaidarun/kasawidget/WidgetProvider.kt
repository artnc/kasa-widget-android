package com.chaidarun.kasawidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class WidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    AppLog.d("Updating widgets:", *appWidgetIds.toTypedArray())
    val state = StateManager.get(context)
    AppLog.d("State:", state)
    appWidgetIds.forEach {
      PollTask().execute(PollTask.Params(context, appWidgetManager, state, it))
    }
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    super.onDeleted(context, appWidgetIds)
    val state = StateManager.get(context)
    val togglers = state.getJSONObject("togglers")
    AppLog.d("Deleting widgets:", *appWidgetIds.toTypedArray())
    appWidgetIds.forEach { togglers.remove("$it") }
    StateManager.set(context, state)
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    when (intent.action) {
      TOGGLE_INTENT_ACTION -> {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
          AppWidgetManager.INVALID_APPWIDGET_ID)
        AppLog.d("Toggling widget:", appWidgetId)
        when (appWidgetId) {
          AppWidgetManager.INVALID_APPWIDGET_ID -> AppLog.e("Invalid widget ID")
          else -> ToggleTask().execute(ToggleTask.Params(context, appWidgetId))
        }
      }
    }
  }

  companion object {
    fun render(
      ctx: Context,
      appWidgetManager: AppWidgetManager?,
      appWidgetId: Int,
      icon: Icon,
      text: String?,
    ) {
      (appWidgetManager ?: AppWidgetManager.getInstance(ctx)).updateAppWidget(appWidgetId,
        RemoteViews(ctx.applicationContext.packageName, R.layout.widget).apply {
          // Draw icon
          setImageViewResource(R.id.toggle, icon.resId)
          setOnClickPendingIntent(R.id.toggle,
            PendingIntent.getBroadcast(ctx,
              appWidgetId,
              Intent(ctx, WidgetProvider::class.java).apply {
                action = TOGGLE_INTENT_ACTION
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
              },
              0))

          // Draw alias
          setTextViewText(R.id.widget_alias, text ?: "")
          setOnClickPendingIntent(R.id.widget_alias,
            PendingIntent.getActivity(ctx,
              appWidgetId,
              Intent(ctx, ConfigActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
              },
              0)
          )
        })
    }

    private const val TOGGLE_INTENT_ACTION = "toggle"
  }
}
