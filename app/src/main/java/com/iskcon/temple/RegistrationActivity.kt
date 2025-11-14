package com.iskcon.temple

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val webView: WebView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://docs.google.com/forms/d/e/1FAIpQLScqwqMRqrmqVx9EctQ__MI939inorqszAUF8ojnq8rCPewXxQ/viewform?usp=header")
    }
}