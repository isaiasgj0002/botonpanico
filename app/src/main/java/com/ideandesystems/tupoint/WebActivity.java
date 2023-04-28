package com.ideandesystems.tupoint;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
public class WebActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        String url = getIntent().getStringExtra("url");
        String correo = getIntent().getStringExtra("correo");
        String password = getIntent().getStringExtra("password");
        WebView webView = (WebView) findViewById(R.id.web);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // La página se ha cargado completamente
                Toast.makeText(WebActivity.this, "Bienvenido a Tu Point", Toast.LENGTH_SHORT).show();
            }
        });
        String postData = "user="+correo+"&pass="+password;
        webView.postUrl(url, postData.getBytes());
    }
}