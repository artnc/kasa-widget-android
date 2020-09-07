package com.chaidarun.kasawidget

import android.util.Log

object AppLog {
  fun d(vararg messages: Any) = Log.d(TAG, messages.joinToString(" ") { "$it" })
  fun e(vararg messages: Any) = Log.e(TAG, messages.joinToString(" ") { "$it" })
  fun i(vararg messages: Any) = Log.i(TAG, messages.joinToString(" ") { "$it" })
  fun w(vararg messages: Any) = Log.w(TAG, messages.joinToString(" ") { "$it" })

  private const val TAG = "KasaWidget"
}
