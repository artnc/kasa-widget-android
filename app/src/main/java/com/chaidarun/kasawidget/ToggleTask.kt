package com.chaidarun.kasawidget

import android.content.Context
import android.os.AsyncTask

class ToggleTask : AsyncTask<ToggleTask.Params, Void, Unit>() {
  data class Params(val ctx: Context, val appWidgetId: Int)

  override fun doInBackground(vararg p0: Params) {
    // Read saved state
    val (ctx, appWidgetId) = p0[0]
    val state = StateManager.get(ctx)
    val alias = state.getJSONObject("togglers").getJSONObject("$appWidgetId").getString("alias")

    // Show loading state
    WidgetProvider.render(ctx, null, appWidgetId, Icon.LOADING, alias)

    // Toggle
    val isOn = Api.toggle(state.getString("email"), state.getString("password"), alias)
    WidgetProvider.render(ctx, null, appWidgetId, if (isOn) Icon.ON else Icon.OFF, alias)
  }
}
