package ai.aadhini.android.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import ai.aadhini.android.databinding.ActivityMainBinding
import ai.aadhini.core.engine.AadhiniEngine
import ai.aadhini.platform.adapter.OfflineTamilProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private val core = AadhiniEngine(OfflineTamilProvider())
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
        binding.tvAppName.text = "⬡ AaNira v01"
        binding.tvSubtitle.text = "Live 3D AI Companion • First Avatar Milestone"
        binding.btnProvider.text = "3D AVATAR PROTOTYPE • TEXT INPUT"
        binding.btnVoice.visibility = View.GONE
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) {
            val tamilResult = textToSpeech.setLanguage(Locale.forLanguageTag("ta-IN"))
            val result = if (tamilResult == TextToSpeech.LANG_MISSING_DATA ||
                tamilResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech.setLanguage(Locale.getDefault())
            } else tamilResult
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            textToSpeech.setSpeechRate(0.95f)
            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = runOnUiThread { setAvatarState("SPEAKING") }
                override fun onDone(utteranceId: String?) = runOnUiThread { setAvatarState("IDLE") }
                override fun onError(utteranceId: String?) = runOnUiThread { setAvatarState("IDLE") }
            })
        }
    }

    private fun setupWebView() {
        webView = binding.webViewAvatar
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                setAvatarState("IDLE")
            }
        }
        webView.loadUrl("file:///android_asset/aanira_v01_live3d.html")
    }

    private fun setAvatarState(state: String) {
        if (::webView.isInitialized) {
            webView.evaluateJavascript("window.aaniraSetState && window.aaniraSetState('$state'.toLowerCase());", null)
        }
    }

    private fun setExpression(expression: String) {
        if (::webView.isInitialized) {
            webView.evaluateJavascript("window.aaniraSetExpression && window.aaniraSetExpression('$expression');", null)
        }
    }

    private fun stopAvatarTalking() {
        // V01 is intentionally text-first; lip-sync will be added after the live avatar milestone.
    }

    private fun setupUI() {
        binding.btnSpeak.setOnClickListener { simulateQuery("Give me a happy status") }
        binding.btnDecision.setOnClickListener { simulateQuery("I am thinking about our next step") }
        binding.btnMemory.setOnClickListener { showMemoryState() }
        binding.btnAgents.setOnClickListener { simulateQuery("Hello, how are you?") }
        binding.btnProvider.setOnClickListener { setExpression("neutral") }
        binding.btnSend.setOnClickListener { sendTypedMessage() }
        binding.etChatInput.setOnEditorActionListener { _, _, _ -> sendTypedMessage(); true }
        binding.btnVoice.setOnClickListener { startVoiceInput() }
    }

    private fun sendTypedMessage() {
        val input = binding.etChatInput.text.toString().trim()
        if (input.isEmpty()) return
        binding.etChatInput.text?.clear()
        simulateQuery(input)
    }

    private fun startVoiceInput() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO)
            return
        }
        setAvatarState("LISTENING")
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ta-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "AaNira-விடம் பேசுங்கள்")
        }
        startActivityForResult(intent, REQUEST_VOICE_INPUT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_VOICE_INPUT) return
        if (resultCode != RESULT_OK) {
            setAvatarState("IDLE")
            return
        }
        val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim().orEmpty()
        if (spoken.isNotEmpty()) {
            binding.etChatInput.setText(spoken)
            simulateQuery(spoken)
        } else setAvatarState("IDLE")
    }

    private fun checkCoreStatus() {
        binding.tvStatus.text = "AaNira v01 • First milestone\n\nLive procedural 3D character prototype.\nText-first interaction.\nAI provider integration is not part of this milestone."
    }

    private fun detectExpression(input: String): String {
        val text = input.lowercase()
        return when {
            listOf("romantic", "love", "kiss", "baby", "chellam", "காதல்", "செல்லம்", "முத்தம்").any { it in text } -> "romantic"
            listOf("sad", "sorry", "miss you", "கவலை", "வருத்தம்").any { it in text } -> "shy"
            listOf("happy", "great", "super", "சந்தோஷம்", "அருமை", "சூப்பர்").any { it in text } -> "happy"
            listOf("think", "why", "எப்படி", "யோசனை").any { it in text } -> "thinking"
            else -> "neutral"
        }
    }

    private fun simulateQuery(input: String) {
        binding.tvStatus.text = "AaNira is reacting..."
        stopAvatarTalking()
        setExpression(detectExpression(input))
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { core.process(input) }
                binding.tvStatus.text = "$response\n\nAaNira v01 • Procedural live 3D avatar"
            } catch (error: Exception) {
                binding.tvStatus.text = "Local demo response unavailable.\n\n" + (error.message ?: "Unknown error")
                setExpression("thinking")
            }
        }
    }

    private fun speak(text: String) {
        // Voice output is deliberately deferred: V01 focuses on typed input and visible reactions.
    }

    private fun buildStateReport(response: String): String = response

    private fun showMemoryState() {
        val memories = core.getMemories()
        val status = if (memories.isEmpty()) "Memory Engine\n\nNo memories stored yet."
        else "Memory Engine\n\nStored: ${memories.size}"
        binding.tvStatus.text = status
        setExpression("thinking")
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}
