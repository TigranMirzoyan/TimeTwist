package com.timetwist.info;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.timetwist.R;
import com.timetwist.bottombar.MapFragment;

import java.util.Objects;

public class PlaceInfoDialog2 extends DialogFragment {
    private final MapFragment mMapFragment;
    private final String mTitle;
    private final String mDescription;
    private final String mMarkerId;
    private Button mDeleteButton;

    public PlaceInfoDialog2(String mTitle, String mDescription,
                            MapFragment mMapFragment, String mMarkerId) {
        this.mTitle = mTitle;
        this.mDescription = mDescription;
        this.mMapFragment = mMapFragment;
        this.mMarkerId = mMarkerId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_place_info_dialog2, null);

        ImageButton cancelButton = view.findViewById(R.id.cancelID);
        TextView title = view.findViewById(R.id.placeTitle);
        TextView description = view.findViewById(R.id.placeDescription);
        mDeleteButton = view.findViewById(R.id.deleteBtn);

        title.setText(mTitle);
        description.setText(mDescription);
        cancelButton.setOnClickListener(v -> dismiss());

        builder.setView(view);
        AlertDialog dialog = builder.create();
        configureDeleteButton();

        Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    public void configureDeleteButton() {
        mDeleteButton.setOnClickListener(v -> {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = Objects.requireNonNull(FirebaseAuth.getInstance()
                    .getCurrentUser()).getUid();

            db.collection("Users").document(userId).collection("Markers")
                    .document(mMarkerId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Marker deleted successfully!",
                                Toast.LENGTH_SHORT).show();
                        mMapFragment.refreshMapMarkers();
                        dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error deleting marker",
                            Toast.LENGTH_SHORT).show());
        });
    }
}
