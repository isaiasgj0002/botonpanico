package com.ideandesystems.tupoint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PermisosActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permisos);
        Button btnaceptar = findViewById(R.id.btnaceptarpermiso);
        Button btnrechazar = findViewById(R.id.btnrechazarpermiso);
        btnaceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificarPermisos();
            }
        });
        btnrechazar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity();
            }
        });
        solicitarOtrosPermisos();
    }

    private void solicitarOtrosPermisos() {
        if (ActivityCompat.checkSelfPermission(PermisosActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si no ha concedido permisos, solicitarlos
            ActivityCompat.requestPermissions(PermisosActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    2);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(PermisosActivity.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        3);
            }
        }
    }

    private void verificarPermisos() {
        // Verificar si los permisos están concedidos
        boolean backgroundLocationPermissionGranted = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        // Si los permisos están concedidos, cerrar el activity actual y abrir otro
        if (backgroundLocationPermissionGranted) {
            // Cerrar el activity actual
            finish();
            // Abrir otro activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            //Log.d("Permisos", "Background Location Permission Granted: " + backgroundLocationPermissionGranted);
            // Si los permisos no están concedidos, solicitarlos
            List<String> permissionsToRequest = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !backgroundLocationPermissionGranted) {
                permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_LOCATION_PERMISSION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            // Verificar si los permisos fueron concedidos
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                // Iniciar el nuevo activity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Debes aceptar los permisos para usar Tu Point - Pánico", Toast.LENGTH_SHORT).show();
            }
        }
    }

}