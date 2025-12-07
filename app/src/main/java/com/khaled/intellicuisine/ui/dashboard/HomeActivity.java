package com.khaled.intellicuisine.ui.dashboard;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.khaled.intellicuisine.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Fragment homeFragment = new HomeFragment();
        Fragment inventoryFragment = new InventoryFragment();
        Fragment favoritesFragment = new FavoritesFragment();
        Fragment profileFragment = new ProfileFragment();

        loadFragment(homeFragment);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navInventory = findViewById(R.id.navInventory);
        LinearLayout navFavorites = findViewById(R.id.navFavorites);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            loadFragment(homeFragment);
            updateNavUI(navHome, navInventory, navFavorites, navProfile);
        });

        navInventory.setOnClickListener(v -> {
            loadFragment(inventoryFragment);
            updateNavUI(navInventory, navHome, navFavorites, navProfile);
        });

        navFavorites.setOnClickListener(v -> {
            loadFragment(favoritesFragment);
            updateNavUI(navFavorites, navHome, navInventory, navProfile);
        });

        navProfile.setOnClickListener(v -> {
            loadFragment(profileFragment);
            updateNavUI(navProfile, navHome, navInventory, navFavorites);
        });

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            AddIngredientBottomSheet bottomSheet = new AddIngredientBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "addIngredientTag");
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void updateNavUI(LinearLayout selected, LinearLayout... others) {
        setSelected(selected, true);
        for (LinearLayout other : others) {
            setSelected(other, false);
        }
    }

    private void setSelected(LinearLayout container, boolean isSelected) {
        int color = isSelected ? getColor(R.color.primary_orange) : getColor(R.color.hint_text);
        ImageView icon = (ImageView) container.getChildAt(0);
        TextView text = (TextView) container.getChildAt(1);
        icon.setColorFilter(color);
        text.setTextColor(color);
    }
}