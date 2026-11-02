package com.example.eggxtremeflight;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private final UserRepository userRepository = new UserRepository();
    private AlertDialog nicknameDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        startAuthFlow();
    }

    private void startAuthFlow() {
        userRepository.ensureSignedIn(new UserRepository.Callback<FirebaseUser>() {
            @Override
            public void onSuccess(FirebaseUser user) {
                if (isFinishing() || isDestroyed()) return;
                checkNickname(user);
            }

            @Override
            public void onError(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(MainActivity.this, R.string.auth_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkNickname(FirebaseUser user) {
        if (UserRepository.getCachedNickname(this) != null) return;

        userRepository.loadNickname(this, user, new UserRepository.Callback<String>() {
            @Override
            public void onSuccess(String nickname) {
                if (isFinishing() || isDestroyed()) return;
                if (nickname == null) {
                    nicknameDialog = NicknameDialog.show(MainActivity.this, user, userRepository, null);
                }
            }

            @Override
            public void onError(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(MainActivity.this, R.string.auth_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (nicknameDialog != null && nicknameDialog.isShowing()) {
            nicknameDialog.dismiss();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.loadLocale(newBase));
    }
}