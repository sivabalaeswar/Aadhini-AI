package ai.aadhini.android.ui

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ai.aadhini.android.databinding.ActivityMainBinding
import ai.aadhini.android.core.AadhiniCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val core = AadhiniCore()
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        setupUI()
        updateProviderButton()
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
    }

    private fun toggleProvider() {
        val current = core.getActiveProvider()
        val next = if (current == "demo") "gemini" else "demo"

        if (core.setProvider(next)) {
            updateProviderButton()
            binding.tvStatus.text = "AI Provider switched to ${next.uppercase()}"
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
            webView.evaluateJavascript("avatarTalk(false)", null)
        }
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
        if (memories.isEmpty()) {
            binding.tvStatus.text = "Memory Engine\n\nNo memories stored yet."
            return
        }

        val recent = memories.takeLast(5).asReversed().joinToString("\n") { memory ->
            "• ${memory.content}"
        }

        binding.tvStatus.text = "Memory Engine\n\nStored: ${memories.size}\n\n$recent"
    }
}
