package com.example.eggxtremeflight;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.eggxtremeflight.databinding.FragmentMainMenuBinding;

public class MainMenuFragment extends Fragment {

    private FragmentMainMenuBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainMenuBinding.inflate(inflater, container, false);

        binding.singlePlayButton.setOnClickListener(view->{
            Navigation.findNavController(view).navigate(R.id.action_fragment_main_menu_to_fragment_single_play);
        });
        binding.shadowPlayButton.setOnClickListener(view->{
            Navigation.findNavController(view).navigate(R.id.action_fragment_main_menu_to_fragment_shadowboxing);
        });
        binding.shopButton.setOnClickListener(view->{
            Navigation.findNavController(view).navigate(R.id.action_fragment_main_menu_to_fragment_shop);
        });
        binding.leadersButton.setOnClickListener(view->{
            Navigation.findNavController(view).navigate(R.id.action_fragment_main_menu_to_fragment_leaders);
        });
        binding.settingsButton.setOnClickListener(view->{
            Navigation.findNavController(view).navigate(R.id.action_fragment_main_menu_to_fragment_settings);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
