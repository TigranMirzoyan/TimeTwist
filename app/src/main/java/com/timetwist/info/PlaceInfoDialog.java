package com.timetwist.info;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.timetwist.R;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.utils.NetworkUtils;

import java.util.Objects;

public class PlaceInfoDialog extends DialogFragment {
    private final OnFavoriteUpdateListener updateListener;
    private final Context mContext;
    private final String mTitle;
    private final String mDescription;
    private FirestoreServices mFirestoreServices;
    private Button mFavoriteLocation;
    private TextView mReadMore;
    private boolean mButtonClicked;

    public interface OnFavoriteUpdateListener {
        void onFavoriteAdded(String title);

        void onFavoriteRemoved(String title);
    }

    public PlaceInfoDialog(String mTitle, String mDescription, Boolean bool, Context mContext,
                           OnFavoriteUpdateListener updateListener) {
        this.mContext = mContext;
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        this.updateListener = updateListener;
        mButtonClicked = bool;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_place_info_dialog, null);
        mFirestoreServices = FirestoreServices.getInstance();

        mReadMore = view.findViewById(R.id.readMore);
        mFavoriteLocation = view.findViewById(R.id.favoriteLocation);
        ImageButton cancelButton = view.findViewById(R.id.cancelID);
        TextView title = view.findViewById(R.id.placeTitle);
        TextView description = view.findViewById(R.id.placeDescription);
        description.setMovementMethod(new ScrollingMovementMethod());

        title.setText(mTitle);
        description.setText(mDescription);
        cancelButton.setOnClickListener(v -> dismiss());
        changeDrawable();
        configureFavoriteButton();

        builder.setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    private void configureFavoriteButton() {
        mFavoriteLocation.setOnClickListener(v -> {
            if (NetworkUtils.isWifiDisconnected(mContext)) {
                Toast.makeText(mContext, "Wifi Required",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            mButtonClicked = !mButtonClicked;
            changeDrawable();

            if (mButtonClicked) {
                mFirestoreServices.makeFavoriteLocation(mTitle,
                        success -> {
                            Toast.makeText(mContext, success,
                                    Toast.LENGTH_SHORT).show();
                            if (updateListener != null) updateListener.onFavoriteAdded(mTitle);
                        },
                        error -> Toast.makeText(mContext, error,
                                Toast.LENGTH_SHORT).show());
                return;
            }

            mFirestoreServices.findFavoriteMarkerAndDelete(mTitle,
                    success -> {
                        if (updateListener == null) return;
                        updateListener.onFavoriteRemoved(mTitle);
                        Toast.makeText(mContext, success,
                                Toast.LENGTH_SHORT).show();
                    },
                    error -> Toast.makeText(mContext, error,
                            Toast.LENGTH_SHORT).show());
        });
    }

    private void changeDrawable() {
        mFavoriteLocation.setBackground(ContextCompat.getDrawable(mContext,
                mButtonClicked ? R.drawable.favorite_button_clicked :
                        R.drawable.favorite_button_not_clicked));
    }
}