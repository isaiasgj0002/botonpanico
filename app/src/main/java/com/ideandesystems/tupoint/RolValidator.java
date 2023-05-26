package com.ideandesystems.tupoint;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

public class RolValidator {
    static String rol ="";
    public static String getRol(Context context, String id, String url){
        StringRequest req = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    rol = response;
                    Log.i("rol", response);
                }else{
                    String[] opciones = {"Policia", "Vigilante","Usuario"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setCancelable(false);
                    builder.setTitle("¿Que erés?");
                    builder.setItems(opciones, (dialog, item) -> {
                        // Acción a realizar cuando se selecciona una opción
                        if (item == 0) {
                            updateRol(id, "https://tupoint.com/apk/rol.php","Policia",context);
                            //saveRol("Policia", context);
                        } else if (item == 1) {
                            updateRol(id,"https://tupoint.com/apk/rol.php","Vigilante",context);
                            //saveRol("Vigilante", context);
                        }else if(item==2){
                            updateRol(id,"https://tupoint.com/apk/rol.php","Usuario", context);
                            //saveRol("Usuario",context);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

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
                Map<String,String> data = new HashMap<String,String>();
                data.put("operacion","validacion");
                data.put("id",id);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(req);
        return rol;
    }
    public static void updateRol(String id, String url, String rol, Context context){
        StringRequest saverol = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    Toast.makeText(context, "Se registro tu rol", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(context, "Hubo algun error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String,String> data = new HashMap<String,String>();
                data.put("operacion","registro");
                data.put("id",id);
                data.put("rol",rol);
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(saverol);
    }
}
