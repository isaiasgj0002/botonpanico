package com.ideandesystems.tupoint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
    private String cod_user, rolUsuario;
    private FusedLocationProviderClient mFusedLocationClient;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            cod_user = intent.getExtras().getString("cod_user");
            rolUsuario = intent.getExtras().getString("rol");
        }else{
            getCodUserWithFileTXT();
            rolUsuario = "Usuario";
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Aquí puedes ejecutar tu consulta a la base de datos
                // por ejemplo, usando Volley
                // y verificar si hay alguna alerta cerca de ti
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String URL = "https://tupoint.com/denuncia/obtener_denuncias_cercanas.php";
                // Crear la solicitud GET
                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, URL, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Obtener la latitud y longitud del usuario actual
                                //LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(@NonNull Location location) {
                                        try{
                                            JSONArray data = response.getJSONArray("data");
                                            if(location!=null){
                                                double latitudUsuario = location.getLatitude();
                                                double longitudUsuario = location.getLongitude();
                                                save(latitudUsuario,longitudUsuario, cod_user);
                                                Log.i("Latitud:", String.valueOf(latitudUsuario));
                                                Log.i("Longitud:", String.valueOf(longitudUsuario));
                                                Log.i("Cod_user",cod_user);
                                                // Definir el radio de detección en kilómetros
                                                double radio = 0.2;
                                                if(rolUsuario.equals("Policia")){
                                                    radio = 0.5;
                                                }else if(rolUsuario.equals("Vigilante")){
                                                    radio = 0.3;
                                                }
                                                //String codigo = ((Activity) PrncipalActivity.class).getIntent().getExtras().get("cod_user").toString();
                                                // Recorrer los datos obtenidos
                                                if(data.length()>0){
                                                    for (int i = 0; i < data.length(); i++) {
                                                        JSONObject dato = data.getJSONObject(i);
                                                        String cod_denuncia = dato.getString("cod_denuncia");
                                                        String codigo_usuario = dato.getString("cod_user");
                                                        double latitud = dato.getDouble("latitud");
                                                        double longitud = dato.getDouble("longitud");
                                                        // Hacer algo con la latitud y longitud obtenidos
                                                        double distancia = calcularDistancia(latitudUsuario, longitudUsuario, latitud, longitud);
                                                        if (distancia < radio && !codigo_usuario.equals(cod_user)) {
                                                            // Hacer algo con la latitud y longitud obtenidos
                                                            Intent intent1 = new Intent(getApplicationContext(),AlarmaActivity.class);
                                                            intent1.putExtra("cod_usuario",cod_user);
                                                            intent1.putExtra("cod_denuncia",cod_denuncia);
                                                            intent1.putExtra("cod_user_denuncia",codigo_usuario);
                                                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent1);
                                                            stopSelf();
                                                        }
                                                    }
                                                }
                                            }
                                        }catch (JSONException ex){
                                            ex.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                queue.add(getRequest);
            }
        }, 0, 60000);
        return START_STICKY;
    }
    private void save(double latitudUsuario, double longitudUsuario, String codigo) {
        String lat = String.valueOf(latitudUsuario);
        String lon = String.valueOf(longitudUsuario);
        String url="https://tupoint.com/apk/guardar_ubicacion_telefono.php";
        StringRequest telefonoRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    Log.i("Respuesta:",response);
                }else{
                    Log.e("Error","El servidor no ha enviado ninguna respuesta");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
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
            String thirdLine = bufferedReader.readLine();
            cod_user = thirdLine;
            bufferedReader.close();
        } catch (Exception e) {
            Log.i("Mensaje","El usuario no se logueo, archivo de texto no existe en almacenamiento");
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
        double distancia = radioTierra * c;
        return distancia;
    }
}
