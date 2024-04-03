package com.example.authvpn;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import com.google.gson.Gson;

import org.json.JSONObject;

import com.example.authvpn.SingletonRequest.SingletonRequest;

import java.util.HashMap;
import java.util.Map;

public class RequestCodeActivity extends AppCompatActivity implements View.OnClickListener {
    String url = "http://10.0.2.2:8000/api/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_code);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button btnCode = findViewById(R.id.submit);
        btnCode.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        try {
            EditText txtCode = findViewById(R.id.admin_code);
            String admin_code = txtCode.getText().toString();
            if(admin_code.isEmpty()){
                AlertDialog.Builder builder = new AlertDialog.Builder(RequestCodeActivity.this);
                builder.setMessage("Code is required.")
                        .setTitle("Error")
                        .setPositiveButton("Ok", null);
                builder.create().show();
                return;
            }else{
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("admin_code", admin_code);
                Log.e("json", jsonBody.toString());
                // Token should be saved in SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("token", "");
                Log.e("token", token);
                SharedPreferences preferences = getSharedPreferences("admin_code", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("code", admin_code);
                editor.apply();
                //Json request with bearer token
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url + "verify-code", jsonBody, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Gson gson = new Gson();
                            String json = response.toString();
                            Code authResponse = gson.fromJson(json, Code.class);
                            SharedPreferences sharedPref = getSharedPreferences("codegenerated", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("code", authResponse.getCode());
                            editor.apply();
                            Log.e("code", authResponse.getCode());
                            Log.e("SharedPref", sharedPref.getString("code", ""));
                            Intent intent = new Intent(RequestCodeActivity.this, CodeActivity.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.getMessage());
                        AlertDialog.Builder builder = new AlertDialog.Builder(RequestCodeActivity.this);
                        builder.setMessage("Invalid code.")
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
            }

        }catch (Exception e){
            Log.e("Error", e.getMessage());
        }
    }
}