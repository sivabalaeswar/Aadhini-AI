package ai.aadhini.android.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
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
    private lateinit var assetLoader: WebViewAssetLoader
    private var ttsReady = false

    companion object {
        private const val REQUEST_RECORD_AUDIO = 1001
        private const val REQUEST_VOICE_INPUT = 1002
        private const val AVATAR_URL = "https://appassets.androidplatform.net/assets/aanira_v01_live3d.html"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        textToSpeech = TextToSpeech(this, this)
        setupWebView()
        setupUI()
        binding.tvAppName.text = "⬡ AADHINI"
        binding.tvSubtitle.text = "Private AI Companion • Live 3D Avatar"
        binding.btnProvider.text = "AADHINI 3D AVATAR"
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
        assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = true
        webView.settings.setSupportZoom(false)
        webView.settings.builtInZoomControls = false
        webView.settings.displayZoomControls = false
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =
                assetLoader.shouldInterceptRequest(request.url)

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                setAvatarState("IDLE")
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    binding.tvStatus.text = "Aadhini avatar page failed to load: ${error?.description ?: "unknown error"}"
                }
            }

            override fun onRenderProcessGone(view: WebView?, detail: android.webkit.RenderProcessGoneDetail?): Boolean {
                binding.tvStatus.text = "3D renderer stopped unexpectedly. Reload Aadhini."
                return true
            }
        }
        webView.loadUrl(AVATAR_URL)
    }

    private fun setAvatarState(state: String) {
        if (::webView.isInitialized) {
            webView.evaluateJavascript("window.aadhiniSetState && window.aadhiniSetState('$state'.toLowerCase());", null)
        }
    }

    private fun setExpression(expression: String) {
        if (::webView.isInitialized) {
            webView.evaluateJavascript("window.aadhiniSetExpression && window.aadhiniSetExpression('$expression');", null)
        }
    }

    private fun triggerAvatarAction(action: String) {
        if (!::webView.isInitialized) return
        val function = when (action) {
            "kiss" -> "aadhiniKiss"
            "wink" -> "aadhiniWink"
            else -> return
        }
        webView.evaluateJavascript("window.$function && window.$function();", null)
    }

    private fun stopAvatarTalking() {}

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
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Aadhini-யிடம் பேசுங்கள்")
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
        binding.tvStatus.text = "Aadhini is reacting..."
        stopAvatarTalking()
        val normalized = input.lowercase()
        setExpression(detectExpression(input))
        when {
            listOf("blow a kiss", "send a kiss", "kiss me", "முத்தம் கொடு", "kiss").any { it in normalized } -> triggerAvatarAction("kiss")
            listOf("wink", "கண் சிமிட்டு", "கண்ணட").any { it in normalized } -> triggerAvatarAction("wink")
        }
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { core.process(input) }
                binding.tvStatus.text = "$response\n\nAadhini • Live 3D avatar"
            } catch (error: Exception) {
                binding.tvStatus.text = "Local demo response unavailable.\n\n" + (error.message ?: "Unknown error")
                setExpression("thinking")
            }
        }
    }

    private fun showMemoryState() {
        val memories = core.getMemories()
        val status = if (memories.isEmpty()) "Memory Engine\n\nNo memories stored yet."
        else "Memory Engine\n\nStored: ${memories.size}"
        binding.tvStatus.text = status
        setExpression("thinking")
    }

    override fun onPause() {
        if (::webView.isInitialized) webView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) webView.onResume()
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.removeAllViews()
            webView.destroy()
        }
        super.onDestroy()
    }
}
