package com.timetwist.account;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timetwist.R;

public class LoginRegisterFragment extends Fragment {
    private TextView mRegister, mLogin;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_register,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRegister = view.findViewById(R.id.regBtn);
        mLogin = view.findViewById(R.id.loginBtn);

        configureLoginBtn();
        configureRegisterBtn();
    }

    public void configureRegisterBtn() {
        mRegister.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), RegisterActivity.class);
                getActivity().overridePendingTransition(0, 0);
                startActivity(intent);

                getActivity().finish();
            }
        });
    }

    public void configureLoginBtn() {
        mLogin.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0);
                getActivity().finish();
            }
        });
    }
}
