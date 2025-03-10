package com.eddlj.sendsms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.widget.Toast

class BroadCast_Reciver : BroadcastReceiver() {
    var context: Context? = null
    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            Toast.makeText(context, "Call Incoming", Toast.LENGTH_SHORT).show()
            val incomingCallerNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            if (incomingCallerNumber != null) {
                val intent1 = Intent(context, MainActivity::class.java)
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent1.putExtra("number", incomingCallerNumber)
                context.startActivity(intent1)
            }
        }
    }
}