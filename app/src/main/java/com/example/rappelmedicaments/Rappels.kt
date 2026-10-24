package com.example.rappelmedicaments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

data class Rappel(val h: Int, val m: Int, val nom: String) {
    val code get() = h * 100 + m
    fun cle() = "%02d:%02d|%s".format(h, m, nom)
}

object Rappels {
    private fun prefs(c: Context) = c.getSharedPreferences("rappels", Context.MODE_PRIVATE)

    fun charger(c: Context): List<Rappel> =
        prefs(c).getStringSet("liste", emptySet())!!.mapNotNull {
            val (t, n) = it.split("|", limit = 2)
            val (h, m) = t.split(":")
            Rappel(h.toInt(), m.toInt(), n)
        }.sortedBy { it.code }

    private fun sauver(c: Context, l: List<Rappel>) =
        prefs(c).edit().putStringSet("liste", l.map { it.cle() }.toSet()).apply()

    fun ajouter(c: Context, r: Rappel) {
        sauver(c, charger(c).filter { it.code != r.code } + r)
        planifier(c, r)
    }

    fun supprimer(c: Context, r: Rappel) {
        sauver(c, charger(c) - r)
        c.getSystemService(AlarmManager::class.java).cancel(pending(c, r))
    }

    private fun pending(c: Context, r: Rappel) = PendingIntent.getBroadcast(
        c, r.code,
        Intent(c, AlarmReceiver::class.java)
            .putExtra("h", r.h).putExtra("m", r.m).putExtra("nom", r.nom),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun planifier(c: Context, r: Rappel) {
        val am = c.getSystemService(AlarmManager::class.java)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, r.h); set(Calendar.MINUTE, r.m)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms())
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending(c, r))
        else
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending(c, r))
    }
}
