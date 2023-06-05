package com.ideandesystems.tupoint;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DenunciaService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private static final int NOTIFICATION_DENUNCIA_ID = 2;
    private String cod_user, rolUsuario;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            cod_user = intent.getExtras().getString("cod_user");
            rolUsuario = intent.getExtras().getString("rol");
        } else {
            getCodUserWithFileTXT();
            rolUsuario = "Usuario";
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startForeground(NOTIFICATION_ID, createNotification());
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i("Aviso", "Ejecutando denuncia service");
                obtenerDenunciasCercanas();
            }
        }, 0, 60000);
        return START_STICKY;
    }
    private void obtenerDenunciasCercanas() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String URL = "https://tupoint.com/denuncia/obtener_denuncias_cercanas.php";
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED
                                    && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED
                                    && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                            LocationRequest locationRequest = LocationRequest.create();
                            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            locationRequest.setInterval(10000);
                            LocationCallback locationCallback = new LocationCallback() {
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    if (locationResult != null) {
                                        Location location = locationResult.getLastLocation();
                                        if (location != null) {
                                            try {
                                                JSONArray data = response.getJSONArray("data");
                                                double latitude = location.getLatitude();
                                                double longitude = location.getLongitude();
                                                save(latitude, longitude, cod_user);
                                                Log.i("Latitud:", String.valueOf(latitude));
                                                Log.i("Longitud:", String.valueOf(longitude));
                                                Log.i("Cod_user", cod_user);
                                                double radio = obtenerRadio();
                                                if (data.length() > 0) {
                                                    for (int i = 0; i < data.length(); i++) {
                                                        JSONObject dato = data.getJSONObject(i);
                                                        verificarDenunciaCercana(dato, latitude, longitude, radio);
                                                    }
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            Log.e("Aviso", "La última ubicación conocida es nula");
                                        }
                                    } else {
                                        Log.e("Aviso", "La última ubicación conocida es nula");
                                    }
                                }
                            };
                            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                        } else {
                            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(@NonNull Location location) {
                                    try {
                                        JSONArray data = response.getJSONArray("data");
                                        if (location != null) {
                                            double latitudUsuario = location.getLatitude();
                                            double longitudUsuario = location.getLongitude();
                                            save(latitudUsuario, longitudUsuario, cod_user);
                                            Log.i("Latitud:", String.valueOf(latitudUsuario));
                                            Log.i("Longitud:", String.valueOf(longitudUsuario));
                                            Log.i("Cod_user", cod_user);
                                            double radio = obtenerRadio();
                                            if (data.length() > 0) {
                                                for (int i = 0; i < data.length(); i++) {
                                                    JSONObject dato = data.getJSONObject(i);
                                                    verificarDenunciaCercana(dato, latitudUsuario, longitudUsuario, radio);
                                                }
                                            }
                                        }
                                    } catch (JSONException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(DenunciaService.this, "No se pudo obtener la última ubicación", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        queue.add(getRequest);
    }
    private void verificarDenunciaCercana(JSONObject dato, double latitudUsuario, double longitudUsuario, double radio) throws JSONException {
        String cod_denuncia = dato.getString("cod_denuncia");
        String codigo_usuario = dato.getString("cod_user");
        double latitud = dato.getDouble("latitud");
        double longitud = dato.getDouble("longitud");
        double distancia = calcularDistancia(latitudUsuario, longitudUsuario, latitud, longitud);
        if (distancia < radio && !codigo_usuario.equals(cod_user)) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                startForeground(NOTIFICATION_DENUNCIA_ID, createNotificationAlert(cod_denuncia, codigo_usuario));
            }else{
                Intent intent1 = new Intent(getApplicationContext(), AlarmaActivity.class);
                intent1.putExtra("cod_usuario", cod_user);
                intent1.putExtra("cod_denuncia", cod_denuncia);
                intent1.putExtra("cod_user_denuncia", codigo_usuario);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
            }
            //stopSelf();
        }
    }

    private Notification createNotificationAlert(String id_denuncia, String codigo_usuario) {
        // Crea un Intent para abrir el Activity deseado
        Intent intent = new Intent(this, DenunciaActivity.class);
        intent.putExtra("cod_denuncia", id_denuncia);
        intent.putExtra("cod_user", codigo_usuario);
        // Crea un PendingIntent para abrir el Activity cuando se haga clic en la notificación
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/raw/alerta");
        String channelId = "ReportNearFoundServiceChannel";
        String channelName = "Foreground Report Alert Service";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(soundUri, audioAttributes);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.baseline_error_outline_24)
                .setContentTitle("ALERTA")
                .setContentText("SE ENCONTRO UNA DENUNCIA CERCA DE TI!")
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
        return builder.build();
    }

    private double obtenerRadio() {
        double radio = 0.2;
        if (rolUsuario.equals("Policia")) {
            radio = 0.5;
        } else if (rolUsuario.equals("Vigilante")) {
            radio = 0.3;
        }
        return radio;
    }
    private Notification createNotification() {
        String channelId = "ForegroundLocationServiceChannel";
        String channelName = "Foreground Location Service";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Tu Point Pánico")
                .setContentText("Buscando denuncias cercanas...")
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true);
        return builder.build();
    }
    private void save(double latitudUsuario, double longitudUsuario, String codigo) {
        String lat = String.valueOf(latitudUsuario);
        String lon = String.valueOf(longitudUsuario);
        String url = "https://tupoint.com/apk/guardar_ubicacion_telefono.php";
        StringRequest telefonoRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    Log.i("Respuesta:", response);
                } else {
                    Log.e("Error", "El servidor no ha enviado ninguna respuesta");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("cod_user", codigo);
                params.put("latitud", lat);
                params.put("longitud", lon);
                // Devolvemos el mapa con los datos
                return params;
            }
        };
        RequestQueue queuque = Volley.newRequestQueue(getApplicationContext());
        queuque.add(telefonoRequest);
    }

    private void getCodUserWithFileTXT() {
        try {
            FileInputStream fileInputStream = openFileInput("datos.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            bufferedReader.readLine();
            // Leer la segunda y tercera línea
            bufferedReader.readLine(); // ignorar la segunda línea
            cod_user = bufferedReader.readLine();
            bufferedReader.close();
        } catch (Exception e) {
            Log.i("Mensaje", "El usuario no se logueo, archivo de texto no existe en almacenamiento");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private double calcularDistancia(double latitudUsuario, double longitudUsuario, double latitud, double longitud) {
        double radioTierra = 6378.0; // Radio de la Tierra en km
        double dLat = Math.toRadians(latitudUsuario - latitud);
        double dLon = Math.toRadians(longitudUsuario - longitud);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(latitudUsuario)) * Math.cos(Math.toRadians(latitud)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return radioTierra * c;
    }
}
