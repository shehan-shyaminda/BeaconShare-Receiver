package com.example.test_customer

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import com.github.ybq.android.spinkit.SpinKitView
import com.github.ybq.android.spinkit.style.Pulse

class LoadingDialog(private val activity: Activity) {
    private val dialog: Dialog = Dialog(activity)

    init {
        val view: View = LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null)
        dialog.setContentView(view)

        val spinKitView = view.findViewById<SpinKitView>(R.id.spin_kit)
        val pulse = Pulse()
        spinKitView.setIndeterminateDrawable(pulse)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
    }

    fun show() {
        if (!activity.isFinishing && !activity.isDestroyed) {
            dialog.show()
        }
    }

    fun dismiss() {
        if (dialog.isShowing && !activity.isFinishing && !activity.isDestroyed) {
            dialog.dismiss()
        }
    }
}