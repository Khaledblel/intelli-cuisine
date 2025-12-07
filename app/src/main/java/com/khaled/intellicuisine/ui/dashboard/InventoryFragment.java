package com.khaled.intellicuisine.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
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

        adapter.setOnIngredientActionListener(new IngredientAdapter.OnIngredientActionListener() {
            @Override
            public void onDelete(Ingredient ingredient) {
                showDeleteConfirmation(ingredient);
            }

            @Override
            public void onEdit(Ingredient ingredient) {
                showEditIngredientDialog(ingredient);
            }
        });

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

    private void showEditIngredientDialog(Ingredient ingredient) {
        if (getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        layout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(getContext());
        title.setText("Modifier l'ingrédient");
        title.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, padding);
        layout.addView(title);

        EditText nameInput = new EditText(getContext());
        nameInput.setHint("Nom");
        nameInput.setText(ingredient.getName());
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameInput.setBackground(createRoundedDrawable(ContextCompat.getColor(getContext(), R.color.input_background), 30f));
        nameInput.setPadding(50, 40, 50, 40);
        nameInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        nameInput.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, padding / 2);
        nameInput.setLayoutParams(params);
        layout.addView(nameInput);

        EditText qtyInput = new EditText(getContext());
        qtyInput.setHint("Quantité");
        qtyInput.setText(ingredient.getQuantity());
        qtyInput.setInputType(InputType.TYPE_CLASS_TEXT);
        qtyInput.setBackground(createRoundedDrawable(ContextCompat.getColor(getContext(), R.color.input_background), 30f));
        qtyInput.setPadding(50, 40, 50, 40);
        qtyInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        qtyInput.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));
        
        LinearLayout.LayoutParams lastParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lastParams.setMargins(0, 0, 0, padding);
        qtyInput.setLayoutParams(lastParams);
        layout.addView(qtyInput);

        Button btnSave = new Button(getContext());
        btnSave.setText("Enregistrer");
        btnSave.setTextColor(Color.WHITE);
        btnSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        btnSave.setTypeface(null, android.graphics.Typeface.BOLD);
        
        GradientDrawable btnBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        ContextCompat.getColor(getContext(), R.color.primary_orange),
                        ContextCompat.getColor(getContext(), R.color.gradient_end_orange)
                });
        btnBg.setCornerRadius(30f);
        btnSave.setBackground(btnBg);
        btnSave.setElevation(10f);

        btnSave.setOnClickListener(v -> {
            String newName = nameInput.getText().toString().trim();
            String newQty = qtyInput.getText().toString().trim();

            if (newName.isEmpty()) {
                nameInput.setError("Champ requis");
                return;
            }
            if (newQty.isEmpty()) {
                qtyInput.setError("Champ requis");
                return;
            }

            performUpdate(ingredient, newName, newQty);
            dialog.dismiss();
        });
        layout.addView(btnSave);

        dialog.setContentView(layout);
        dialog.show();
    }

    private void performUpdate(Ingredient ingredient, String newName, String newQty) {
        if (mAuth.getCurrentUser() == null || ingredient.getId() == null) return;

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("ingredients")
                .document(ingredient.getId())
                .update("name", newName, "quantity", newQty)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Ingrédient mis à jour", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                });
    }

    private GradientDrawable createRoundedDrawable(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(radius);
        shape.setColor(color);
        return shape;
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