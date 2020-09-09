package com.chaidarun.kasawidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.widget.RemoteViews
import org.json.JSONObject

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

      // Show loading state
      render(ctx, null, appWidgetId, Icon.LOADING, alias)

      // Toggle
      val isOn = Api.toggle(email, password, alias)
      render(ctx, null, appWidgetId, if (isOn) Icon.ON else Icon.OFF, alias)
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

    class PollTask : AsyncTask<PollTask.Params, Void, Unit>() {
      data class Params(
        val ctx: Context,
        val appWidgetManager: AppWidgetManager?,
        val state: JSONObject,
        val appWidgetId: Int,
      )

      override fun doInBackground(vararg p0: Params) {
        val (ctx, appWidgetManager, state, appWidgetId) = p0[0]
        val alias =
          state.getJSONObject("togglers").optJSONObject("$appWidgetId")?.getString("alias")
        val isOn =
          alias?.let { Api.isOn(state.getString("email"), state.getString("password"), it) }
        val resId = when (isOn) {
          true -> Icon.ON
          false -> Icon.OFF
          null -> Icon.ERROR
        }
        render(ctx, appWidgetManager, appWidgetId, resId, alias)
      }
    }
  }
}
