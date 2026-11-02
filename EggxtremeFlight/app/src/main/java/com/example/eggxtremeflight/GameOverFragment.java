package com.example.eggxtremeflight;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.eggxtremeflight.databinding.FragmentGameOverBinding;

public class GameOverFragment extends Fragment {
    public static final String ARG_PLAYER_SCORE = "playerScore";
    public static final String ARG_SHADOW_SCORE = "shadowScore";
    public static final String ARG_FROM_SHADOW = "fromShadow";

    private FragmentGameOverBinding binding;

    public static void open(View view, int playerScore, int shadowScore, boolean fromShadow) {
        Bundle args = new Bundle();
        args.putInt(ARG_PLAYER_SCORE, playerScore);
        args.putInt(ARG_SHADOW_SCORE, shadowScore);
        args.putBoolean(ARG_FROM_SHADOW, fromShadow);
        Navigation.findNavController(view).navigate(R.id.fragment_game_over, args);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGameOverBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        int playerScore = args == null ? 0 : args.getInt(ARG_PLAYER_SCORE);
        int shadowScore = args == null ? 0 : args.getInt(ARG_SHADOW_SCORE);
        boolean fromShadow = args != null && args.getBoolean(ARG_FROM_SHADOW);

        binding.yourScoreValue.setText(String.valueOf(playerScore));
        if (fromShadow) {
            binding.shadowScoreValue.setText(String.valueOf(shadowScore));
            binding.duelResult.setText(resultText(playerScore, shadowScore));
        } else {
            binding.shadowScoreRow.setVisibility(View.GONE);
            binding.duelResult.setVisibility(View.GONE);
        }

        binding.rematchButton.setOnClickListener(v -> rematch(fromShadow));
        binding.menuButton.setOnClickListener(v -> goToMainMenu());

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        goToMainMenu();
                    }
                }
        );
    }

    private String resultText(int playerScore, int shadowScore) {
        if (playerScore > shadowScore) return getString(R.string.shadow_win);
        if (playerScore < shadowScore) return getString(R.string.shadow_lose);
        return getString(R.string.shadow_draw);
    }

    private void goToMainMenu() {
        Navigation.findNavController(requireView()).popBackStack(R.id.fragment_main_menu, false);
    }

    private void rematch(boolean fromShadow) {
        NavController nav = Navigation.findNavController(requireView());
        nav.popBackStack(R.id.fragment_main_menu, false);
        nav.navigate(fromShadow
                ? R.id.action_fragment_main_menu_to_fragment_shadowboxing
                : R.id.action_fragment_main_menu_to_fragment_single_play);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
