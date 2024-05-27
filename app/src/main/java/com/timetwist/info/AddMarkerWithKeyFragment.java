package com.timetwist.info;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;
import com.timetwist.databinding.FragmentAddMarkerWithKeyBinding;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.ui.manager.MarkerData;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.Base64Utils;
import com.timetwist.utils.ToastUtils;

public class AddMarkerWithKeyFragment extends Fragment {
    private FragmentAddMarkerWithKeyBinding mBinding;
    private FirestoreServices mFirestoreServices;
    private ActivityUtils mActivityUtils;
    private FirebaseUser mCurrentUser;
    private String mType;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentAddMarkerWithKeyBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirestoreServices = FirestoreServices.getInstance();
        mActivityUtils = ActivityUtils.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        setTypeSelectionListeners();
        mBinding.save.setOnClickListener(v -> handleSaveButtonClick());
        mBinding.back.setOnClickListener(v ->
                mActivityUtils.replace(mActivityUtils.HOME_FRAGMENT, requireContext()));
    }

    private void setTypeSelectionListeners() {
        mBinding.church.setOnClickListener(v -> setType("church", "Church"));
        mBinding.prehistoricSite.setOnClickListener(v -> setType("temple", "Temple"));
        mBinding.tree.setOnClickListener(v -> setType("nature", "Nature"));
        mBinding.defaultMarker.setOnClickListener(v -> setType("default_marker", "Default"));
    }

    private void setType(String type, String displayName) {
        mType = type;
        mBinding.showMarkerName.setText(displayName);
    }

    private void handleSaveButtonClick() {
        String key = mBinding.key.getText().toString().trim();
        if (key.isEmpty()) {
            ToastUtils.show(requireContext(), "Please enter a key");
            return;
        }

        String name = mBinding.name.getText().toString().trim();
        if (name.isEmpty()) {
            ToastUtils.show(requireContext(), "Please enter a name");
            return;
        }

        if (mActivityUtils.MAP_FRAGMENT.checkIfMarkerWithSameNameExists(name)) {
            ToastUtils.show(requireContext(), "A marker with this name already exists");
            return;
        }

        if (mType == null) {
            ToastUtils.show(requireContext(), "Please select a type");
            return;
        }

        addMarkerToFirestore(name, key);
    }

    private void addMarkerToFirestore(String name, String key) {
        try {
            MarkerData markerData = Base64Utils.decode(key, MarkerData.class);
            GeoPoint geoPoint = new GeoPoint(markerData.getLat(), markerData.getLng());
            if (mActivityUtils.MAP_FRAGMENT.checkIfMarkerWithSameLocationExists(geoPoint)) {
                ToastUtils.show(requireContext(), "Marker with this location already exist");
                return;
            }
            String description = mBinding.description.getText().toString().trim();

            mFirestoreServices.addMarkerDb(mCurrentUser.getUid(), name,
                    description, mType, geoPoint,
                    success -> {
                        ToastUtils.show(requireContext(), success);
                        ActivityUtils.getInstance().MAP_FRAGMENT
                                .refreshCustomMarkers(true, name);
                        clearInputFields();
                    },
                    error -> ToastUtils.show(requireContext(), error));
        } catch (Exception e) {
            ToastUtils.show(requireContext(), "Invalid key");
        }
    }

    private void clearInputFields() {
        mBinding.name.setText("");
        mBinding.description.setText("");
        mBinding.key.setText("");
        mBinding.showMarkerName.setText("");
        mType = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
