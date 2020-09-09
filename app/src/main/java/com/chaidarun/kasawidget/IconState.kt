package com.chaidarun.kasawidget

import androidx.annotation.DrawableRes

enum class IconState(@DrawableRes val resId: Int) {
  LOADING(R.drawable.ic_power_yellow),
  OFF(R.drawable.ic_power_gray),
  ON(R.drawable.ic_power_green),
}
