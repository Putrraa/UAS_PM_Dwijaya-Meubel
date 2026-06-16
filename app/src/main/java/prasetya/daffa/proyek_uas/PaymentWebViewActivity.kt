package prasetya.daffa.proyek_uas

import android.app.Activity
import android.graphics.Bitmap
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import prasetya.daffa.proyek_uas.databinding.ActivityPaymentWebviewBinding
import java.util.Locale

class PaymentWebViewActivity : AppCompatActivity() {

    private lateinit var b: ActivityPaymentWebviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityPaymentWebviewBinding.inflate(layoutInflater)
        setContentView(b.root)
        enableEdgeToEdge()
        setSupportActionBar(b.toolbarPayment)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        b.toolbarPayment.setNavigationOnClickListener {
            finish()
        }

        val paymentUrl = intent.getStringExtra(EXTRA_PAYMENT_URL).orEmpty()
        if (paymentUrl.isEmpty()) {
            finish()
            return
        }

        setupWebView()
        setupBackHandler()
        b.webViewPayment.loadUrl(paymentUrl)
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (b.webViewPayment.canGoBack()) {
                    b.webViewPayment.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    private fun setupWebView() {
        val webView = b.webViewPayment
        copyWebViewSettings(webView)

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                transport.webView = webView
                resultMsg.sendToTarget()
                return true
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                android.util.Log.d(
                    "MIDTRANS_WEBVIEW",
                    "${consoleMessage?.message()} -- line ${consoleMessage?.lineNumber()}"
                )
                return super.onConsoleMessage(consoleMessage)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val nextUrl = request?.url?.toString().orEmpty()
                if (isPaymentReturnUrl(nextUrl)) {
                    kirimHasilPembayaran(nextUrl)
                    finish()
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                b.progressPayment.visibility = View.VISIBLE
                android.util.Log.d("MIDTRANS_WEBVIEW", "Mulai load: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                b.progressPayment.visibility = View.GONE
                android.util.Log.d("MIDTRANS_WEBVIEW", "Selesai load: $url")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                android.util.Log.e(
                    "MIDTRANS_WEBVIEW",
                    "Error load ${request?.url}: ${error?.description}"
                )
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                android.util.Log.e(
                    "MIDTRANS_WEBVIEW",
                    "HTTP error ${request?.url}: ${errorResponse?.statusCode}"
                )
            }
        }
    }

    private fun copyWebViewSettings(webView: WebView) {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
    }

    private fun isPaymentReturnUrl(url: String): Boolean {
        val lowerUrl = url.lowercase(Locale.getDefault())
        return lowerUrl.contains("finish") ||
                lowerUrl.contains("payment/success") ||
                lowerUrl.contains("transaction_status=settlement") ||
                lowerUrl.contains("transaction_status=capture") ||
                lowerUrl.contains("transaction_status=pending") ||
                lowerUrl.contains("transaction_status=deny") ||
                lowerUrl.contains("transaction_status=expire") ||
                lowerUrl.contains("transaction_status=cancel")
    }

    private fun kirimHasilPembayaran(url: String) {
        val uri = Uri.parse(url)
        val transactionStatus = uri.getQueryParameter("transaction_status")
            ?: uri.getQueryParameter("status")
            ?: ""

        val resultIntent = Intent().apply {
            putExtra(EXTRA_TRANSACTION_STATUS, transactionStatus)
        }

        setResult(Activity.RESULT_OK, resultIntent)
    }

    companion object {
        const val EXTRA_PAYMENT_URL = "extra_payment_url"
        const val EXTRA_TRANSACTION_STATUS = "extra_transaction_status"
    }
}
