package com.timetwist.favoritelocations;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.timetwist.R;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_location_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String place = mFavoritePlaces.get(position);
        holder.textView.setText(place);
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) mListener.onMarkerSelected(place);
        });
    }

    @Override
    public int getItemCount() {
        return mFavoritePlaces.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textViewFavoriteLocation);
        }
    }
}