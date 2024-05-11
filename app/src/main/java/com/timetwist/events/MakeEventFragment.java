package com.timetwist.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.timetwist.MainActivity;
import com.timetwist.R;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.utils.ActivityUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MakeEventFragment extends Fragment {
    private final List<String> mGlobalMarkerNames = new ArrayList<>();
    private final Calendar mCalendar = Calendar.getInstance();
    private ActivityUtils mActivityUtils;
    private AutoCompleteTextView mEventName;
    private TextView mDataTime;
    private EditText mEventDescription, mNumberOfPeople;
    private Button mMakeEvent, mBack;
    private FirestoreServices mFirestoreServices;
    private FirebaseUser mCurrentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_make_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirestoreServices = FirestoreServices.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mActivityUtils = ActivityUtils.getInstance();

        mBack = view.findViewById(R.id.closeFragment);
        mDataTime = view.findViewById(R.id.dataTime);
        mMakeEvent = view.findViewById(R.id.makeEventButton);
        mEventName = view.findViewById(R.id.eventName);
        mEventDescription = view.findViewById(R.id.eventDescription);
        mNumberOfPeople = view.findViewById(R.id.numberOfPeople);
        mDataTime.setOnClickListener(v -> showDatePicker());
        mEventName.setOnClickListener(v -> mEventName.showDropDown());

        loadGlobalMarkerNames();
        configureMakeEventButton();
        configureBackButton();
    }

    private void configureBackButton() {
        mBack.setOnClickListener(v -> {
            if (mCurrentUser == null) {
                return;
            }
            if (!(requireActivity() instanceof MainActivity)) {
                return;
            }
            mActivityUtils.replace(requireActivity().getSupportFragmentManager(),
                    mActivityUtils.HOME_FRAGMENT, requireContext());
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, monthOfYear, dayOfMonth) -> {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, monthOfYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            showTimePicker();
        }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mCalendar.set(Calendar.MINUTE, minute);
            updateLabel();
        }, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE),
                false).show();
    }

    private void updateLabel() {
        SimpleDateFormat format = new SimpleDateFormat
                ("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault());
        mDataTime.setText(format.format(mCalendar.getTime()));
    }

    private void loadGlobalMarkerNames() {
        FirestoreServices.getInstance().getGlobalMarkerNames(
                names -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, names);
                    adapter.sort(String.CASE_INSENSITIVE_ORDER);
                    mEventName.setAdapter(adapter);
                    mGlobalMarkerNames.clear();
                    mGlobalMarkerNames.addAll(names);
                    if (names.isEmpty()) {
                        Toast.makeText(requireContext(), "No names available",
                                Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error loading event names: " + error,
                        Toast.LENGTH_SHORT).show()
        );
    }

    private void configureMakeEventButton() {
        mMakeEvent.setOnClickListener(v -> {
            String name = mEventName.getText().toString().trim();
            String dataTime = mDataTime.getText().toString().trim();
            String description = mEventDescription.getText().toString().trim();
            String peopleStr = mNumberOfPeople.getText().toString().trim();
            int people = peopleStr.isEmpty() ? 0 : Integer.parseInt(peopleStr);

            if (!mGlobalMarkerNames.contains(name) && !TextUtils.isEmpty(name) &&
                    !TextUtils.isEmpty(dataTime) && description.length() <= 50
                    && people >= 0 && people <= 50) {
                sendEventToFirebase(name, description, people);
                return;
            }

            if (!mGlobalMarkerNames.contains(name))
                mEventName.setError("Name must match one of the global marker names");

            if (TextUtils.isEmpty(mDataTime.getText().toString()))
                Toast.makeText(getContext(), "Please add the time",
                        Toast.LENGTH_SHORT).show();

            if (description.length() >= 50)
                mEventDescription.setError("Max size of letters should be 50");

            if (description.isEmpty()) mEventDescription.setError("Description cannot be empty");

            if (people > 50)
                mNumberOfPeople.setError("Impossible to get " + people + "companions with you");

        });
    }

    private void sendEventToFirebase(String name, String description, int people) {
        mFirestoreServices.makeEvent(name, mActivityUtils.PROFILE_FRAGMENT
                        .getUsername(), description, mCalendar, people,
                success -> Toast.makeText(requireContext(), success,
                        Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(getContext(), error,
                        Toast.LENGTH_SHORT).show());

        mEventName.getText().clear();
        mEventDescription.getText().clear();
        mDataTime.setText("");
        mNumberOfPeople.setText("");
    }
}