package com.veestores.minibrowser

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {
    private lateinit var tplInput: EditText
    private lateinit var homeInput: EditText
    private lateinit var blockUrlInput: EditText
    private lateinit var aiEndpointInput: EditText
    private lateinit var themeInput: EditText
    private lateinit var dataSaverSwitch: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        tplInput = findViewById(R.id.input_search_tpl)
        homeInput = findViewById(R.id.input_homepage)
        blockUrlInput = findViewById(R.id.input_block_url)
        aiEndpointInput = findViewById(R.id.input_ai_endpoint)
        themeInput = findViewById(R.id.input_theme_mode)
        dataSaverSwitch = findViewById(R.id.switch_data_saver)

        tplInput.setText(SettingsStore.getSearchTemplate(this))
        homeInput.setText(SettingsStore.getHomepage(this))
        blockUrlInput.setText(SettingsStore.getBlocklistUrl(this))
        aiEndpointInput.setText(SettingsStore.getAiEndpoint(this))
        themeInput.setText(SettingsStore.getThemeMode(this))
        dataSaverSwitch.isChecked = SettingsStore.isDataSaverOn(this)

        findViewById<Button>(R.id.btn_import_easylist).setOnClickListener {
            val hosts = BlocklistImporter.importFromAssets(this)
            BlocklistImporter.mergeIntoBlocklist(this, hosts)
            Toast.makeText(this, "Imported ${hosts.size} hosts into local blocklist", Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            SettingsStore.setSearchTemplate(this, tplInput.text.toString().trim())
            SettingsStore.setHomepage(this, homeInput.text.toString().trim())
            SettingsStore.setBlocklistUrl(this, blockUrlInput.text.toString().trim())
            SettingsStore.setAiEndpoint(this, aiEndpointInput.text.toString().trim())
            val mode = themeInput.text.toString().trim().ifEmpty { "system" }
            SettingsStore.setThemeMode(this, mode)
            SettingsStore.setDataSaver(this, dataSaverSwitch.isChecked)
            // apply theme immediately
            ThemeManager.applyThemeMode(this, mode)
            ThemeManager.scheduleAutoCheck(this)
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
