package com.example.authvpn.Models;

public class User {
    /*    ['name', 'email', 'password', 'role_id', 'phone', 'verification_code', 'admin_code','is_verified'];
     */
    private String name;
    private String email;
    private String password;
    private int role_id;
    private String phone;
    private String verification_code;
    private String admin_code;
    private boolean is_verified;

    public User(String name, String email, String password, int role_id, String phone, String verification_code, String admin_code, boolean is_verified) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role_id = role_id;
        this.phone = phone;
        this.verification_code = verification_code;
        this.admin_code = admin_code;
        this.is_verified = is_verified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRole_id() {
        return role_id;
    }

    public void setRole_id(int role_id) {
        this.role_id = role_id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVerification_code() {
        return verification_code;
    }

    public void setVerification_code(String verification_code) {
        this.verification_code = verification_code;
    }

    public String getAdmin_code() {
        return admin_code;
    }

    public void setAdmin_code(String admin_code) {
        this.admin_code = admin_code;
    }

    public boolean isIs_verified() {
        return is_verified;
    }

    public void setIs_verified(boolean is_verified) {
        this.is_verified = is_verified;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role_id=" + role_id +
                ", phone='" + phone + '\'' +
                ", verification_code='" + verification_code + '\'' +
                ", admin_code='" + admin_code + '\'' +
                ", is_verified=" + is_verified +
                '}';
    }
}
