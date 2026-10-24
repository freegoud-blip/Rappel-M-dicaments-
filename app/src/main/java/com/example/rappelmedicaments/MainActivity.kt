package com.example.rappelmedicaments

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*

class MainActivity : Activity() {
    private lateinit var liste: LinearLayout

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (Build.VERSION.SDK_INT >= 33)
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        if (Build.VERSION.SDK_INT >= 31 &&
            !getSystemService(AlarmManager::class.java).canScheduleExactAlarms())
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))

        val titre = TextView(this).apply { text = "💊 Rappel médicaments"; textSize = 24f }
        val nom = EditText(this).apply { hint = "Médicament (ex. Metformine 500 mg)" }
        val ajout = Button(this).apply {
            text = "Ajouter un rappel quotidien"
            setOnClickListener {
                TimePickerDialog(this@MainActivity, { _, h, m ->
                    val n = nom.text.toString().ifBlank { "Médicament" }
                    Rappels.ajouter(this@MainActivity, Rappel(h, m, n))
                    nom.text.clear()
                    afficher()
                }, 8, 0, true).show()
            }
        }
        liste = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
            addView(titre); addView(nom); addView(ajout)
            addView(ScrollView(this@MainActivity).apply { addView(liste) })
        })
        afficher()
    }

    private fun afficher() {
        liste.removeAllViews()
        Rappels.charger(this).forEach { r ->
            liste.addView(TextView(this).apply {
                text = "%02d:%02d — %s".format(r.h, r.m, r.nom)
                textSize = 20f
                setPadding(0, 32, 0, 32)
                setOnLongClickListener {
                    AlertDialog.Builder(this@MainActivity)
                        .setMessage("Supprimer ce rappel ?")
                        .setPositiveButton("Supprimer") { _, _ -> Rappels.supprimer(this@MainActivity, r); afficher() }
                        .setNegativeButton("Annuler", null).show()
                    true
                }
            })
        }
        if (liste.childCount > 0) liste.addView(TextView(this).apply { text = "Appui long sur un rappel pour le supprimer."; textSize = 12f })
    }
}
