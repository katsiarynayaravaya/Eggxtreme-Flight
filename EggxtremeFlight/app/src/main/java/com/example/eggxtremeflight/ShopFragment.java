package com.example.eggxtremeflight;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.eggxtremeflight.databinding.FragmentShopBinding;

public class ShopFragment extends Fragment {
    private FragmentShopBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = requireContext();

        // Количество колонок зависит от ширины экрана: 2 на телефоне, до 4 на планшете
        int screenWidthDp = getResources().getConfiguration().screenWidthDp;
        int columns = Math.max(2, Math.min(4, screenWidthDp / 180));

        String selectedId = SkinCatalog.getSelectedSkin(context).id;
        SkinsAdapter adapter = new SkinsAdapter(
                SkinCatalog.getAll(),
                selectedId,
                skin -> SkinCatalog.saveSelectedSkinId(context, skin.id));

        binding.skinsRecycler.setLayoutManager(new GridLayoutManager(context, columns));
        binding.skinsRecycler.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}