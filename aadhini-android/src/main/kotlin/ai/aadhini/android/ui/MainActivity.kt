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
            val result = textToSpeech.setLanguage(Locale.getDefault())
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            textToSpeech.setSpeechRate(0.95f)
            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    runOnUiThread { setAvatarState("SPEAKING") }
                }

                override fun onDone(utteranceId: String?) {
                    runOnUiThread { setAvatarState("IDLE") }
                }

                override fun onError(utteranceId: String?) {
                    runOnUiThread { setAvatarState("ERROR") }
                }
            })
        }
    }

    private fun setupWebView() {
        webView = binding.webViewAvatar
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.evaluateJavascript(
                    """
                    (function() {
                        window.avatarSetState = function(state) {
                            document.body.dataset.avatarState = state;
                            const ring = document.querySelector('.glow-ring');
                            if (ring) {
                                ring.style.animationDuration = state === 'THINKING' ? '0.8s' : '2s';
                                ring.style.transform = state === 'LISTENING' ? 'scale(1.04)' : 'scale(1)';
                            }
                            if (state === 'SPEAKING') avatarTalk(true);
                            else avatarTalk(false);
                        };
                        window.avatarSetState('IDLE');
                    })();
                    """.trimIndent(), null
                )
            }
        }
        webView.loadUrl("file:///android_asset/avatar.html")
    }

    private fun setAvatarState(state: String) {
        if (::webView.isInitialized) {
            webView.evaluateJavascript("window.avatarSetState && window.avatarSetState('$state');", null)
        }
    }

    private fun setupUI() {
        binding.btnSpeak.setOnClickListener { simulateQuery("Chief, status report") }
        binding.btnDecision.setOnClickListener { simulateQuery("Run decision engine") }
        binding.btnMemory.setOnClickListener { showMemoryState() }
        binding.btnAgents.setOnClickListener { simulateQuery("List active agents") }
        binding.btnProvider.setOnClickListener { toggleProvider() }
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
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to Aadhini")
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
        } else {
            setAvatarState("IDLE")
        }
    }

    private fun toggleProvider() {
        val current = core.getActiveProvider()
        val next = if (current == "demo") "gemini" else "demo"
        if (core.setProvider(next)) {
            updateProviderButton()
            val message = "AI Provider switched to ${next.uppercase()}"
            binding.tvStatus.text = message
            speak(message)
        } else {
            binding.tvStatus.text = "Provider unavailable: ${next.uppercase()}"
            setAvatarState("ERROR")
        }
    }

    private fun updateProviderButton() {
        binding.btnProvider.text = "AI Provider: ${core.getActiveProvider().uppercase()}"
    }

    private fun simulateQuery(input: String) {
        binding.tvStatus.text = "Processing..."
        setAvatarState("THINKING")
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) { core.process(input) }
            binding.tvStatus.text = buildStateReport(response)
            speak(response)
        }
    }

    private fun speak(text: String) {
        if (!ttsReady || text.isBlank()) {
            setAvatarState("IDLE")
            return
        }
        setAvatarState("SPEAKING")
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "aadhini-response")
    }

    private fun buildStateReport(response: String): String {
        val intentLabel = core.getLastIntent()?.type?.name ?: "UNKNOWN"
        val decisionLabel = core.getLastDecisionType()
        val contextLabel = core.getLastContextType()
        val memoryCount = core.getMemoryCount()
        val provider = core.getActiveProvider().uppercase()
        return "$response\n\nIntent   : $intentLabel\nDecision : $decisionLabel\nContext  : $contextLabel\nMemory   : $memoryCount\nProvider : $provider"
    }

    private fun showMemoryState() {
        val memories = core.getMemories()
        val status = if (memories.isEmpty()) {
            "Memory Engine\n\nNo memories stored yet."
        } else {
            val recent = memories.takeLast(5).asReversed().joinToString("\n") { "• ${it.content}" }
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
