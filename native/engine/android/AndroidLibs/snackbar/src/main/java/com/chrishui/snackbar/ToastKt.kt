package com.chrishui.snackbar

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout.LayoutParams
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.BaseTransientBottomBar.Duration

object ToastKt {
    const val LENGTH_SHORT = Toast.LENGTH_SHORT;
    const val LENGTH_LONG = Toast.LENGTH_LONG;

    /**
     * ToastKt.make(context, "message", ToastKt.LENGTH_LONG).show();
     */
    @JvmStatic
    fun make(
        context: Context, text: CharSequence, @Duration duration: Int = LENGTH_LONG
    ): Toast {
        fun heightPixels(context: Context): Int {
            return context.resources.displayMetrics.heightPixels
        }

        return Toast(context).apply {
            view = TextView(context).apply {
                layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    gravity = Gravity.CENTER
                }

                background = GradientDrawable().apply {
                    cornerRadius = 100F
                    setColor(Color.parseColor("#E6FFFFFF"))
                    setStroke(1, Color.parseColor("#1A000000"))
                }

                setPadding(80, 40, 80, 40)
                setTextColor(Color.BLACK)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                this.text = text
                isEnabled = false
            }
            setGravity(Gravity.BOTTOM, 0, heightPixels(context) / 2)
            this.duration = duration
        }
    }
}
