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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.khaled.intellicuisine.R;
import com.khaled.intellicuisine.models.Ingredient;

import java.util.HashSet;
import java.util.Set;

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

    private void resetButtonState() {
        btnSaveAll.setEnabled(true);
        btnSaveAll.setText("Tout valider");
    }

    private void saveAllIngredients() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        HashSet<String> localCheckSet = new HashSet<>();
        boolean hasLocalData = false;

        for (int i = 0; i < container.getChildCount(); i++) {
            View row = container.getChildAt(i);
            EditText etName = row.findViewById(R.id.etRowName);
            String rawName = etName.getText().toString().trim();

            if (!TextUtils.isEmpty(rawName)) {
                String nameToSave = rawName.toLowerCase();

                if (localCheckSet.contains(nameToSave)) {
                    Toast.makeText(getContext(), "Doublon local détecté : " + rawName, Toast.LENGTH_LONG).show();
                    return;
                }
                localCheckSet.add(nameToSave);
                hasLocalData = true;
            }
        }

        if (!hasLocalData) {
            Toast.makeText(getContext(), "Veuillez entrer au moins un ingrédient", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveAll.setEnabled(false);
        btnSaveAll.setText("Vérification...");

        CollectionReference ingredientsRef = db.collection("users").document(userId).collection("ingredients");

        ingredientsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {

            Set<String> existingDbNames = new HashSet<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String dbName = doc.getString("name");
                if (dbName != null) existingDbNames.add(dbName.toLowerCase());
            }

            WriteBatch batch = db.batch();

            for (int i = 0; i < container.getChildCount(); i++) {
                View row = container.getChildAt(i);
                EditText etName = row.findViewById(R.id.etRowName);
                EditText etQty = row.findViewById(R.id.etRowQty);

                String rawName = etName.getText().toString().trim();
                String qty = etQty.getText().toString().trim();

                if (!TextUtils.isEmpty(rawName)) {
                    String nameToSave = rawName.toLowerCase();

                    if (existingDbNames.contains(nameToSave)) {
                        Toast.makeText(getContext(), "Le frigo contient déjà : " + rawName, Toast.LENGTH_LONG).show();
                        resetButtonState();
                        return;
                    }

                    if (TextUtils.isEmpty(qty)) qty = "1";

                    Ingredient ingredient = new Ingredient(nameToSave, qty);
                    DocumentReference newDocRef = ingredientsRef.document();
                    batch.set(newDocRef, ingredient);
                }
            }

            btnSaveAll.setText("Envoi...");
            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Frigo mis à jour !", Toast.LENGTH_SHORT).show();
                dismiss();
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                resetButtonState();
            });

        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Erreur lors de la vérification : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            resetButtonState();
        });
    }
}