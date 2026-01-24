package com.example.sportfieldreservation.network;

import com.example.sportfieldreservation.model.Field;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface FieldApiService {

    @GET("fields.json")
    Call<List<Field>> getFields();
}
