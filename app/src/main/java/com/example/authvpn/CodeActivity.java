package com.example.authvpn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import org.json.JSONObject;

import com.example.authvpn.Response.Code;
import com.example.authvpn.SingletonRequest.SingletonRequest;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class CodeActivity extends AppCompatActivity implements View.OnClickListener {
    String url = "http://10.0.2.2:8000/api/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generated_code);
        TextView txtCode = findViewById(R.id.code_generated);
        SharedPreferences preferences = getSharedPreferences("codegenerated", MODE_PRIVATE);
        String code = preferences.getString("code", "");
        txtCode.setText(code);
        Button btnRequestCode = findViewById(R.id.TryAgain);
        btnRequestCode.setOnClickListener(this);
        Button btnLogout = findViewById(R.id.exit);
        btnLogout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.TryAgain) {

            try {
                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                String token = sharedPreferences.getString("token", "");
                JSONObject jsonBody = new JSONObject();
                SharedPreferences preferences = getSharedPreferences("admin_code", MODE_PRIVATE);
                String admin_code = preferences.getString("code", "");
                try {
                    jsonBody.put("admin_code", admin_code);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("Code", admin_code);
                Log.e("Token", token);
                Log.e("Json", jsonBody.toString());
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url + "regenerate-code", jsonBody, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Gson gson = new Gson();
                            String json = response.toString();
                            Code authResponse = gson.fromJson(json, Code.class);
                            Log.e("Code", authResponse.getCode());
                            if (authResponse.getCode() != null) {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("codegenerated", authResponse.getCode());
                                editor.apply();
                                Log.e("Code", authResponse.getCode());
                                TextView txtCode = findViewById(R.id.code_generated);
                                txtCode.setText(authResponse.getCode());

                                Log.e("Token", token);

                            } else {
                                //Error
                                Log.e("Error", "Error generating code.");
                                AlertDialog.Builder builder = new AlertDialog.Builder(CodeActivity.this);
                                builder.setMessage("Error generating code.")
                                        .setTitle("Error")
                                        .setPositiveButton("Ok", null);
                                builder.create().show();

                            }
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                            AlertDialog.Builder builder = new AlertDialog.Builder(CodeActivity.this);
                            builder.setMessage("Error generating code.")
                                    .setTitle("Error")
                                    .setPositiveButton("Ok", null);
                            builder.create().show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.getMessage());
                        AlertDialog.Builder builder = new AlertDialog.Builder(CodeActivity.this);
                        builder.setMessage("Error generating code.")
                                .setTitle("Error")
                                .setPositiveButton("Ok", null);
                        builder.create().show();
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
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                AlertDialog.Builder builder = new AlertDialog.Builder(CodeActivity.this);
                builder.setMessage("Error generating code.")
                        .setTitle("Error")
                        .setPositiveButton("Ok", null);
                builder.create().show();
            }

        } else if (v.getId() == R.id.exit) {
            try {
                //Hacer la llamada a la API para cerrar sesión
                SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                String token = sharedPreferences.getString("token", "");
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url + "logout", null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            SharedPreferences preferences = getSharedPreferences("token", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.remove("token");
                            editor.apply();
                            finish();
                            Intent intent = new Intent(CodeActivity.this, LoginActivity.class);
                            startActivity(intent);

                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.getMessage());
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + token);
                        return headers;
                    }
                };
                // Add the request to the RequestQueue.
                SingletonRequest.getInstance(this).addToRequestQueue(jsonObjectRequest);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                AlertDialog.Builder builder = new AlertDialog.Builder(CodeActivity.this);
                builder.setMessage("Error logging out.")
                        .setTitle("Error")
                        .setPositiveButton("Ok", null);
                builder.create().show();
            }

        }

    }
}
