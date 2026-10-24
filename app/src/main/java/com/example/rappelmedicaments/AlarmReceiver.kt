package com.example.rappelmedicaments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val r = Rappel(i.getIntExtra("h", 0), i.getIntExtra("m", 0), i.getStringExtra("nom") ?: "Médicament")
        val nm = c.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("meds", "Rappels médicaments", NotificationManager.IMPORTANCE_HIGH)
        )
        val ouvrir = PendingIntent.getActivity(
            c, 0, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        nm.notify(
            r.code,
            NotificationCompat.Builder(c, "meds")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("💊 C'est l'heure de vos médicaments")
                .setContentText(r.nom)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(ouvrir)
                .build()
        )
        // Reprogramme pour le lendemain si le rappel existe toujours
        if (Rappels.charger(c).contains(r)) Rappels.planifier(c, r)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        Rappels.charger(c).forEach { Rappels.planifier(c, it) }
    }
}
