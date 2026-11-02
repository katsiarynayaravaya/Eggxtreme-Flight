package com.example.eggxtremeflight;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class SkinCatalog {
    public static final String PREFS_NAME = "GameSharedPreferences";
    public static final String KEY_SELECTED_SKIN = "selected_skin";

    private static final List<SkinItem> SKINS = Collections.unmodifiableList(Arrays.asList(
            new SkinItem("skin1", R.string.skin1_name,
                    R.drawable.skin1_chicken_up, R.drawable.skin1_chicken_mid, R.drawable.skin1_chicken_down),
            new SkinItem("skin2", R.string.skin2_name,
                    R.drawable.skin2_egg_up, R.drawable.skin2_egg_mid, R.drawable.skin2_egg_down),
            new SkinItem("skin3", R.string.skin3_name,
                    R.drawable.skin3_nagget_up, R.drawable.skin3_nagget_mid, R.drawable.skin3_nagget_down)
    ));

    private SkinCatalog() {
    }

    public static List<SkinItem> getAll() {
        return SKINS;
    }

    public static SkinItem findById(String id) {
        for (SkinItem skin : SKINS) {
            if (skin.id.equals(id)) {
                return skin;
            }
        }
        return SKINS.get(0);
    }

    public static SkinItem getSelectedSkin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return findById(prefs.getString(KEY_SELECTED_SKIN, SKINS.get(0).id));
    }

    public static void saveSelectedSkinId(Context context, String skinId) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_SELECTED_SKIN, skinId).apply();
    }
}