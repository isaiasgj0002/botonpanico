package com.ideandesystems.tupoint;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
public class RegistroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        ActionBar toolbar = getSupportActionBar();
        if(toolbar!=null){
            toolbar.setTitle("Registrate");
            toolbar.setDisplayHomeAsUpEnabled(true);
        }
        WebView registro = findViewById(R.id.registroweb);
        registro.loadUrl("https://tupoint.com/reciclaje/formulario_registro.php");
        registro.getSettings().setJavaScriptEnabled(true);
        registro.setWebViewClient(new WebViewClient());
        registro.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                AlertDialog alertDialog = new AlertDialog.Builder(RegistroActivity.this).create();
                alertDialog.setTitle("Alerta");
                alertDialog.setMessage(message);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                        Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
                alertDialog.show();
                return true;
            }
        });

    }
}