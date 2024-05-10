package com.timetwist.bottombar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.timetwist.R;
import com.timetwist.firebase.FirestoreServices;

import java.util.ArrayList;
import java.util.List;

public class FavoriteLocationsFragment extends Fragment {
    private final List<String> mFavoriteMarkers = new ArrayList<>();
    private FirestoreServices mFirestoreServices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite_locations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirestoreServices = FirestoreServices.getInstance();
        getFavoritePlaces();
    }

    private void getFavoritePlaces() {
        mFirestoreServices.getFavoritePlaces(
                favoritePlaces -> {
                    mFavoriteMarkers.clear();
                    mFavoriteMarkers.addAll(favoritePlaces);
                },
                error -> Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show());
    }

    public List<String> getList() {
        return mFavoriteMarkers;
    }

    public void addFavoritePlace(String title) {
        if (!mFavoriteMarkers.contains(title)) mFavoriteMarkers.add(title);
    }

    public void removeFavoritePlace(String title) {
        mFavoriteMarkers.remove(title);
    }
}