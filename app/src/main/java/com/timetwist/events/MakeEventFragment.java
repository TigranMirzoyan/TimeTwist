package com.timetwist.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.timetwist.databinding.FragmentMakeEventBinding;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;
import com.timetwist.utils.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MakeEventFragment extends Fragment {
    private final List<String> mGlobalMarkerNames = new ArrayList<>();
    private final Calendar mCalendar = Calendar.getInstance();
    private FragmentMakeEventBinding mBinding;
    private ActivityUtils mActivityUtils;
    private FirestoreServices mFirestoreServices;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentMakeEventBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirestoreServices = FirestoreServices.getInstance();
        mActivityUtils = ActivityUtils.getInstance();

        mBinding.time.setOnClickListener(v -> showDatePicker());
        mBinding.name.setOnClickListener(v -> mBinding.name.showDropDown());
        mBinding.back.setOnClickListener(v ->
                mActivityUtils.replace(mActivityUtils.HOME_FRAGMENT, requireContext()));
        mBinding.makeEvent.setOnClickListener(v -> configureMakeEventButton());

        loadGlobalMarkerNames();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    mCalendar.set(Calendar.YEAR, year);
                    mCalendar.set(Calendar.MONTH, monthOfYear);
                    mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePicker();
                }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mCalendar.set(Calendar.MINUTE, minute);
            updateLabel();
        }, mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE),
                false).show();
    }

    private void updateLabel() {
        SimpleDateFormat format = new SimpleDateFormat
                ("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault());
        mBinding.time.setText(format.format(mCalendar.getTime()));
    }

    private void loadGlobalMarkerNames() {
        mFirestoreServices.getGlobalMarkerNames(names -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, names);
            adapter.sort(String.CASE_INSENSITIVE_ORDER);
            mBinding.name.setAdapter(adapter);
            mGlobalMarkerNames.clear();
            mGlobalMarkerNames.addAll(names);
            if (names.isEmpty()) ToastUtils.show(requireContext(), "No names available");
        }, e -> ToastUtils.show(requireContext(), e));
    }

    private void configureMakeEventButton() {
        String eventName = mBinding.name.getText().toString().trim();
        String dataTime = mBinding.time.getText().toString().trim();
        String description = mBinding.description.getText().toString().trim();
        String contacts = mBinding.email.getText().toString().trim();
        String peopleStr = mBinding.companions.getText().toString().trim();
        int people = peopleStr.isEmpty() ? 0 : Integer.parseInt(peopleStr);

        if (!isInputValid(eventName, dataTime, description, contacts, people)) return;

        mActivityUtils.initialiseFragments(requireContext());
        sendEventToFirebase(eventName, description, contacts, people);
    }

    private boolean isInputValid(String eventName, String dataTime,
                                 String description, String contacts, int people) {
        boolean check = true;

        if (!mGlobalMarkerNames.contains(eventName)) {
            mBinding.name.setError("Name must match one of the global marker names");
            check = false;
        }

        if (TextUtils.isEmpty(dataTime)) {
            mBinding.time.setError("Please add the time");
            check = false;
        }

        if (TextUtils.isEmpty(description)) {
            mBinding.description.setError("Description cannot be empty");
            check = false;
        } else if (description.length() >= 50) {
            mBinding.description.setError("Max size of letters should be 50");
            check = false;
        }

        if (people > 50) {
            mBinding.companions.setError("Impossible to get " + people + " companions with you");
            check = false;
        } else if (TextUtils.isEmpty(contacts)) {
            mBinding.email.setError("Impossible to contact you");
            check = false;
        }

        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireContext(), "Internet required");
            check = false;
        }

        return check;
    }

    private void sendEventToFirebase(String eventName, String description,
                                     String contacts, int people) {
        mFirestoreServices.makeEvent(eventName, mActivityUtils.PROFILE_FRAGMENT.getUsername(),
                description, mCalendar, contacts, people,
                success -> ToastUtils.show(requireContext(), success),
                e -> ToastUtils.show(requireContext(), e));

        mBinding.email.setText("");
        mBinding.time.setText("");
        mBinding.companions.setText("");
        mBinding.description.setText("");
        mBinding.name.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}