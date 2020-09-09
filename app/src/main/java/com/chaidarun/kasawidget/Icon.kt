package com.chaidarun.kasawidget

import androidx.annotation.DrawableRes

enum class Icon(@DrawableRes val resId: Int) {
  ERROR(R.drawable.ic_power_red),
  LOADING(R.drawable.ic_power_yellow),
  OFF(R.drawable.ic_power_gray),
  ON(R.drawable.ic_power_green),
}
