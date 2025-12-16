package com.nspcreativehub.autoshorts

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import java.net.URLDecoder

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    
    // File upload callback
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    
    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            Toast.makeText(this, "Storage permission needed for video upload", Toast.LENGTH_LONG).show()
        }
    }
    
    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            fileUploadCallback?.onReceiveValue(arrayOf(uri))
        } else {
            fileUploadCallback?.onReceiveValue(null)
        }
        fileUploadCallback = null
    }
    
    // Multi-file picker for more flexibility
    private val multiFilePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            // Take persistent permission
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            fileUploadCallback?.onReceiveValue(arrayOf(uri))
        } else {
            fileUploadCallback?.onReceiveValue(null)
        }
        fileUploadCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        
        setupWebView()
        checkPermissions()
        
        // Load the web app
        // TODO: Update this URL after deploying frontend to Vercel
        webView.loadUrl(WEB_APP_URL)
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            // Enable JavaScript (required for React/Next.js)
            javaScriptEnabled = true
            
            // Enable DOM storage for localStorage
            domStorageEnabled = true
            
            // Enable file access
            allowFileAccess = true
            allowContentAccess = true
            
            // Enable media playback
            mediaPlaybackRequiresUserGesture = false
            
            // Enable zoom controls
            builtInZoomControls = true
            displayZoomControls = false
            
            // Improve loading performance
            cacheMode = WebSettings.LOAD_DEFAULT
            
            // Enable mixed content (http resources on https page) - for development
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            
            // User agent (identify as mobile browser)
            userAgentString = "$userAgentString AutoShortsApp/1.0"
        }
        
        // Handle page loading events
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }
            
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    // Show error page for main frame errors
                    view?.loadData(
                        """
                        <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1">
                            <style>
                                body { 
                                    font-family: sans-serif; 
                                    padding: 40px; 
                                    text-align: center;
                                    background: #1a1a2e;
                                    color: white;
                                }
                                h1 { color: #e94560; }
                                button {
                                    background: #e94560;
                                    color: white;
                                    border: none;
                                    padding: 15px 30px;
                                    border-radius: 8px;
                                    font-size: 16px;
                                    margin-top: 20px;
                                }
                            </style>
                        </head>
                        <body>
                            <h1>⚠️ Connection Error</h1>
                            <p>Unable to connect to AutoShorts server.</p>
                            <p>Please check your internet connection.</p>
                            <button onclick="location.reload()">Retry</button>
                        </body>
                        </html>
                        """.trimIndent(),
                        "text/html",
                        "UTF-8"
                    )
                }
            }
        }
        
        // Handle file uploads and JavaScript dialogs
        webView.webChromeClient = object : WebChromeClient() {
            // Progress indicator
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
            }
            
            // File upload handler (modern API)
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                // Cancel any existing callback
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = filePathCallback
                
                // Check for video/* accept type
                val acceptTypes = fileChooserParams?.acceptTypes ?: arrayOf("*/*")
                val isVideo = acceptTypes.any { it.startsWith("video/") || it == "*/*" }
                
                if (isVideo) {
                    // Use document picker for videos
                    try {
                        multiFilePickerLauncher.launch(arrayOf("video/*"))
                    } catch (e: Exception) {
                        // Fallback to simple content picker
                        filePickerLauncher.launch("video/*")
                    }
                } else {
                    filePickerLauncher.launch("*/*")
                }
                
                return true
            }
            
            // Handle JavaScript alerts
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                result?.confirm()
                return true
            }
        }
        
        // Handle file downloads
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
            downloadFile(url, userAgent, contentDisposition, mimeType)
        }
    }
    
    private fun downloadFile(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String
    ) {
        try {
            // Extract filename from Content-Disposition or URL
            var fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            
            // Decode URL-encoded filename
            fileName = try {
                URLDecoder.decode(fileName, "UTF-8")
            } catch (e: Exception) {
                fileName
            }
            
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setMimeType(mimeType)
                addRequestHeader("User-Agent", userAgent)
                setTitle(fileName)
                setDescription("Downloading video...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
            
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            
            Toast.makeText(this, "Download started: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    
    // Handle back button - navigate back in WebView if possible
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    
    companion object {
        // TODO: Update this URL after deploying frontend to Vercel
        private const val WEB_APP_URL = "https://autoshorts.vercel.app"
    }
}
