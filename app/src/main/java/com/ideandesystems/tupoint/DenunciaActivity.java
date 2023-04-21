package com.ideandesystems.tupoint;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class DenunciaActivity extends AppCompatActivity {
    WebView www;
    String iddenuncia, idusuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denuncia);
        www = findViewById(R.id.www);
        iddenuncia = getIntent().getStringExtra("cod_denuncia");
        idusuario = getIntent().getStringExtra("cod_user");
        String enlace = "https://tupoint.com/denuncia/denuncia_botones.php?datos=" + iddenuncia + "-" + idusuario;
        www.loadUrl(enlace);
        www.getSettings().setJavaScriptEnabled(true);
        www.getSettings().setLoadWithOverviewMode(true);
        www.getSettings().setUseWideViewPort(true);
        www.setWebViewClient(new WebViewClient());
    }
}