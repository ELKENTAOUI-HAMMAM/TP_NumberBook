package com.example.numberbook.api;

import com.example.numberbook.classes.ApiResponse;
import com.example.numberbook.classes.Contact;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/create.php")
    Call<ApiResponse> createContact(@Body Contact contact);

    @GET("api/read.php")
    Call<List<Contact>> getAllContacts();}
