package com.ideandesystems.tupoint;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.Manifest;
import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
public class PrncipalActivity extends AppCompatActivity {
    private FusedLocationProviderClient mFusedLocationClient;
    TextView correoView, latView, longView, titulo;
    Button btncerrarsesion, openlink;
    ImageButton btnactivar, btnphone;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prncipal);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("SEGURIDAD CIUDADANA");
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.orange)));
        }
        correoView = findViewById(R.id.txtcorreo);
        latView = findViewById(R.id.txtlat);
        longView = findViewById(R.id.txtlong);
        titulo = findViewById(R.id.txtlast);
        btnactivar = findViewById(R.id.btnlocate);
        btnphone = findViewById(R.id.getphone);
        btncerrarsesion = findViewById(R.id.btncerrarsesion);
        progressBar = findViewById(R.id.progressbarreportar);
        openlink = findViewById(R.id.openlinkprincipal);
        //Obtenemos la variable correo que se envia al validar los datos de el usuario
        String correoUser = getIntent().getExtras().get("correo").toString();
        obtenerNombre(correoUser);
        String cod_user = getIntent().getExtras().get("cod_user").toString();
        verificarUbicacionActivada();
        Intent intent = new Intent(this, DenunciaService.class);
        intent.putExtra("cod_user", cod_user);
        startService(intent);
        btnphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    // Si no se pudo obtener el número de teléfono, mostrar un AlertDialog con un EditText para que el usuario ingrese su número
                    AlertDialog.Builder builder = new AlertDialog.Builder(PrncipalActivity.this);
                    builder.setTitle("Ingrese su número de teléfono");
                    final EditText editText = new EditText(PrncipalActivity.this);
                    builder.setView(editText);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String phoneNumber = editText.getText().toString();
                            // Guardar el número de teléfono ingresado por el usuario y continuar con el proceso normal de la aplicación
                            savePhone(phoneNumber,cod_user);
                        }
                    });
                    builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Cerrar el AlertDialog y volver a la pantalla anterior o hacer otra acción
                            Log.i("aviso","se cancelo");
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
            }
        });
        openlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://www.tupoint.com";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        btnactivar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Verificamos si el usuario dio permiso a la app para acceder a su ubicacion
                if (ActivityCompat.checkSelfPermission(PrncipalActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Si no ha concedido permisos, solicitarlos
                    ActivityCompat.requestPermissions(PrncipalActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1);
                    return;
                }
                verificarUbicacionActivada();
                // Obtener la última ubicación conocida
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(PrncipalActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // La última ubicación conocida puede ser nula
                                if (location != null) {
                                    // Mostrar la ubicación en un Toast
                                    Toast.makeText(PrncipalActivity.this, "Latitud: " + location.getLatitude() + ", Longitud: " + location.getLongitude(), Toast.LENGTH_LONG).show();
                                    guardarUbicacionDB(location.getLatitude(), location.getLongitude());
                                    progressBar.setVisibility(View.VISIBLE);
                                }
                            }
                        });
            }
        });
        boolean isServiceRunning = isMyServiceRunning(DenunciaService.class);
        if (isServiceRunning) {
            Log.i("info","Se esta ejecuntando");
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
        btncerrarsesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Para cerrar la sesion se debe eliminar el archivo txt con las credenciales, y se debe redirecionar
                //al activity de Login
                File archivotxt = new File(getFilesDir(), "datos.txt");
                if (archivotxt.delete()) {
                    // El archivo fue eliminado exitosamente
                    Log.d("TAG", "El archivo fue eliminado exitosamente.");
                } else {
                    // Ocurrió un error al intentar eliminar el archivo
                    Log.d("TAG", "No se pudo eliminar el archivo.");
                }
                //Abrimos la pantalla de login
                Intent intent = new Intent(PrncipalActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    private void savePhone(String phoneNumber, String cod_user) {
        String url = "https://tupoint.com/apk/guardar_telefono.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("SUCCES")){
                    Toast.makeText(PrncipalActivity.this, "Tu telefono fue registrado", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(PrncipalActivity.this,
                            "No se pudo guardar tu telefono, intentalo mas tarde, si el problema persiste, contactate con nosotros",
                            Toast.LENGTH_SHORT).show();
                    Log.e("ERROR EN LA CONSULTA",response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(PrncipalActivity.this,
                        "No se pudo guardar tu telefono, intentalo mas tarde, si el problema persiste, contactate con nosotros",
                        Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("cod_user",cod_user);
                params.put("telefono",phoneNumber);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(PrncipalActivity.this);
        queue.add(request);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //timer.cancel();
    }

    private void verificarUbicacionActivada() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Mostrar alerta para solicitar que active su ubicación
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ubicación desactivada");
            builder.setMessage("Por favor active su ubicación para continuar");
            builder.setCancelable(false);
            builder.setPositiveButton("Activar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finishAffinity();
                }
            });
            builder.show();
        }
    }
    private void obtenerNombre(String correoUser) {
        String url = "https://tupoint.com/apk/nombreusuario.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    correoView.setText(response+": Si estas en peligro envia tu alerta, presionando el botón");
                }else{
                    correoView.setText("Si estas en peligro envia tu alerta, presionando el botón");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR",error.getMessage());
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();
                params.put("correo",correoUser);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void guardarUbicacionDB(double latitude, double longitude) {
        // Definimos la URL del archivo PHP que procesará la petición
        String url = "https://tupoint.com/denuncia/denuncia.php";
        DecimalFormat formato = new DecimalFormat("#.#################"); // Definimos el patrón para mostrar hasta 18 decimales
        String latitudStr = formato.format(latitude);
        String longitudStr = formato.format(longitude);
        //String latitud = String.valueOf(latitude);
        //String longitud = String.valueOf(longitude);
// Creamos un objeto StringRequest con el método POST para enviar los datos
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Aquí puedes manejar la respuesta del servidor
                        if(!response.isEmpty()){
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(PrncipalActivity.this, "Se envio tu reporte con exito", Toast.LENGTH_SHORT).show();
                            titulo.setVisibility(View.VISIBLE);
                            latView.setVisibility(View.VISIBLE);
                            longView.setVisibility(View.VISIBLE);
                            latView.setText("Latitud: "+latitudStr);
                            longView.setText("Longitud: "+longitudStr);
                            Intent intent = new Intent(PrncipalActivity.this,ConfirmacionActivity.class);
                            startActivity(intent);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Aquí puedes manejar el error de la petición
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(PrncipalActivity.this, "Se produjo un error, verifica tu conexion e intentalo de nuevo"+error.getMessage(), Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Creamos un mapa para almacenar los datos a enviar
                Map<String, String> params = new HashMap<>();
                // Agregamos los datos que queremos enviar al archivo PHP
                String codigo = getIntent().getExtras().get("cod_user").toString();
                String correoUser = getIntent().getExtras().get("correo").toString();
                params.put("cod_visitante", codigo);
                params.put("correo", correoUser);
                params.put("lat", latitudStr);
                params.put("lon", longitudStr);
                params.put("mensaje", "robo");
                params.put("cod_mensaje", "2");
                params.put("tipo", "1");
                params.put("android", "1");
                // Devolvemos el mapa con los datos
                return params;
            }
        };
// Agregamos la petición a la cola de Volley para enviarla
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}