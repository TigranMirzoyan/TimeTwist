package com.timetwist.info;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.timetwist.R;

import java.util.Objects;

public class PlaceInfoDialog extends DialogFragment {
    private final String mTitle;
    private final String mDescription;

    public PlaceInfoDialog(String title, String message) {
        mTitle = title;
        mDescription = message;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_place_info_dialog, null);

        ImageButton cancelButton = view.findViewById(R.id.cancelID);
        TextView title = view.findViewById(R.id.placeTitle);
        TextView description = view.findViewById(R.id.placeDescription);
        TextView placeLink = view.findViewById(R.id.placeLink);

        title.setText(mTitle);
        description.setText(mDescription);

        cancelButton.setOnClickListener(v -> dismiss());

        placeLink.setMovementMethod(LinkMovementMethod.getInstance());
        placeLink.setOnClickListener(v -> {
            String url = "https://en.wikipedia.org/wiki/" + mTitle.replaceAll(" ", "_");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

        builder.setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

}
