package com.example.eggxtremeflight;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eggxtremeflight.databinding.ItemSkinBinding;

import java.util.List;

public class SkinsAdapter extends RecyclerView.Adapter<SkinsAdapter.ViewHolder> {

    public interface OnSkinSelectedListener {
        void onSkinSelected(SkinItem skin);
    }

    private final List<SkinItem> skins;
    private final OnSkinSelectedListener listener;
    private String selectedId;

    public SkinsAdapter(List<SkinItem> skins, String selectedId, OnSkinSelectedListener listener) {
        this.skins = skins;
        this.selectedId = selectedId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSkinBinding binding = ItemSkinBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SkinItem skin = skins.get(position);
        Context context = holder.binding.getRoot().getContext();
        boolean selected = skin.id.equals(selectedId);
        String name = context.getString(skin.nameRes);

        holder.binding.skinPreview.setImageResource(skin.midRes);
        holder.binding.skinPreview.setContentDescription(name);
        holder.binding.skinName.setText(name);
        holder.binding.skinSelectButton.setText(selected ? R.string.shop_selected : R.string.shop_select);

        // Фон карточки и кнопки зависит от состояния "selected"
        holder.binding.getRoot().setSelected(selected);
        holder.binding.skinSelectButton.setSelected(selected);

        holder.binding.getRoot().setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            select(skins.get(adapterPosition));
        });
    }

    private void select(SkinItem skin) {
        if (skin.id.equals(selectedId)) return;

        int oldPosition = indexOf(selectedId);
        selectedId = skin.id;
        if (oldPosition >= 0) notifyItemChanged(oldPosition);
        notifyItemChanged(indexOf(skin.id));

        listener.onSkinSelected(skin);
    }

    private int indexOf(String skinId) {
        for (int i = 0; i < skins.size(); i++) {
            if (skins.get(i).id.equals(skinId)) return i;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return skins.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSkinBinding binding;

        ViewHolder(ItemSkinBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}