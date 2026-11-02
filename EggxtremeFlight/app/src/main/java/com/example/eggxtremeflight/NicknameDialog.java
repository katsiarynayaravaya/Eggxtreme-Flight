package com.example.eggxtremeflight;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AlertDialog;

import com.example.eggxtremeflight.databinding.DialogNicknameBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseUser;

public class NicknameDialog {

    public static AlertDialog show(Activity activity, FirebaseUser user,
                                   UserRepository repository, Runnable onDone) {
        DialogNicknameBinding binding =
                DialogNicknameBinding.inflate(LayoutInflater.from(activity));

        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setView(binding.getRoot())
                .setCancelable(false)
                .create();
        dialog.setCanceledOnTouchOutside(false);

        Window window = dialog.getWindow();
        if (window != null) {
            // Убираем стандартный белый фон окна: рисовать будет наша панель
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        // Как только игрок начинает печатать, убираем сообщение об ошибке
        binding.nicknameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.nicknameError.setVisibility(View.GONE);
            }
        });

        // Кнопка "Готово" на клавиатуре работает как "Сохранить"
        binding.nicknameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.nicknameSaveButton.performClick();
                return true;
            }
            return false;
        });

        binding.nicknameSaveButton.setOnClickListener(v -> {
            String nickname = String.valueOf(binding.nicknameEditText.getText()).trim();
            if (nickname.length() < 3 || nickname.length() > 16) {
                showError(binding, activity.getString(R.string.nickname_error_length));
                return;
            }
            binding.nicknameError.setVisibility(View.GONE);
            setSaving(binding, true);

            repository.saveNickname(activity, user, nickname, new UserRepository.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    if (dialog.isShowing()) dialog.dismiss();
                    if (onDone != null) onDone.run();
                }

                @Override
                public void onError(Exception e) {
                    setSaving(binding, false);
                    showError(binding, activity.getString(R.string.nickname_save_error));
                }
            });
        });

        dialog.show();
        return dialog;
    }

    private static void showError(DialogNicknameBinding binding, String message) {
        binding.nicknameError.setText(message);
        binding.nicknameError.setVisibility(View.VISIBLE);
    }

    /** Пока идёт сохранение, кнопка блокируется и становится полупрозрачной. */
    private static void setSaving(DialogNicknameBinding binding, boolean saving) {
        binding.nicknameSaveButton.setEnabled(!saving);
        binding.nicknameSaveButton.setAlpha(saving ? 0.5f : 1f);
    }
}