package com.example.numberbook.classes;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("id")
    private String id;

    @SerializedName("nom")
    private String nom;

    @SerializedName("numero")
    private String numero;

    // Getters
    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getNumero() {
        return numero;
    }
}
