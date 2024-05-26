package com.timetwist.favoritelocations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.timetwist.databinding.FragmentFavoriteLocationsBinding;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.interfaces.OnMarkerSelectedListener;
import com.timetwist.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class FavoriteLocationsFragment extends Fragment {
    private final List<String> mFavoriteMarkers = new ArrayList<>();
    private final List<String> mDisplayedMarkers = new ArrayList<>();
    private FragmentFavoriteLocationsBinding mBinding;
    private FirestoreServices mFirestoreServices;
    private FavoriteLocationsAdapter mAdapter;
    private OnMarkerSelectedListener mListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentFavoriteLocationsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirestoreServices = FirestoreServices.getInstance();

        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mAdapter = new FavoriteLocationsAdapter(mDisplayedMarkers, mListener);
        mBinding.recyclerView.setAdapter(mAdapter);

        getFavoritePlaces();
        setupSearchView();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnMarkerSelectedListener) {
            mListener = (OnMarkerSelectedListener) context;
            return;
        }
        throw new RuntimeException(context + " must implement OnMarkerSelectedListener");
    }

    private void getFavoritePlaces() {
        mFirestoreServices.getFavoritePlaces(
                favoritePlaces -> {
                    mFavoriteMarkers.clear();
                    mFavoriteMarkers.addAll(favoritePlaces);
                    filter("");
                },
                error -> ToastUtils.show(requireContext(), error));
    }

    private void setupSearchView() {
        mBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
            if (item.toLowerCase().contains(text.toLowerCase())) filteredList.add(item);
        }
        mDisplayedMarkers.clear();
        mDisplayedMarkers.addAll(filteredList);
        mAdapter.notifyDataSetChanged();
    }

    public void addFavoritePlace(String title) {
        if (mFavoriteMarkers.contains(title)) {
            ToastUtils.show(requireContext(), "Something went wrong");
            return;
        }
        mFavoriteMarkers.add(title);
        mFavoriteMarkers.sort(String.CASE_INSENSITIVE_ORDER);
        filter(mBinding.searchView.getQuery().toString());
    }

    public void removeFavoritePlace(String title) {
        if (mFavoriteMarkers.remove(title)) filter(mBinding.searchView.getQuery().toString());
    }

    public List<String> getFavoriteLocationsList() {
        return mFavoriteMarkers;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}