package com.example.eggxtremeflight;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eggxtremeflight.databinding.FragmentShopBinding;

public class ShopFragment extends Fragment {
    private FragmentShopBinding binding;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences("GameSharedPreferences", Context.MODE_PRIVATE);
        String savedSkin = sharedPreferences.getString("selected_skin", "skin1");
        binding.btnSkin1.setOnClickListener(v -> {
            saveSkin("skin1");
        });

        binding.btnSkin2.setOnClickListener(v -> {
            saveSkin("skin2");
        });

        binding.btnSkin3.setOnClickListener(v -> {
            saveSkin("skin3");
        });
    }

    private void saveSkin(String skinId) {
        sharedPreferences.edit().putString("selected_skin", skinId).apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
