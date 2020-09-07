package com.chaidarun.kasawidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import org.json.JSONObject

class ConfigActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_config)
    val ctx = applicationContext

    // Get app widget ID
    val appWidgetId = intent?.extras?.getInt(
      AppWidgetManager.EXTRA_APPWIDGET_ID,
      AppWidgetManager.INVALID_APPWIDGET_ID
    ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    AppLog.d("Starting ConfigActivity for widget ID:", appWidgetId)
    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      Toast.makeText(ctx, "Invalid widget ID", Toast.LENGTH_LONG).show()
      finish()
    }

    // Set result as "canceled" in case user backs out
    // https://developer.android.com/guide/topics/appwidgets#UpdatingFromTheConfiguration
    val resultIntent = Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) }
    setResult(Activity.RESULT_CANCELED, resultIntent)

    // Prepopulate fields
    val state = StateManager.get(ctx)
    val email = findViewById<EditText>(R.id.email).apply { setText(state.getString("email")) }
    val password =
      findViewById<EditText>(R.id.password).apply { setText(state.getString("password")) }
    val alias = findViewById<EditText>(R.id.alias).apply {
      setText(
        state.getJSONObject("togglers").optJSONObject("$appWidgetId")?.getString("alias")
          ?: ""
      )
    }
    findViewById<AppCompatTextView>(R.id.about).text =
      ctx.getString(R.string.about, BuildConfig.VERSION_NAME)

    // Handle form submission
    findViewById<Button>(R.id.save).setOnClickListener {
      // Validate inputs
      if (arrayOf(email, password, alias).any { it.text.isNullOrBlank() }) {
        Toast.makeText(ctx, "All fields are required!", Toast.LENGTH_LONG).show()
        return@setOnClickListener
      }

      // Save fields
      state.put("email", email.text)
      state.put("password", password.text)
      state.put(
        "togglers",
        state.getJSONObject("togglers")
          .put("$appWidgetId", JSONObject(mapOf("alias" to alias.text.toString())))
      )
      StateManager.set(ctx, state)

      // Update widget
      WidgetProvider.render(ctx, null, appWidgetId)

      // Exit activity
      setResult(Activity.RESULT_OK, resultIntent)
      finish()
    }
  }
}
