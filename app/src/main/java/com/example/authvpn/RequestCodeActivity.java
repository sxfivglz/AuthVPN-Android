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
import com.example.authvpn.Response.Code;
import com.example.authvpn.SingletonRequest.SingletonRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RequestCodeActivity extends AppCompatActivity implements View.OnClickListener {
    String url = "http://192.168.1.2/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_code);

        Button btnCode = findViewById(R.id.submit);
        btnCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            EditText txtCode = findViewById(R.id.admin_code);
            String admin_code = txtCode.getText().toString();

            // Validación de longitud y caracteres
            if (admin_code.length() != 4 || !admin_code.matches("[0-9]+") || admin_code.isEmpty()) {
            } else {
                // Resto del código para enviar la solicitud al servidor
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("admin_code", admin_code);

                // Token should be saved in SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("token", "");

                SharedPreferences preferences = getSharedPreferences("admin_code", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("code", admin_code);
                editor.apply();

                //Json request with bearer token
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url + "verify-code", jsonBody, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response == null) {
                                showErrorDialog("Invalid response from server.");
                                return;
                            }

                            // Check if the response contains "error" field with value "Invalid code"
                            if (response.has("error") && response.getString("error").equals("Invalid code")) {
                                showErrorDialog("Invalid code.");
                                return;
                            }

                            // Rest of your response handling logic
                            Gson gson = new Gson();
                            String json = response.toString();
                            Code authResponse = gson.fromJson(json, Code.class);
                            SharedPreferences sharedPref = getSharedPreferences("codegenerated", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("code", authResponse.getCode());
                            editor.apply();

                            Intent intent = new Intent(RequestCodeActivity.this, CodeActivity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                            showErrorDialog("Error: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Check if the error is due to a 400 Bad Request
                        if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                            try {
                                // Parse the error body
                                String errorBody = new String(error.networkResponse.data, "UTF-8");
                                JSONObject jsonObject = new JSONObject(errorBody);

                                // Check if the error message is "Invalid code"
                                if (jsonObject.has("error") && jsonObject.getString("error").equals("Invalid code")) {
                                    showErrorDialog("Invalid code.");
                                    return; // Return here to avoid further processing
                                }
                            } catch (Exception e) {
                                // Handle parsing exception
                                Log.e("Error", "Error parsing error response: " + e.getMessage());
                            }
                        }

                        // Handle other types of errors
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                // Parse the error body
                                String errorBody = new String(error.networkResponse.data, "UTF-8");
                                showErrorDialog("Server error: " + errorBody);
                            } catch (Exception e) {
                                // Handle parsing exception
                                Log.e("Error", "Error parsing error response: " + e.getMessage());
                            }
                        } else {

                            handleServerError();
                        }
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + token);
                        return headers;
                    }
                };
                SingletonRequest.getInstance(this).addToRequestQueue(jsonObjectRequest);
            }

        } catch (Exception e) {
            Log.e("Error", e.getMessage());

        }
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RequestCodeActivity.this);
        builder.setMessage(message)
                .setTitle("Error")
                .setPositiveButton("Ok", null);
        builder.create().show();
    }

    private void handleServerError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RequestCodeActivity.this);
        builder.setMessage("Server error. Please login and try again.")
                .setTitle("Error")
                .setPositiveButton("Ok", (dialog, which) -> {
                    // Limpiar SharedPreferences
                    SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("token");
                    editor.apply();

                    // Redirigir a la actividad de inicio de sesión
                    Intent intent = new Intent(RequestCodeActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });

        // Crear el AlertDialog después de establecer el onDismissListener
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
