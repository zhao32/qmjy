package com.chrishui.snackbar

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout.LayoutParams
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import com.google.android.material.snackbar.BaseTransientBottomBar.Duration
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout

object SnackbarKt {
    const val LENGTH_SHORT = Snackbar.LENGTH_SHORT;
    const val LENGTH_LONG = Snackbar.LENGTH_LONG;

    /**
     * SnackbarKt.make(view, "message", SnackbarKt.LENGTH_LONG).show();
     */
    @SuppressLint("RestrictedApi")
    @JvmStatic
    fun make(
        view: View, text: CharSequence, @Duration duration: Int = LENGTH_LONG
    ): Snackbar {
        return Snackbar.make(view, "", duration).apply {
            // TextView
            (getView() as SnackbarLayout).addView(TextView(view.context).apply {
                layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    gravity = Gravity.CENTER
                }

                background = GradientDrawable().apply {
                    cornerRadius = 30F
                    setColor(Color.WHITE)
                    setStroke(1, Color.parseColor("#999999"))
                }

                setPadding(60, 40, 60, 40)
                setTextColor(Color.BLACK)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                this.text = text
                isEnabled = false
            })

            // TextView 容器
            getView().apply {
                layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                    gravity = Gravity.CENTER
                    setMargins(150, 250, 150, 250)
                }
                setPadding(0, 0, 0, 0)
                setBackgroundColor(Color.TRANSPARENT)
                isEnabled = false
            }
        }
    }
}
