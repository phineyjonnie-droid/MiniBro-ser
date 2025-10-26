package com.veestores.minibrowser

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.concurrent.thread

class AIActivity : AppCompatActivity() {
    private lateinit var promptInput: EditText
    private lateinit var scriptInput: EditText
    private lateinit var outputView: TextView
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai)

        promptInput = findViewById(R.id.input_prompt)
        scriptInput = findViewById(R.id.input_script)
        outputView = findViewById(R.id.ai_output)

        promptInput.setText(AIStorage.getPrompt(this))
        scriptInput.setText(AIStorage.getScript(this))

        findViewById<Button>(R.id.btn_save_ai).setOnClickListener {
            AIStorage.setPrompt(this, promptInput.text.toString())
            AIStorage.setScript(this, scriptInput.text.toString())
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_run_script).setOnClickListener {
            val prompt = promptInput.text.toString()
            val script = scriptInput.text.toString()
            if (prompt.isBlank()) { Toast.makeText(this, "Enter a prompt first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            runAiRequest(prompt, script)
        }
    }

    private fun runAiRequest(prompt: String, script: String) {
        val url = SettingsStore.getAiEndpoint(this).ifBlank { "https://your-backend.example.com/ai" }
        val json = JSONObject().apply { put("prompt", prompt); put("script", script) }
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val req = Request.Builder().url(url).post(body).build()
        thread {
            try {
                client.newCall(req).execute().use { resp ->
                    val text = resp.body?.string() ?: ""
                    runOnUiThread { outputView.text = text }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "AI request failed: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
    }
}
