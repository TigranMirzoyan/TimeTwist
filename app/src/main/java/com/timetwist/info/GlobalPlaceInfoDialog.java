package com.timetwist.info;

import android.app.Dialog;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.timetwist.R;
import com.timetwist.databinding.FragmentGlobalPlaceInfoDialogBinding;
import com.timetwist.firebase.FirestoreServices;
import com.timetwist.utils.ActivityUtils;
import com.timetwist.utils.NetworkUtils;
import com.timetwist.utils.ToastUtils;

import java.util.Locale;
import java.util.Objects;

public class GlobalPlaceInfoDialog extends DialogFragment {
    private final OnFavoriteUpdateListener mUpdateListener;
    private final String mTitle;
    private final String mDescription;
    private FragmentGlobalPlaceInfoDialogBinding mBinding;
    private FirestoreServices mFirestoreServices;
    private ActivityUtils mActivityUtils;
    private TextToSpeech mTextToSpeech;
    private boolean mIsSpeaking = false;
    private boolean mIsFavorite;


    public interface OnFavoriteUpdateListener {
        void onFavoriteAdded(String title);

        void onFavoriteRemoved(String title);
    }

    public GlobalPlaceInfoDialog(String title, String description, Boolean bool,
                                 OnFavoriteUpdateListener updateListener) {
        mTitle = title;
        mDescription = description;
        mUpdateListener = updateListener;
        mIsFavorite = bool;
    }

    @Override
    public void onPause() {
        super.onPause();
        mTextToSpeech.stop();
        mIsSpeaking = !mIsSpeaking;
        changeSpeakerDrawable();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        mBinding = FragmentGlobalPlaceInfoDialogBinding.inflate(LayoutInflater.from(requireContext()));
        View view = mBinding.getRoot();
        mFirestoreServices = FirestoreServices.getInstance();
        mActivityUtils = ActivityUtils.getInstance();

        mBinding.title.setText(mTitle);
        mBinding.description.setMovementMethod(new ScrollingMovementMethod());
        mBinding.description.setText(mDescription);

        mBinding.cancelID.setOnClickListener(v -> {
            dismiss();
            mTextToSpeech.stop();
        });

        configureAdmin();
        initializeTextToSpeech();
        mBinding.favorite.setOnClickListener(v -> configureFavoriteButton());
        mBinding.readMore.setOnClickListener(v -> configureReadMoreButton());

        changeFavoriteButtonDrawable();
        builder.setView(view);
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        return dialog;
    }

    private void configureAdmin() {
        mActivityUtils.ifUserAdmin(requireContext(),
                () -> configureButtonsVisibility(true),
                () -> configureButtonsVisibility(false));
    }

    private void configureButtonsVisibility(Boolean check) {
        if (!check) {
            mBinding.delete.setVisibility(View.GONE);
            mBinding.favorite.setVisibility(View.VISIBLE);
            return;
        }
        mBinding.delete.setVisibility(View.VISIBLE);
        mBinding.favorite.setVisibility(View.GONE);
        mBinding.delete.setOnClickListener(v -> mFirestoreServices.deleteGlobalMarker(mTitle,
                success -> {
                    ToastUtils.show(requireContext(), success);
                    mActivityUtils.MAP_FRAGMENT.refreshGlobalMarkers(false, mTitle);
                    dismiss();
                },
                error -> ToastUtils.show(requireContext(), error)));
    }

    private void initializeTextToSpeech() {
        mBinding.textToSpeechLoading.setVisibility(View.VISIBLE);
        mTextToSpeech = new TextToSpeech(requireContext(), status -> {
            mBinding.textToSpeechLoading.setVisibility(View.GONE);
            if (status != TextToSpeech.SUCCESS) {
                Log.e("GlobalPlaceInfoDialog", "Initialization failed");
                return;
            }

            int result = mTextToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("GlobalPlaceInfoDialog", "Language not supported");
                return;
            }
            mBinding.textSpeech.setOnClickListener(v -> speakDescription());
        });

        mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {
                mIsSpeaking = !mIsSpeaking;
                changeSpeakerDrawable();
            }

            @Override
            public void onError(String utteranceId) {

            }
        });
    }

    private void configureFavoriteButton() {
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireContext(), "Internet required");
            return;
        }
        mIsFavorite = !mIsFavorite;
        changeFavoriteButtonDrawable();

        if (mIsFavorite) {
            mFirestoreServices.makeFavoriteLocation(mTitle,
                    success -> {
                        ToastUtils.show(requireContext(), success);
                        if (mUpdateListener != null) mUpdateListener.onFavoriteAdded(mTitle);
                    },
                    error -> ToastUtils.show(requireContext(), error));
            return;
        }

        mFirestoreServices.findFavoriteMarkerAndDelete(mTitle,
                success -> {
                    if (mUpdateListener == null) return;
                    mUpdateListener.onFavoriteRemoved(mTitle);
                    ToastUtils.show(requireContext(), success);
                },
                error -> ToastUtils.show(requireContext(), error));
    }

    private void configureReadMoreButton() {
        if (NetworkUtils.isInternetDisconnected(requireContext())) {
            ToastUtils.show(requireContext(), "Internet required");
            return;
        }
        String url = "https://en.wikipedia.org/wiki/"
                + mTitle.replaceAll(" ", "_");
        mActivityUtils.MAP_FRAGMENT.openWebViewActivity(url);
        dismiss();
    }

    private void speakDescription() {
        if (mTextToSpeech == null) return;
        mIsSpeaking = !mIsSpeaking;
        changeSpeakerDrawable();

        if (!mIsSpeaking) {
            mTextToSpeech.stop();
            return;
        }

        mTextToSpeech.speak(mDescription, TextToSpeech.QUEUE_ADD, null, "");
    }

    private void changeFavoriteButtonDrawable() {
        mBinding.favorite.setBackground(ContextCompat.getDrawable(requireContext(),
                mIsFavorite ? R.drawable.favorite_button_clicked
                        : R.drawable.favorite_button_not_clicked));
    }

    private void changeSpeakerDrawable() {
        mBinding.textSpeech.setBackground(ContextCompat.getDrawable(requireContext(),
                mIsSpeaking ? R.drawable.off_speaking : R.drawable.on_speaking));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTextToSpeech.stop();
        mTextToSpeech.shutdown();
        mBinding = null;
    }
}