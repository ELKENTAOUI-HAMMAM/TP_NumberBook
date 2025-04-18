package com.example.numberbook.classes;

import com.google.gson.annotations.SerializedName;

public class Contact {
    @SerializedName("id")
    private String id; // or int, depending on your existing implementation

    @SerializedName("nom")
    private String nom;

    @SerializedName("numero")
    private String numero;

    private String photoUri;

    public Contact(String nom, String numero) {
        this.nom = nom;
        this.numero = numero;
        this.photoUri = photoUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public String getPhoneNumber() {
        return numero;
    }
}
