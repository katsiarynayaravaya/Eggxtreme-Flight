package com.example.eggxtremeflight;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private static final String TAG = "SinglePlay";
    private FragmentSinglePlayBinding binding;
    private SessionRepository sessionRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSinglePlayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        sessionRepository = new SessionRepository();

        SharedPreferences prefs = requireActivity().getSharedPreferences("GameSharedPreferences", Context.MODE_PRIVATE);
        String skinId = prefs.getString("selected_skin", "skin1");

        binding.gameSurfaceView.setOnGameOverListener((score, currentSkinId, trajectory, sampleIntervalMs) -> {
            handleGameOver(score, currentSkinId, trajectory, sampleIntervalMs);
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Navigation.findNavController(view).popBackStack();
                    }
                }
        );
    }

    private void handleGameOver(int score, String skinId, List<Double> trajectory, int sampleIntervalMs) {
        Log.d(TAG, "Game over: score=" + score + ", skin=" + skinId + ", samples=" + trajectory.size());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "User not signed in");
            return;
        }

        sessionRepository.saveSession(user, score, skinId, trajectory, sampleIntervalMs,
                new SessionRepository.Callback<SessionRepository.SaveResult>() {
                    @Override
                    public void onSuccess(SessionRepository.SaveResult result) {
                        Log.d(TAG, "Session saved: id=" + result.sessionId);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Failed to save session", e);
                    }
                });
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
