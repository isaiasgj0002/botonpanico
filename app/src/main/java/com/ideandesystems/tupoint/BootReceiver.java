package com.ideandesystems.tupoint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
public class BootReceiver extends BroadcastReceiver {
    private String rol="Usuario";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Toast.makeText(context, "Se esta ejecutando", Toast.LENGTH_SHORT).show();
            try {
                FileInputStream fileInputStream = context.openFileInput("datos.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                bufferedReader.readLine();
                // Leer la segunda y tercera línea
                bufferedReader.readLine(); // ignorar la segunda línea
                String cod = bufferedReader.readLine();
                String rol = GetRol(cod, context);
                bufferedReader.close();
                // Lanza el servicio al iniciarse el sistema
                Intent serviceIntent = new Intent(context, DenunciaService.class);
                serviceIntent.putExtra("cod_user", cod);
                serviceIntent.putExtra("rol", rol);
                context.startService(serviceIntent);
            } catch (Exception e) {
                Log.i("Mensaje", "El usuario no se logueo, archivo de texto no existe en almacenamiento");
            }
        }
    }

    private String GetRol(String cod, Context context) {
        StringRequest requerimiento = new StringRequest(Request.Method.POST, "https://tupoint.com/apk/rol.php", response -> {
            if(!response.isEmpty()){
                rol = response;
            }else{
                rol = "Usuario";
            }
        }, error -> {
            error.printStackTrace();
            rol = "Usuario";
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String,String> data = new HashMap<>();
                data.put("operacion","validacion");
                data.put("id",cod);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(requerimiento);
        return  rol;
    }
}