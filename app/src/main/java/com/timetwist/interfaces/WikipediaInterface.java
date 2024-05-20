package com.timetwist.interfaces;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
public interface WikipediaInterface {
    @GET("w/api.php?action=opensearch&format=json&limit=10")
    Call<List<Object>> getSuggestions(@Query("search") String query);
}