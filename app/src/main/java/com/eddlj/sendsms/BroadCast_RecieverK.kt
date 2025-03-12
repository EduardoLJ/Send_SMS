package com.eddlj.sendsms

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat

class BroadCast_Reciver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            val incomingNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Si la API es 29 o superior, obtenemos el número desde los registros de llamadas
                getLastCallLogNumber(context)
            } else {
                // En versiones antiguas, usamos TelephonyManager.EXTRA_INCOMING_NUMBER
                intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            }

            if (!incomingNumber.isNullOrEmpty()) {
                Log.d("CallReceiver", "Llamada entrante detectada de: $incomingNumber")
                sendAutoReply(context, incomingNumber)
                Toast.makeText(context, "Mensaje enviado a $incomingNumber", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendAutoReply(context: Context, phoneNumber: String) {
        val message = "Hola, en este momento no puedo atender tu llamada. Te contactaré pronto."

        try {
            val smsManager = android.telephony.SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("SMS", "Mensaje enviado a $phoneNumber: $message")
        } catch (e: Exception) {
            Log.e("SMS", "Error al enviar SMS", e)
        }
    }

    private fun getLastCallLogNumber(context: Context): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER),
                null, null, "${CallLog.Calls.DATE} DESC"
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    return it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                }
            }
        } else {
            Log.e("CallReceiver", "Permiso READ_CALL_LOG no concedido")
        }
        return null
    }
}
