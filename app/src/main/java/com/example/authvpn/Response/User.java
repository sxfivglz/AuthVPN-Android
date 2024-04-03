package com.example.authvpn.Response;

public class User {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "User{" +
                "token='" + token + '\'' +
                '}';
    }
}
