package ai.aadhini.android.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import ai.aadhini.android.databinding.ActivityMainBinding
import ai.aadhini.android.core.AadhiniCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private val core = AadhiniCore()
    private lateinit var webView: WebView
    private lateinit var textToSpeech: TextToSpeech
    private var ttsReady = false

    companion object {
        private const val REQUEST_RECORD_AUDIO = 1001
        private const val REQUEST_VOICE_INPUT = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textToSpeech = TextToSpeech(this, this)
        setupWebView()
        setupUI()
        updateProviderButton()
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) {
            textToSpeech.language = Locale.getDefault()
            textToSpeech.setSpeechRate(0.95f)
        }
    }

    private fun setupWebView() {
        webView = binding.webViewAvatar
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl("file:///android_asset/avatar.html")
    }

    private fun setupUI() {
        binding.btnSpeak.setOnClickListener {
            simulateQuery("Chief, status report")
        }
        binding.btnDecision.setOnClickListener {
            simulateQuery("Run decision engine")
        }
        binding.btnMemory.setOnClickListener {
            showMemoryState()
        }
        binding.btnAgents.setOnClickListener {
            simulateQuery("List active agents")
        }
        binding.btnProvider.setOnClickListener {
            toggleProvider()
        }
        binding.btnSend.setOnClickListener {
            sendTypedMessage()
        }
        binding.etChatInput.setOnEditorActionListener { _, _, _ ->
            sendTypedMessage()
            true
        }
        binding.btnVoice.setOnClickListener {
            startVoiceInput()
        }
    }

    private fun sendTypedMessage() {
        val input = binding.etChatInput.text.toString().trim()
        if (input.isEmpty()) return
        binding.etChatInput.text?.clear()
        simulateQuery(input)
    }

    private fun startVoiceInput() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO
            )
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to Aadhini")
        }
        startActivityForResult(intent, REQUEST_VOICE_INPUT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_VOICE_INPUT || resultCode != RESULT_OK) return

        val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.trim()
            .orEmpty()

        if (spoken.isNotEmpty()) {
            binding.etChatInput.setText(spoken)
            simulateQuery(spoken)
        }
    }

    private fun toggleProvider() {
        val current = core.getActiveProvider()
        val next = if (current == "demo") "gemini" else "demo"

        if (core.setProvider(next)) {
            updateProviderButton()
            binding.tvStatus.text = "AI Provider switched to ${next.uppercase()}"
            speak("AI Provider switched to ${next.uppercase()}")
        } else {
            binding.tvStatus.text = "Provider unavailable: ${next.uppercase()}"
        }
    }

    private fun updateProviderButton() {
        binding.btnProvider.text = "AI Provider: ${core.getActiveProvider().uppercase()}"
    }

    private fun simulateQuery(input: String) {
        binding.tvStatus.text = "Processing..."
        webView.evaluateJavascript("avatarTalk(true)", null)

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                core.process(input)
            }
            binding.tvStatus.text = buildStateReport(response)
            speak(response)
            webView.evaluateJavascript("avatarTalk(false)", null)
        }
    }

    private fun speak(text: String) {
        if (!ttsReady || text.isBlank()) return
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "aadhini-response")
    }

    private fun buildStateReport(response: String): String {
        val intent = core.getLastIntent()
        val intentLabel = intent?.type?.name ?: "UNKNOWN"
        val decisionLabel = core.getLastDecisionType()
        val contextLabel = core.getLastContextType()
        val memoryCount = core.getMemoryCount()
        val provider = core.getActiveProvider().uppercase()

        return "$response\n\n" +
            "Intent   : $intentLabel\n" +
            "Decision : $decisionLabel\n" +
            "Context  : $contextLabel\n" +
            "Memory   : $memoryCount\n" +
            "Provider : $provider"
    }

    private fun showMemoryState() {
        val memories = core.getMemories()
        val status = if (memories.isEmpty()) {
            "Memory Engine\n\nNo memories stored yet."
        } else {
            val recent = memories.takeLast(5).asReversed().joinToString("\n") { memory ->
                "• ${memory.content}"
            }
            "Memory Engine\n\nStored: ${memories.size}\n\n$recent"
        }
        binding.tvStatus.text = status
        speak(status)
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}
