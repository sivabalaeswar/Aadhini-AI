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
            simulateQuery("Check memory engine")
        }
        binding.btnAgents.setOnClickListener {
            simulateQuery("List active agents")
        }
    }

    private fun simulateQuery(input: String) {
        binding.tvStatus.text = "Processing..."
        webView.evaluateJavascript("avatarTalk(true)", null)

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                core.process(input)
            }
            val intent = core.getLastIntent()
            val intentLabel = intent?.type?.name ?: "UNKNOWN"

            binding.tvStatus.text = "$response\n\nIntent: $intentLabel"
            webView.evaluateJavascript("avatarTalk(false)", null)
        }
    }
}
