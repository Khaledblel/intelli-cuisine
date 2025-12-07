package com.khaled.intellicuisine.ui.dashboard;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.khaled.intellicuisine.R;
import com.khaled.intellicuisine.models.Ingredient;

public class AddIngredientBottomSheet extends BottomSheetDialogFragment {

    private LinearLayout container;
    private TextView btnAddRow;
    private Button btnSaveAll;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private int rowCount = 0;
    private final int MAX_ROWS = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup containerView, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_ingredient, containerView, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        container = view.findViewById(R.id.ingredientsContainer);
        btnAddRow = view.findViewById(R.id.btnAddRow);
        btnSaveAll = view.findViewById(R.id.btnSaveAll);

        addRow();

        btnAddRow.setOnClickListener(v -> addRow());
        btnSaveAll.setOnClickListener(v -> saveAllIngredients());

        return view;
    }

    private void addRow() {
        if (rowCount >= MAX_ROWS) return;

        View rowView = getLayoutInflater().inflate(R.layout.item_ingredient_row, container, false);

        ImageView btnRemove = rowView.findViewById(R.id.btnRemoveRow);
        btnRemove.setOnClickListener(v -> {
            container.removeView(rowView);
            rowCount--;
            updateAddButtonState();
        });

        container.addView(rowView);
        rowCount++;
        updateAddButtonState();
    }

    private void updateAddButtonState() {
        if (rowCount >= MAX_ROWS) {
            btnAddRow.setVisibility(View.GONE);
        } else {
            btnAddRow.setVisibility(View.VISIBLE);
        }
    }

    private void saveAllIngredients() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        WriteBatch batch = db.batch();
        CollectionReference ingredientsRef = db.collection("users").document(userId).collection("ingredients");

        boolean hasData = false;

        HashSet<String> uniqueNames = new HashSet<>();

        for (int i = 0; i < container.getChildCount(); i++) {
            View row = container.getChildAt(i);
            EditText etName = row.findViewById(R.id.etRowName);
            EditText etQty = row.findViewById(R.id.etRowQty);

            String name = etName.getText().toString().trim();
            String qty = etQty.getText().toString().trim();

            if (!TextUtils.isEmpty(name)) {
                String nameKey = name.toLowerCase();

                if (uniqueNames.contains(nameKey)) {
                    String message = "Doublon détecté : '" + name + "' est saisi plusieurs fois.";
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    return;
                }

                uniqueNames.add(nameKey);

                if (TextUtils.isEmpty(qty)) qty = "1";

                Ingredient ingredient = new Ingredient(name, qty);
                DocumentReference newDocRef = ingredientsRef.document();
                batch.set(newDocRef, ingredient);
                hasData = true;
            }
        }

        if (!hasData) {
            Toast.makeText(getContext(), "Veuillez entrer au moins un ingrédient", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveAll.setEnabled(false);
        btnSaveAll.setText("Envoi...");

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Inventaire mise à jour !", Toast.LENGTH_SHORT).show();
            dismiss();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnSaveAll.setEnabled(true);
            btnSaveAll.setText("Tout valider");
        });
    }
}