package com.timetwist.events;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.timetwist.MainActivity;
import com.timetwist.R;
import com.timetwist.bottombar.HomeFragment;
import com.timetwist.bottombar.ProfileFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MakeEventFragment extends Fragment {
    private TextView mDataTime;
    private EditText mEventDescription, mEventName;
    private Button mMakeEvent, mBack;
    private final Calendar mCalendar = Calendar.getInstance();
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;

    public MakeEventFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_make_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mBack = view.findViewById(R.id.closeFragment);
        mDataTime = view.findViewById(R.id.dataTime);
        mMakeEvent = view.findViewById(R.id.makeEventButton);
        mEventName = view.findViewById(R.id.eventName);
        mEventDescription = view.findViewById(R.id.eventDescription);
        mDataTime.setOnClickListener(v -> showDatePicker());

        configureMakeEventButton();
        configureBackButton();
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, monthOfYear, dayOfMonth) -> {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, monthOfYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            showTimePicker();
        }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mCalendar.set(Calendar.MINUTE, minute);
            updateLabel();
        }, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), false).show();
    }

    private void updateLabel() {
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault());
        mDataTime.setText(format.format(mCalendar.getTime()));
    }

    private void configureMakeEventButton() {
        mMakeEvent.setOnClickListener(v -> {
            String name = mEventName.getText().toString().trim();
            String dataTime = mDataTime.getText().toString().trim();
            String description = mEventDescription.getText().toString().trim();

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(dataTime) && description.length() <= 50) {
                if (mCurrentUser != null) {
                    sendToFirebase(name, description);
                }
            } else {
                if (TextUtils.isEmpty(name)) {
                    mEventName.setError("Name is required");
                }
                if (TextUtils.isEmpty(mDataTime.getText().toString())) {
                    Toast.makeText(getContext(), "Please add the time", Toast.LENGTH_SHORT).show();
                }
                if (description.length() >= 50) {
                    mEventDescription.setError("Too big description (>50)");
                }
            }
        });
    }

    private void configureBackButton() {
        mBack.setOnClickListener(v -> {
            if (mCurrentUser != null) {
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.replace(new HomeFragment());
                }
            }
        });
    }

    private void sendToFirebase(String name, String description) {
        if (mCurrentUser == null || TextUtils.isEmpty(mCurrentUser.getEmail())) {
            Toast.makeText(getContext(), "User is not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        com.google.firebase.Timestamp timestamp = new com.google.firebase.Timestamp(mCalendar.getTime());

        ProfileFragment profileFragment = (ProfileFragment) getParentFragmentManager().findFragmentByTag("ProfileFragment");

        Map<String, Object> event = new HashMap<>();
        event.put("name", name);
        if (!TextUtils.isEmpty(description)) {
            event.put("description", description);
        }
        event.put("dateTime", timestamp);
        event.put("AEmail",mCurrentUser.getEmail());
        event.put("username", profileFragment.getUsername());

        mFirestore.collection("Events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> mFirestore.collection("Events")
                        .add(event)
                        .addOnSuccessListener(documentReference ->
                                Toast.makeText(getContext(), "Event created successfully!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to find user by email", Toast.LENGTH_SHORT).show());

        mEventName.getText().clear();
        mEventDescription.getText().clear();
        mDataTime.setText("");
    }
}

