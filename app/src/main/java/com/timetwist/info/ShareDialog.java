package com.timetwist.info;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.timetwist.databinding.FragmentShareBinding;

import java.util.Objects;

public class ShareDialog extends DialogFragment {
    private final String mBase64;
    private FragmentShareBinding mBinding;

    public ShareDialog(String base64) {
        mBase64 = base64;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        mBinding = FragmentShareBinding.inflate(LayoutInflater.from(requireContext()));
        mBinding.placeTitle.setText(mBase64);

        builder.setView(mBinding.getRoot());
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}