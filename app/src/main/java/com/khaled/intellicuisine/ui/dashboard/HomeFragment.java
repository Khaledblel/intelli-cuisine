package com.khaled.intellicuisine.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.khaled.intellicuisine.R;
import com.khaled.intellicuisine.adapters.RecipeAdapter;
import com.khaled.intellicuisine.models.Ingredient;
import com.khaled.intellicuisine.models.Recipe;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import androidx.core.content.ContextCompat;

public class HomeFragment extends Fragment {

    private TextView tvGreeting;
    private RecyclerView rvRecentRecipes;
    private RecyclerView rvInventoryHome;
    private View emptyRecentRecipes;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGreeting = view.findViewById(R.id.tvGreeting);
        rvRecentRecipes = view.findViewById(R.id.rvRecentRecipes);
        rvInventoryHome = view.findViewById(R.id.rvInventoryHome);
        emptyRecentRecipes = view.findViewById(R.id.emptyRecentRecipes);
        View btnGenerate = view.findViewById(R.id.btnGenerate);
        TextView tvHomeTitle = view.findViewById(R.id.tvHomeTitle);
        TextView tvInventorySeeAll = view.findViewById(R.id.tvInventorySeeAll);

        String fullText = getString(R.string.home_card_title);
        SpannableString spannable = new SpannableString(fullText);
        String target = "cuisinons";
        int startIndex = fullText.indexOf(target);
        if (startIndex != -1) {
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.primary_orange)),
                    startIndex, startIndex + target.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvHomeTitle.setText(spannable);
        }

        if (tvInventorySeeAll != null) {
            tvInventorySeeAll.setOnClickListener(v -> {
                if (getActivity() != null) {
                    View navInventory = getActivity().findViewById(R.id.navInventory);
                    if (navInventory != null) {
                        navInventory.performClick();
                    }
                }
            });
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null) {
            tvGreeting.setText(getString(R.string.home_greeting, user.getDisplayName()));
        } else {
            tvGreeting.setText(getString(R.string.home_greeting, "Chef"));
        }

        setupRecipeCarousel();
        setupInventoryCarousel();

        btnGenerate.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), RecipeGenerationActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecipeCarousel() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("recipes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    List<Recipe> recipes = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        Recipe recipe = doc.toObject(Recipe.class);
                        if (recipe != null) {
                            recipe.setId(doc.getId());
                            recipes.add(recipe);
                        }
                    }

                    if (recipes.isEmpty()) {
                        rvRecentRecipes.setVisibility(View.GONE);
                        emptyRecentRecipes.setVisibility(View.VISIBLE);
                    } else {
                        rvRecentRecipes.setVisibility(View.VISIBLE);
                        emptyRecentRecipes.setVisibility(View.GONE);
                        
                        RecipeAdapter adapter = new RecipeAdapter(recipes, recipe -> {
                            Intent intent = new Intent(getContext(), RecipeGenerationActivity.class);
                            intent.putExtra("RECIPE_ID", recipe.getId());
                            startActivity(intent);
                        });
                        rvRecentRecipes.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                        rvRecentRecipes.setAdapter(adapter);
                    }
                });
    }

    private void setupInventoryCarousel() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("ingredients")
                .limit(10)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    List<Recipe> dummyRecipes = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        String name = doc.getString("name");
                        if (name != null) {
                            if (name.length() > 1) {
                                name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
                            } else {
                                name = name.toUpperCase();
                            }
                            Recipe r = new Recipe();
                            r.setTitle(name);
                            dummyRecipes.add(r);
                        }
                    }

                    RecipeAdapter adapter = new RecipeAdapter(dummyRecipes, null);
                    rvInventoryHome.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                    rvInventoryHome.setAdapter(adapter);
                });
    }
}