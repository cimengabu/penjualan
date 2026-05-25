package com.example.penjualan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : BaseActivity() {

    private lateinit var rgLanguage: RadioGroup
    private lateinit var rbIndo: RadioButton
    private lateinit var rbEnglish: RadioButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private lateinit var switchNightMode: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        rgLanguage = findViewById(R.id.rgLanguage)
        rbIndo = findViewById(R.id.rbIndo)
        rbEnglish = findViewById(R.id.rbEnglish)
        btnSave = findViewById(R.id.btnSave)
        btnLogout = findViewById(R.id.btnLogout)
        switchNightMode = findViewById(R.id.switchNightMode)

        // Set checked based on current
        val currentLang = LocaleHelper.getLanguage(this)
        if (currentLang == "en") {
            rbEnglish.isChecked = true
        } else {
            rbIndo.isChecked = true
        }

        // Night mode logic
        val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val defaultNight = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val isNightMode = sharedPref.getBoolean("NightMode", defaultNight)
        switchNightMode.isChecked = isNightMode

        switchNightMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("NightMode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        btnSave.setOnClickListener {
            val newLang = if (rbEnglish.isChecked) "en" else "in"
            if (newLang != currentLang) {
                LocaleHelper.setLocale(this, newLang)
                // Restart to apply changes
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                finish()
            }
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
