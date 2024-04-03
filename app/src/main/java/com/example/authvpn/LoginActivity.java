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

import com.example.authvpn.SingletonRequest.SingletonRequest;

import java.io.UnsupportedEncodingException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    String url = "http://10.0.2.2:8000/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnlogin);
        btnLogin.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        try {
            EditText txtEmail = findViewById(R.id.email);
            EditText txtPassword = findViewById(R.id.password);
            String email = txtEmail.getText().toString();
            String password = txtPassword.getText().toString();
            if (email.isEmpty() && password.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage("Email and password are required.")
                        .setTitle("Error")
                        .setPositiveButton("Ok", null);
                builder.create().show();
                return;
            } else if (email.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage("Email is required.")
                        .setTitle("Error")
                        .setPositiveButton("Ok", null);
                builder.create().show();
            } else if (password.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage("Password is required.")
                        .setTitle("Error")
                        .setPositiveButton("Ok", null);
                builder.create().show();
            } else {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);
                jsonBody.put("password", password);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url + "login", jsonBody, new Response.Listener<JSONObject>() {
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
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                            if (response.toString().contains("error")) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setMessage(response.toString())
                                        .setTitle("Error")
                                        .setPositiveButton("Ok", null);
                                builder.create().show();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                      if(error.networkResponse.statusCode == 400) {
                          try {
                              String responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                String message = data.getString("error");
                              AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                              builder.setMessage(message)
                                      .setTitle("Error")
                                      .setPositiveButton("Ok", null);
                              builder.create().show();
                          }catch(JSONException e){
                              Log.e("Error", e.getMessage());
                          } catch (UnsupportedEncodingException e) {
                              throw new RuntimeException(e);
                          }
                      }else{
                          AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                          builder.setMessage("Error: Has not been possible to connect to the server. Please try again later.")
                                  .setTitle("Error")
                                  .setPositiveButton("Ok", null);
                          builder.create().show();
                      }
                    }
                });

                SingletonRequest.getInstance(this).addToRequestQueue(jsonObjectRequest);
            }

            }catch(Exception e){
                Log.e("Error", e.getMessage());
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage("Error: Has not been possible to connect to the server. Please try again later.")
                        .setTitle("Error")
                        .setPositiveButton("Ok", null);
                builder.create().show();
            }
        }



}