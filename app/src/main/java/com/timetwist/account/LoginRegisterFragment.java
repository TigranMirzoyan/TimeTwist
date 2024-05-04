package com.timetwist.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.timetwist.utils.ActivityUtils;
import com.timetwist.R;

public class LoginRegisterFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_register,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView mRegister = view.findViewById(R.id.regBtn);
        TextView mLogin = view.findViewById(R.id.loginBtn);

        mLogin.setOnClickListener(v -> ActivityUtils.changeToLoginActivity(requireActivity()));
        mRegister.setOnClickListener(v -> ActivityUtils.changeToRegisterActivity(requireActivity()));
    }
}
