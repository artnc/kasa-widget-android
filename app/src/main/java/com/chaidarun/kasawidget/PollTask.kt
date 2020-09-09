package com.chaidarun.kasawidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.AsyncTask
import org.json.JSONObject

class PollTask : AsyncTask<PollTask.Params, Void, Unit>() {
  data class Params(
    val ctx: Context,
    val appWidgetManager: AppWidgetManager?,
    val state: JSONObject,
    val appWidgetId: Int,
  )

  override fun doInBackground(vararg p0: Params) {
    val (ctx, appWidgetManager, state, appWidgetId) = p0[0]
    val alias = state.getJSONObject("togglers").optJSONObject("$appWidgetId")?.getString("alias")
    val resId =
      when (alias?.let { Api.isOn(state.getString("email"), state.getString("password"), it) }) {
        true -> Icon.ON
        false -> Icon.OFF
        null -> Icon.ERROR
      }
    WidgetProvider.render(ctx, appWidgetManager, appWidgetId, resId, alias)
  }
}
