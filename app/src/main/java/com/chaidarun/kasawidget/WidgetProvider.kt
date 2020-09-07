package com.chaidarun.kasawidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.widget.RemoteViews

class WidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    appWidgetIds.forEach {
      render(context, appWidgetManager, it)
    }
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    super.onDeleted(context, appWidgetIds)
    val state = StateManager.get(context)
    val togglers = state.getJSONObject("togglers")
    AppLog.d("Widget IDs to delete:", appWidgetIds.joinToString(",") { "$it" })
    appWidgetIds.forEach { togglers.remove("$it") }
    StateManager.set(context, state)
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    if (intent.action == "TOGGLE") {
      val appWidgetId = intent.getIntExtra("APP_WIDGET_ID", -1)
      if (appWidgetId != -1) {
        ToggleTask().execute(ToggleTask.Params(context, appWidgetId))
      }
    }
  }


  private class ToggleTask : AsyncTask<ToggleTask.Params, Void, Unit>() {
    data class Params(val ctx: Context, val appWidgetId: Int)

    override fun doInBackground(vararg p0: Params) {
      // Read saved state
      val (ctx, appWidgetId) = p0[0]
      val state = StateManager.get(ctx)
      val email = state.getString("email")
      val password = state.getString("password")
      val alias =
        state.getJSONObject("togglers").getJSONObject("$appWidgetId").getString("alias")

      Api.toggle(email, password, alias)
      render(ctx, AppWidgetManager.getInstance(ctx), appWidgetId)
    }
  }

  companion object {
    fun render(ctx: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
      RefreshTask().execute(RefreshTask.Params(ctx, appWidgetManager, appWidgetId))
    }

    private class RefreshTask : AsyncTask<RefreshTask.Params, Void, Unit>() {
      data class Params(
        val ctx: Context,
        val appWidgetManager: AppWidgetManager,
        val appWidgetId: Int,
      )

      override fun doInBackground(vararg p0: Params) {
        val (ctx, appWidgetManager, appWidgetId) = p0[0]
        val state = StateManager.get(ctx)
        AppLog.d("State to render:", state)
        val alias = state.getJSONObject("togglers").getJSONObject("$appWidgetId").getString("alias")
        val isOn = Api.getState(state.getString("email"), state.getString("password"), alias) == 1
        appWidgetManager.updateAppWidget(appWidgetId,
          RemoteViews(ctx.applicationContext.packageName, R.layout.widget).apply {
            // Draw icon
            setImageViewResource(R.id.toggle,
              if (isOn) R.drawable.ic_power_on else R.drawable.ic_power_off)
            setOnClickPendingIntent(R.id.toggle,
              PendingIntent.getBroadcast(ctx,
                0,
                Intent(ctx, WidgetProvider::class.java).apply {
                  action = "TOGGLE"
                  putExtra("APP_WIDGET_ID", appWidgetId)
                },
                0))

            // Draw alias
            setTextViewText(R.id.widget_alias, alias)
            setOnClickPendingIntent(R.id.widget_alias,
              PendingIntent.getActivity(ctx,
                0,
                Intent(ctx, ConfigActivity::class.java).apply {
                  putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                },
                0)
            )
          })
      }
    }
  }
}
