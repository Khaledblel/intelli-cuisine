package com.khaled.intellicuisine.ui.dashboard;

import android.graphics.Bitmap;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.khaled.intellicuisine.R;
import com.khaled.intellicuisine.models.Ingredient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

public class AddIngredientBottomSheet extends BottomSheetDialogFragment {

    private LinearLayout container;
    private TextView btnAddRow;
    private Button btnSaveAll;
    private Button btnScan;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private int rowCount = 0;
    private final int MAX_ROWS = 5;

    private final ActivityResultLauncher<Void> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            this::analyzeImage
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup containerView, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_ingredient, containerView, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        container = view.findViewById(R.id.ingredientsContainer);
        btnAddRow = view.findViewById(R.id.btnAddRow);
        btnSaveAll = view.findViewById(R.id.btnSaveAll);
        btnScan = view.findViewById(R.id.btnScan);

        addRow();

        btnAddRow.setOnClickListener(v -> addRow());
        btnSaveAll.setOnClickListener(v -> saveAllIngredients());
        btnScan.setOnClickListener(v -> takePictureLauncher.launch(null));

        return view;
    }

    private void analyzeImage(Bitmap bitmap) {
        if (bitmap == null) return;

        btnScan.setText("Analyse en cours...");
        btnScan.setEnabled(false);
        btnSaveAll.setEnabled(false);

        GenerativeModel ai = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash");

        GenerativeModelFutures model = GenerativeModelFutures.from(ai);

        String promptText = "Identify the food ingredients in this picture. Return ONLY a JSON array of objects with keys 'name' (in French) and 'quantity' (estimate, e.g. '2' or '500g'). Example: [{\"name\": \"Pomme\", \"quantity\": \"3\"}]. Do not use markdown formatting.";

        Content content = new Content.Builder()
                .addImage(bitmap)
                .addText(promptText)
                .build();

        Executor executor = ContextCompat.getMainExecutor(requireContext());
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                parseAndPopulate(resultText);
                btnScan.setText("Scanner");
                btnScan.setEnabled(true);
                btnSaveAll.setEnabled(true);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(), "Erreur d'analyse IA", Toast.LENGTH_SHORT).show();
                btnScan.setText("Scanner");
                btnScan.setEnabled(true);
                btnSaveAll.setEnabled(true);
            }
        }, executor);
    }

    private void parseAndPopulate(String jsonResponse) {
        try {
            if (jsonResponse.startsWith("```json")) {
                jsonResponse = jsonResponse.substring(7);
            }
            if (jsonResponse.startsWith("```")) {
                jsonResponse = jsonResponse.substring(3);
            }
            if (jsonResponse.endsWith("```")) {
                jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
            }

            JSONArray array = new JSONArray(jsonResponse.trim());

            if (container.getChildCount() == 1) {
                View row = container.getChildAt(0);
                EditText etName = row.findViewById(R.id.etRowName);
                if (TextUtils.isEmpty(etName.getText())) {
                    container.removeView(row);
                    rowCount--;
                }
            }

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String name = obj.optString("name");
                String qty = obj.optString("quantity");
                addPopulatedRow(name, qty);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Format de réponse invalide", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPopulatedRow(String name, String qty) {
        View rowView = getLayoutInflater().inflate(R.layout.item_ingredient_row, container, false);

        EditText etName = rowView.findViewById(R.id.etRowName);
        EditText etQty = rowView.findViewById(R.id.etRowQty);

        etName.setText(name);
        etQty.setText(qty);

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