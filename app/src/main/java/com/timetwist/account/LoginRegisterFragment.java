package com.timetwist.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.timetwist.databinding.FragmentLoginRegisterBinding;
import com.timetwist.utils.ActivityUtils;

public class LoginRegisterFragment extends Fragment {
    private FragmentLoginRegisterBinding mBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentLoginRegisterBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.login.setOnClickListener(v -> ActivityUtils
                .changeToLoginActivity(requireActivity()));
        mBinding.register.setOnClickListener(v -> ActivityUtils
                .changeToRegisterActivity(requireActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}