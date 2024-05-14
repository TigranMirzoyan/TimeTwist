package com.timetwist.favorite.locations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.timetwist.R;
import com.timetwist.custom.interfaces.OnMarkerSelectedListener;
import com.timetwist.firebase.FirestoreServices;

import java.util.ArrayList;
import java.util.List;

public class FavoriteLocationsFragment extends Fragment {
    private final List<String> mFavoriteMarkers = new ArrayList<>();
    private final List<String> mDisplayedMarkers = new ArrayList<>();
    private FirestoreServices mFirestoreServices;
    private FavoriteLocationsAdapter mAdapter;
    private OnMarkerSelectedListener mListener;
    private SearchView mSearchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite_locations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirestoreServices = FirestoreServices.getInstance();
        mSearchView = view.findViewById(R.id.searchView);
        RecyclerView mRecyclerView = view.findViewById(R.id.recyclerViewFavoriteLocations);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = new FavoriteLocationsAdapter(mDisplayedMarkers, mListener);
        mRecyclerView.setAdapter(mAdapter);
        getFavoritePlaces();
        setupSearchView();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnMarkerSelectedListener) {
            mListener = (OnMarkerSelectedListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnMarkerSelectedListener");
        }
    }

    private void getFavoritePlaces() {
        mFirestoreServices.getFavoritePlaces(
                favoritePlaces -> {
                    mFavoriteMarkers.clear();
                    mFavoriteMarkers.addAll(favoritePlaces);
                    filter("");
                },
                error -> Toast.makeText(requireContext(), "Error: " + error,
                        Toast.LENGTH_SHORT).show());
    }

    private void setupSearchView() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filter(String text) {
        List<String> filteredList = new ArrayList<>();
        for (String item : mFavoriteMarkers) {
            if (!item.toLowerCase().contains(text.toLowerCase())) continue;
            filteredList.add(item);
        }
        mDisplayedMarkers.clear();
        mDisplayedMarkers.addAll(filteredList);
        mAdapter.notifyDataSetChanged();
    }


    public void addFavoritePlace(String title) {
        if (mFavoriteMarkers.contains(title)) return;
        mFavoriteMarkers.add(title);
        mFavoriteMarkers.sort(String.CASE_INSENSITIVE_ORDER);
        filter(mSearchView.getQuery().toString());
    }

    public void removeFavoritePlace(String title) {
        if (mFavoriteMarkers.remove(title)) filter(mSearchView.getQuery().toString());
    }

    public List<String> getFavoriteLocationsList() {
        return mFavoriteMarkers;
    }
}