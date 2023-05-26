package com.ideandesystems.tupoint;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
public class AlarmaActivity extends AppCompatActivity {
    Button btnhaceralgo, btnnohacernada;
    MediaPlayer mp;
    Handler handler;
    String iddenuncia,idusuario,id_denunciante;
    CircleImageView imgperfil;
    TextView txtname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarma);
        mp = MediaPlayer.create(this,R.raw.alerta);
        mp.start();
        btnhaceralgo=findViewById(R.id.btnhacer);
        btnnohacernada=findViewById(R.id.btnnohacer);
        txtname = findViewById(R.id.txtnombre);
        imgperfil = findViewById(R.id.imgPerfil);
        idusuario = getIntent().getStringExtra("cod_usuario");
        iddenuncia = getIntent().getStringExtra("cod_denuncia");
        id_denunciante = getIntent().getStringExtra("cod_user_denuncia");
        getData(id_denunciante);
        btnhaceralgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlarmaActivity.this,DenunciaActivity.class);
                intent.putExtra("cod_user",idusuario);
                intent.putExtra("cod_denuncia",iddenuncia);
                startActivity(intent);
            }
        });
        btnnohacernada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(AlarmaActivity.this)
                        .setTitle("Aviso")
                        .setMessage("¿Está seguro de no deseas hacer nada?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Cerrar todas las actividades y salir de la aplicación
                                finishAffinity();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(AlarmaActivity.this, DenunciaActivity.class);
                intent.putExtra("cod_user",idusuario);
                intent.putExtra("cod_denuncia",iddenuncia);
                startActivity(intent);
            }
        }, 5 * 60 * 1000);

    }

    private void getData(String id_denunciante) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://tupoint.com/apk/getImage.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String imagen = jsonObject.getString("imagen");
                            try{
                                Picasso.get().load(imagen).error(R.drawable.baseline_person_24).into(imgperfil);
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                            String nombre = jsonObject.getString("nombre");
                            txtname.setText("Nombre del denunciante: "+nombre);
                            // Utiliza los datos obtenidos aquí
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<>();
                params.put("cod_user", id_denunciante); // Reemplaza con el valor correcto
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        // Reiniciar el tiempo si se detecta una interacción del usuario
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(AlarmaActivity.this, DenunciaActivity.class);
                intent.putExtra("cod_user",idusuario);
                intent.putExtra("cod_denuncia",iddenuncia);
                startActivity(intent);
            }
        }, 5 * 60 * 1000);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.stop();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mp.stop();
    }
}