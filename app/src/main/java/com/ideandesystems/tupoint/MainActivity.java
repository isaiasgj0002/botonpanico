package com.ideandesystems.tupoint;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
public class MainActivity extends AppCompatActivity {
    EditText email,password;
    Button guardar;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Instanciar objetos
        email = findViewById(R.id.txtemail);
        password = findViewById(R.id.txtpassword);
        guardar = findViewById(R.id.btnguardar);
        Button btnregistro = findViewById(R.id.btnregistrar);
        btnregistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si no ha concedido permisos, solicitarlos
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
        CheckBox checkBoxShowPassword = findViewById(R.id.checkBoxShowPassword);
        progressBar = findViewById(R.id.progressbarlogin);
        checkBoxShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // mostrar contraseña
                    password.setInputType(InputType.TYPE_CLASS_TEXT);
                } else {
                    // ocultar contraseña
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        //Acciones que se ejecutaran al hacer click al bóton
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Obtener valores de los EditText, convertirlos a String y borrarles los esopacios en blanco extras
                String uEmail = email.getText().toString().trim();
                String uPassword = password.getText().toString().trim();
                //Si ambos campos estan completos, debera ejecutarse la validacion de usuario
                if(!TextUtils.isEmpty(uEmail)&&!TextUtils.isEmpty(uPassword)){
                    validarUsuario("https://tupoint.com/apk/cod_usuario.php");
                    progressBar.setVisibility(View.VISIBLE);
                }else{
                    //Mostrar mensaje al usuario en caso de que haga click al bóton sin completar los campos
                    Toast.makeText(MainActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //Metodo para guardar datos en un archivo TXT
    private void guardarDatos(String uEmail, String uPassword, String cod_user) {
        try{
            //Se crea el archivo y se escribe el correo y contraseña
            File archivotxt = new File(getFilesDir(), "datos.txt");
            FileWriter writer = new FileWriter(archivotxt);
            writer.write(uEmail + "\n");
            writer.write(uPassword + "\n");
            writer.write(cod_user);
            writer.close();
            Toast.makeText(this, "Datos registrados satisfactoriamente", Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            //Si se produce un error al intentar guardar el archivo, se mostrara el mensaje al usuario y detalles de error en la consola
            Toast.makeText(this, "Se produjo un error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    //Metodo para validar si el usuario existe en la base de datos, pasamos como parametro la URL
    private void validarUsuario(String URL){
        StringRequest request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                /*
                Si hubo respuesta del servidor, y no fue FALSE, se guardan los datos en un archivo TXT
                y se abre el activity principal
                 */
                if(!response.isEmpty()){
                    progressBar.setVisibility(View.INVISIBLE);
                    guardarDatos(email.getText().toString().trim(), password.getText().toString().trim(),response);
                    Intent intent = new Intent(MainActivity.this,PrncipalActivity.class);
                    intent.putExtra("correo",email.getText().toString().trim());
                    intent.putExtra("cod_user",response);
                    startActivity(intent);
                    finish();
                }else{
                    //Si las credenciales no existen en la base de datos, se mostrara un mensaje al usuario
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, "Usuario o password no existen", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            //Si se produce un error al intentar ejecutar la validacion, se mostrara un mensaje al usuario
            //y se mostraran los detalles en la consola
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }){
            /*
            Aca pasamos los parametros a validarse, que son el correo y la contraseña introducidas
            en los EditTexts, deben ser convertidos a String y deben borrarse los espacios en blanco extras
             */
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> data = new HashMap<String,String>();
                data.put("user",email.getText().toString().trim());
                data.put("contra",password.getText().toString().trim());
                return data;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }


    @Override
    protected void onStart() {
        super.onStart();
        /*
        Para que el usuario no tenga que introducir sus credenciales cada vez que abre la aplicacion, buscamos
        el archivo de texto que debio crearse al validar sus datos, si existe, se redirigira al usuario directamente
        al activity principal, si no existe, se mostrara un mensaje en el LOG
         */
        try {
            FileInputStream fileInputStream = openFileInput("datos.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String firstLine = bufferedReader.readLine();
            // Leer la segunda y tercera línea
            bufferedReader.readLine(); // ignorar la segunda línea
            String thirdLine = bufferedReader.readLine();
            bufferedReader.close();
            Intent intent = new Intent(MainActivity.this,PrncipalActivity.class);
            intent.putExtra("correo",firstLine);
            intent.putExtra("cod_user",thirdLine);
            startActivity(intent);
        } catch (Exception e) {
            Log.i("Mensaje","El usuario no se logueo, archivo de texto no existe en almacenamiento");
        }
    }
}