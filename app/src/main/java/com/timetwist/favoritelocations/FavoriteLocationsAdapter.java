package com.timetwist.favoritelocations;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.timetwist.databinding.FavoriteLocationItemBinding;
import com.timetwist.interfaces.OnMarkerSelectedListener;

import java.util.List;

public class FavoriteLocationsAdapter extends RecyclerView.Adapter<FavoriteLocationsAdapter.ViewHolder> {
    private final List<String> mFavoritePlaces;
    private final OnMarkerSelectedListener mListener;

    public FavoriteLocationsAdapter(List<String> favoritePlaces, OnMarkerSelectedListener listener) {
        mFavoritePlaces = favoritePlaces;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        FavoriteLocationItemBinding binding = FavoriteLocationItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String place = mFavoritePlaces.get(position);
        holder.bind(place, mListener);
    }

    @Override
    public int getItemCount() {
        return mFavoritePlaces.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final FavoriteLocationItemBinding binding;

        public ViewHolder(FavoriteLocationItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String place, OnMarkerSelectedListener listener) {
            binding.name.setText(place);
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onMarkerSelected(place);
            });
        }
    }
}