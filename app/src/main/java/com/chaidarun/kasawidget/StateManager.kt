package com.chaidarun.kasawidget

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit
import org.json.JSONObject

object StateManager {
  private var lastSerializedState: String? = null

  fun get(ctx: Context): JSONObject {
    val state = JSONObject(lastSerializedState ?: getPrefs(ctx).getString(PREFS_KEY, null)
    ?: return setDefault(ctx))
    if (state.getInt("version") != STATE_SCHEMA_VERSION) {
      return setDefault(ctx)
    }
    return state
  }

  fun set(ctx: Context, state: JSONObject) {
    // Abort if no-op
    val serializedState = state.toString()
    if (lastSerializedState == serializedState) {
      return
    }
    lastSerializedState = serializedState

    // Write to disk
    getPrefs(ctx).edit {
      clear()
      putString(PREFS_KEY, serializedState)
    }
  }

  private fun getPrefs(ctx: Context) = PreferenceManager.getDefaultSharedPreferences(ctx)

  private fun setDefault(ctx: Context) = JSONObject(mapOf(
    "email" to "",
    "password" to "",
    "togglers" to JSONObject(),
    "version" to STATE_SCHEMA_VERSION,
  )).also { set(ctx, it) }

  private const val PREFS_KEY = "state"
  private const val STATE_SCHEMA_VERSION = 1
}
