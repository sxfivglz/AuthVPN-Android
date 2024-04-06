package com.example.authvpn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.authvpn.SingletonRequest.SingletonRequest;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    String url = "http://192.168.1.2/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnlogin);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        EditText txtEmail = findViewById(R.id.email);
        EditText txtPassword = findViewById(R.id.password);
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

        // Validación de correo electrónico
        if (!isValidEmail(email)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Please enter a valid email.")
                    .setTitle("Error")
                    .setPositiveButton("Ok", null);
            builder.create().show();
            return;
        }

        // Validación de campos vacíos
        if (email.isEmpty() || password.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Email and password are required.")
                    .setTitle("Error")
                    .setPositiveButton("Ok", null);
            builder.create().show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url + "login", jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Gson gson = new Gson();
                            //Class from directory app/src/main/java/com/example/authvpn/Response/User.java
                            com.example.authvpn.Response.User user = gson.fromJson(response.toString(), com.example.authvpn.Response.User.class);
                            SharedPreferences sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("token", user.getToken());
                            editor.apply();
                            Intent intent = new Intent(LoginActivity.this, RequestCodeActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                            showErrorDialog("Error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                String errorMessage = data.getString("error");
                                if (errorMessage.equalsIgnoreCase("Access denied")) {
                                    showErrorDialog("Access Denied: You do not have permission to access the VPN.");
                                } else {
                                    showErrorDialog(errorMessage);
                                }
                            } catch (Exception e) {
                                Log.e("Error", e.getMessage());
                                showErrorDialog("Error: " + e.getMessage());
                            }
                        } else {
                            showErrorDialog("Error: Has not been possible to connect to the server. Please try again later.");
                        }
                    }
                });



        SingletonRequest.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    // Método para validar el formato de correo electrónico
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();


    }

    // Método para mostrar un diálogo de error
    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(message)
                .setTitle("Error")
                .setPositiveButton("Ok", null);
        builder.create().show();
    }
}