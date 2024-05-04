package com.timetwist.info;

import android.app.Dialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.timetwist.R;

import java.util.Objects;

public class PlaceInfoDialog extends DialogFragment {
    private final String mTitle;
    private final String mDescription;
    private final String mMarkerType;
    private TextView mReadMore;

    public PlaceInfoDialog(String mTitle, String mDescription, String mMarkerType) {
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        this.mMarkerType = mMarkerType;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_place_info_dialog, null);

        mReadMore = view.findViewById(R.id.readMore);
        ImageButton cancelButton = view.findViewById(R.id.cancelID);
        TextView title = view.findViewById(R.id.placeTitle);
        TextView description = view.findViewById(R.id.placeDescription);
        description.setMovementMethod(new ScrollingMovementMethod());

        title.setText(mTitle);
        description.setText(mDescription);
        configureReadMoreButton();
        cancelButton.setOnClickListener(v -> dismiss());

        builder.setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    private void configureReadMoreButton() {
        mReadMore.setOnClickListener(v -> {
            switch (mMarkerType) {
                case "church":

                    break;
                case "temple":

                    break;
                case "tree":

                    break;
                default:
                    Toast.makeText(requireActivity(), "Something went Wrong", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
