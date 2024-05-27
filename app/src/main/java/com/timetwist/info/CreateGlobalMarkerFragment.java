package com.timetwist.info;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;
import com.timetwist.databinding.FragmentCreateGlobalMarkerBinding;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.interfaces.WikipediaInterface;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;
import com.timetwist.utils.RetrofitClient;
import com.timetwist.utils.ToastUtils;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateGlobalMarkerFragment extends DialogFragment {
    private final LatLng mMarkerLatLng;
    private FragmentCreateGlobalMarkerBinding mBinding;
    private FirestoreServices mFirestoreServices;
    private ActivityUtils mActivityUtils;
    private String mType;

    public CreateGlobalMarkerFragment(LatLng markerLatLng) {
        mMarkerLatLng = markerLatLng;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        mBinding = FragmentCreateGlobalMarkerBinding.inflate(LayoutInflater.from(requireContext()));
        View view = mBinding.getRoot();
        mFirestoreServices = FirestoreServices.getInstance();
        mActivityUtils = ActivityUtils.getInstance();

        mBinding.church.setOnClickListener(v -> {
            mType = "church";
            mBinding.showMarkerName.setText("Church");
        });
        mBinding.prehistoricSite.setOnClickListener(v -> {
            mType = "temple";
            mBinding.showMarkerName.setText("Temple");
        });
        mBinding.tree.setOnClickListener(v -> {
            mType = "nature";
            mBinding.showMarkerName.setText("Nature");
        });

        mBinding.close.setOnClickListener(v -> dismiss());
        mBinding.save.setOnClickListener(v -> configureSaveButtonClick());

        setupAutocomplete();

        builder.setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow())
                .setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    private void setupAutocomplete() {
        mBinding.markerName.setThreshold(1);
        mBinding.markerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 1) {
                    fetchSuggestions(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchSuggestions(String query) {
        WikipediaInterface apiService = RetrofitClient.getClient().create(WikipediaInterface.class);
        Call<List<Object>> call = apiService.getSuggestions(query);

        call.enqueue(new Callback<List<Object>>() {
            @Override
            public void onResponse(@NonNull Call<List<Object>> call, @NonNull Response<List<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Object> responseBody = response.body();
                    if (responseBody.size() > 1 && responseBody.get(1) instanceof List) {
                        List<String> suggestions = (List<String>) responseBody.get(1);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_dropdown_item_1line, suggestions);
                        mBinding.markerName.setAdapter(adapter);
                        mBinding.markerName.showDropDown();
                    } else {
                        ToastUtils.show(requireContext(), "Unexpected response format");
                    }
                } else {
                    ToastUtils.show(requireContext(), "Failed to fetch suggestions");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Object>> call, @NonNull Throwable t) {
                ToastUtils.show(requireContext(), "Failed to fetch suggestions: " + t.getMessage());
            }
        });
    }

    private void configureSaveButtonClick() {
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireContext(), "Internet required");
            return;
        }

        String name = mBinding.markerName.getText().toString().trim();
        String type = mType;
        boolean check = false;

        if (mActivityUtils.MAP_FRAGMENT.checkIfMarkerWithSameNameExists(name)) {
            ToastUtils.show(requireContext(), "You have already marker with this name");
            check = true;
        }
        if (name.isEmpty()) {
            ToastUtils.show(requireContext(), "Marker name is required");
            check = true;
        } else if (mType == null) {
            ToastUtils.show(requireContext(), "You must select a type for the marker");
            check = true;
        }
        if (check) return;

        GeoPoint geoPoint = new GeoPoint(mMarkerLatLng.latitude, mMarkerLatLng.longitude);
        mFirestoreServices.addGlobalMarkerDb(name, type, geoPoint,
                success -> {
                    Toast.makeText(requireContext(), success,
                            Toast.LENGTH_SHORT).show();
                    mActivityUtils.MAP_FRAGMENT.refreshGlobalMarkers(true, name);
                    dismiss();
                },
                error -> Toast.makeText(requireContext(), error,
                        Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}