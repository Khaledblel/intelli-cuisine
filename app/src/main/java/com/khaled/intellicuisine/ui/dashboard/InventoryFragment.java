package com.khaled.intellicuisine.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.khaled.intellicuisine.R;
import com.khaled.intellicuisine.adapters.IngredientAdapter;
import com.khaled.intellicuisine.models.Ingredient;
import android.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class InventoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private IngredientAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout emptyStateView;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration firestoreListener;

    public InventoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.recyclerViewInventory);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateView = view.findViewById(R.id.emptyStateView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new IngredientAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnIngredientActionListener(ingredient -> showDeleteConfirmation(ingredient));

        loadIngredientsRealtime();
    }

    private void loadIngredientsRealtime() {
        if (mAuth.getCurrentUser() == null) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        firestoreListener = db.collection("users").document(userId).collection("ingredients")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Erreur chargement", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    if (value != null) {
                        List<Ingredient> list = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Ingredient i = doc.toObject(Ingredient.class);
                            if (i != null) {
                                i.setId(doc.getId());
                                list.add(i);
                            }
                        }

                        adapter.setIngredients(list);
                        progressBar.setVisibility(View.GONE);

                        if (list.isEmpty()) {
                            emptyStateView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            emptyStateView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void performDelete(Ingredient ingredient) {
        if (mAuth.getCurrentUser() == null || ingredient.getId() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("ingredients")
                .document(ingredient.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Ingrédient supprimé", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteConfirmation(Ingredient ingredient) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Supprimer l'ingrédient ?")
                .setMessage("Voulez-vous vraiment retirer '" + ingredient.getName() + "' ?")
                .setPositiveButton("Supprimer", (d, which) -> performDelete(ingredient))
                .setNegativeButton("Annuler", null)
                .show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
        );
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }
}