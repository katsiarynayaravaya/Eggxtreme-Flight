package com.example.eggxtremeflight;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.Toast;

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
                .setTitle(R.string.nickname_dialog_title)
                .setMessage(R.string.nickname_dialog_message)
                .setView(binding.getRoot())
                .setCancelable(false)
                .setPositiveButton(R.string.nickname_save, null)
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        saveButton.setOnClickListener(v -> {
            String nickname = String.valueOf(binding.nicknameEditText.getText()).trim();
            if (nickname.length() < 3 || nickname.length() > 16) {
                binding.nicknameInputLayout.setError(activity.getString(R.string.nickname_error_length));
                return;
            }
            binding.nicknameInputLayout.setError(null);
            saveButton.setEnabled(false);

            repository.saveNickname(activity, user, nickname, new UserRepository.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    if (dialog.isShowing()) dialog.dismiss();
                    if (onDone != null) onDone.run();
                }

                @Override
                public void onError(Exception e) {
                    saveButton.setEnabled(true);
                    Toast.makeText(activity, R.string.nickname_save_error, Toast.LENGTH_LONG).show();
                }
            });
        });
        return dialog;
    }
}