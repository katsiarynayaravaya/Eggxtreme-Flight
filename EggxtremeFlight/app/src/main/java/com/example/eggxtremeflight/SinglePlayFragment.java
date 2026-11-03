package com.example.eggxtremeflight;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.eggxtremeflight.databinding.FragmentSinglePlayBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class SinglePlayFragment extends Fragment {
    private static final String TAG = "SessionSave";

    private FragmentSinglePlayBinding binding;
    private final SessionRepository sessionRepository = new SessionRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSinglePlayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Navigation.findNavController(view).popBackStack();
                    }
                }
        );

        binding.gameSurfaceView.setOnGameOverListener(this::handleGameOver);
    }

    private void handleGameOver(int score, String skinId, List<Double> trajectory, int sampleIntervalMs,
                                 List<Double> pipeTops, int worldWidth) {
        Log.d(TAG, "Game over: score=" + score + ", skin=" + skinId
                + ", samples=" + trajectory.size()
                + ", pipes=" + pipeTops.size());

        View root = binding == null ? null : binding.getRoot();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showToast(R.string.session_not_signed_in);
            if (root != null) {
                GameOverFragment.open(root, score, -1, false, null);
            }
            return;
        }

        Context context = getContext();
        String nickname = context == null ? null : UserRepository.getCachedNickname(context);
        float scrollSpeed = binding == null ? 0f : binding.gameSurfaceView.getScrollSpeed();
        float spawnInterval = binding == null ? 0f : binding.gameSurfaceView.getSpawnInterval();
        sessionRepository.saveSession(user, score, skinId, trajectory, sampleIntervalMs,
                pipeTops, worldWidth, nickname, scrollSpeed, spawnInterval,
                new UserRepository.Callback<SessionRepository.SaveResult>() {
                    @Override
                    public void onSuccess(SessionRepository.SaveResult result) {
                        Log.d(TAG, "Saved: id=" + result.sessionId
                                + ", newRecord=" + result.newRecord
                                + ", best=" + result.bestScore);
                        if (result.newRecord) {
                            showToast(R.string.session_new_record, score);
                        } else {
                            showToast(R.string.session_saved, score, result.bestScore);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Save failed", e);
                        showToast(R.string.session_save_error);
                    }
                });
        if (root != null) {
            GameOverFragment.open(root, score, -1, false, null);
        }
    }

    private void showToast(int stringRes, Object... args) {
        Context context = getContext();
        if (context == null) return;
        Toast.makeText(context, context.getString(stringRes, args), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.gameSurfaceView.setOnGameOverListener(null);
        }
        binding = null;
    }
}