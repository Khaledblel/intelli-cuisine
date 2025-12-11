package com.khaled.intellicuisine.ui.dashboard;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerationConfig;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.firebase.ai.type.ImagePart;
import com.google.firebase.ai.type.Part;
import com.google.firebase.ai.type.ResponseModality;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.khaled.intellicuisine.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

public class RecipeGenerationActivity extends AppCompatActivity {

    private TextView tabIngredients, tabInstructions, tabTips, tvContent;
    private TextView tvRecipeTitle, tvItemsCount, tvTime, tvDifficulty;
    private View headerBg;
    private ImageView imgFood;
    private LinearLayout ingredientsContainer;
    private LinearLayout tipsContainer;
    private LinearLayout instructionsContainer;
    private int selectedColor, unselectedColor;
    private Button btnStart;
    
    private View loadingView;
    private View contentScrollView;
    private TextView tvLoadingText;

    private List<JSONObject> ingredientsList = new ArrayList<>();
    private List<JSONObject> instructionsList = new ArrayList<>();
    private List<String> tipsList = new ArrayList<>();
    private String servingsText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_generation);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadingView = findViewById(R.id.loadingView);
        contentScrollView = findViewById(R.id.contentScrollView);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        headerBg = findViewById(R.id.headerBg);
        imgFood = findViewById(R.id.imgFood);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        tipsContainer = findViewById(R.id.tipsContainer);
        instructionsContainer = findViewById(R.id.instructionsContainer);

        tvRecipeTitle = findViewById(R.id.tvRecipeTitle);
        
        LinearLayout statsContainer = findViewById(R.id.statsContainer);
        LinearLayout itemsLayout = (LinearLayout) statsContainer.getChildAt(0);
        LinearLayout itemsInner = (LinearLayout) itemsLayout.getChildAt(0);
        tvItemsCount = (TextView) itemsInner.getChildAt(1);

        LinearLayout timeLayout = (LinearLayout) statsContainer.getChildAt(1);
        LinearLayout timeInner = (LinearLayout) timeLayout.getChildAt(0);
        tvTime = (TextView) timeInner.getChildAt(1);

        LinearLayout diffLayout = (LinearLayout) statsContainer.getChildAt(2);
        LinearLayout diffInner = (LinearLayout) diffLayout.getChildAt(0);
        tvDifficulty = (TextView) diffInner.getChildAt(1);

        tabIngredients = findViewById(R.id.tabIngredients);
        tabInstructions = findViewById(R.id.tabInstructions);
        tabTips = findViewById(R.id.tabTips);
        tvContent = findViewById(R.id.tvContent);
        btnStart = findViewById(R.id.btnStart);

        selectedColor = ContextCompat.getColor(this, R.color.recipe_tab_selected);
        unselectedColor = ContextCompat.getColor(this, R.color.hint_text);

        tabIngredients.setOnClickListener(v -> selectTab(tabIngredients));
        tabInstructions.setOnClickListener(v -> selectTab(tabInstructions));
        tabTips.setOnClickListener(v -> selectTab(tabTips));

        generateRecipe();
    }

    private void selectTab(TextView selectedTab) {
        tabIngredients.setTextColor(unselectedColor);
        tabInstructions.setTextColor(unselectedColor);
        tabTips.setTextColor(unselectedColor);

        selectedTab.setTextColor(selectedColor);

        tvContent.setVisibility(View.GONE);
        ingredientsContainer.setVisibility(View.GONE);
        tipsContainer.setVisibility(View.GONE);
        instructionsContainer.setVisibility(View.GONE);

        if (selectedTab == tabIngredients) {
            ingredientsContainer.setVisibility(View.VISIBLE);
        } else if (selectedTab == tabTips) {
            tipsContainer.setVisibility(View.VISIBLE);
        } else {
            instructionsContainer.setVisibility(View.VISIBLE);
        }
    }

    private void generateRecipe() {
        loadingView.setVisibility(View.VISIBLE);
        contentScrollView.setVisibility(View.GONE);
        btnStart.setVisibility(View.GONE);
        tvLoadingText.setText("Veuillez patienter, notre chef IA prépare votre recette...");

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            handleError("Utilisateur non connecté.");
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(userId).collection("ingredients")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> ingredients = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        if (name != null) ingredients.add(name);
                    }
                    callGeminiAI(ingredients);
                })
                .addOnFailureListener(e -> {
                    handleError("Erreur : Impossible de récupérer les ingrédients.");
                });
    }

    private void callGeminiAI(List<String> ingredients) {
        String ingredientsList = String.join(", ", ingredients);
        if (ingredients.isEmpty()) ingredientsList = "Aucun ingrédient spécifique (propose une recette simple)";

        String promptText = "Tu es un assistant culinaire expert. Ton rôle est de créer une recette simple et délicieuse à partir d'une liste d'ingrédients.\n\n" +
                "INGRÉDIENTS DISPONIBLES : " + ingredientsList + "\n\n" +
                "Tu dois impérativement répondre UNIQUEMENT avec un objet JSON valide, sans aucun texte avant ou après (pas de markdown).\n" +
                "Contexte : Cette réponse servira à alimenter une application de cuisine interactive étape par étape.\n" +
                "Structure le JSON exactement comme suit :\n" +
                "{\n" +
                "  \"titre\": \"Le nom créatif de la recette\",\n" +
                "  \"description\": \"Une description courte et alléchante.\",\n" +
                "  \"difficulte\": \"Niveau de difficulté (ex: Facile, Moyen, Difficile)\",\n" +
                "  \"servings\": \"Le nombre de personnes (ex: '2 personnes')\",\n" +
                "  \"temps_total_minutes\": un entier (durée totale),\n" +
                "  \"conseils\": [\n" +
                "    \"Une astuce de chef pour réussir la recette\",\n" +
                "    \"Une variation possible\"\n" +
                "  ],\n" +
                "  \"ingredients_necessaires\": [\n" +
                "    { \"nom\": \"nom de l'ingrédient\", \"quantite\": \"ex: 200g ou 2 unités\" }\n" +
                "  ],\n" +
                "  \"etapes\": [\n" +
                "    {\n" +
                "      \"etape_index\": 1,\n" +
                "      \"description\": \"Instruction claire et précise pour cette étape.\",\n" +
                "      \"ingredients_used\": [\"Nom des ingrédients (parmi la liste ingredients_necessaires) utilisés dans cette étape\"],\n" +
                "      \"equipment_used\": [\"Liste des ustensiles nécessaires pour cette étape (ex: planche, poêle) - à déduire\"],\n" +
                "      \"timer\": {\n" +
                "        \"active\": un booléen (true si une minuterie est nécessaire pour cuisson/repos, false sinon),\n" +
                "        \"duree_secondes\": un entier (0 si active est false)\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        GenerativeModel ai = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-3-pro-preview");

        GenerativeModelFutures model = GenerativeModelFutures.from(ai);

        Content prompt = new Content.Builder()
                .addText(promptText)
                .build();

        Executor executor = ContextCompat.getMainExecutor(this);
        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                parseAndDisplayRecipe(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                handleError("L'IA n'a pas pu générer la recette. Veuillez réessayer.");
            }
        }, executor);
    }

    private void parseAndDisplayRecipe(String jsonResponse) {
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

            JSONObject json = new JSONObject(jsonResponse.trim());

            String title = json.optString("titre", "Recette Mystère");
            String difficulty = json.optString("difficulte", "Moyen");
            int time = json.optInt("temps_total_minutes", 30);
            servingsText = json.optString("servings", "N/A");
            
            JSONArray ingredientsArray = json.optJSONArray("ingredients_necessaires");
            JSONArray stepsArray = json.optJSONArray("etapes");
            JSONArray tipsArray = json.optJSONArray("conseils");

            int itemsCount = (ingredientsArray != null) ? ingredientsArray.length() : 0;

            ingredientsList.clear();
            if (ingredientsArray != null) {
                for (int i = 0; i < ingredientsArray.length(); i++) {
                    ingredientsList.add(ingredientsArray.getJSONObject(i));
                }
            }
            populateIngredientsUI();

            instructionsList.clear();
            if (stepsArray != null) {
                for (int i = 0; i < stepsArray.length(); i++) {
                    instructionsList.add(stepsArray.getJSONObject(i));
                }
            }
            populateInstructionsUI();

            tipsList.clear();
            if (tipsArray != null) {
                for (int i = 0; i < tipsArray.length(); i++) {
                    tipsList.add(tipsArray.getString(i));
                }
            }
            populateTipsUI();

            tvRecipeTitle.setText(title);
            tvDifficulty.setText(difficulty);
            tvTime.setText(time + " Min");
            tvItemsCount.setText(itemsCount + " Items");

            selectTab(tabInstructions);
            
            generateRecipeImage(title);

        } catch (JSONException e) {
            e.printStackTrace();
            handleError("Erreur de formatage de la recette.");
        }
    }

    private void populateIngredientsUI() {
        ingredientsContainer.removeAllViews();
        for (JSONObject obj : ingredientsList) {
            String name = obj.optString("nom");
            String quantity = obj.optString("quantite");

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 16);
            row.setLayoutParams(params);
            row.setPadding(24, 24, 24, 24);
            
            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            shape.setCornerRadius(16f);
            shape.setColor(ContextCompat.getColor(this, R.color.input_background));
            row.setBackground(shape);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            android.widget.CheckBox checkBox = new android.widget.CheckBox(this);
            checkBox.setButtonTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_orange)));
            row.addView(checkBox);

            TextView tvName = new TextView(this);
            tvName.setText(name);
            tvName.setTextSize(16f);
            tvName.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            nameParams.setMargins(16, 0, 16, 0);
            tvName.setLayoutParams(nameParams);
            row.addView(tvName);

            TextView tvQty = new TextView(this);
            tvQty.setText(quantity);
            tvQty.setTextSize(14f);
            tvQty.setTextColor(ContextCompat.getColor(this, R.color.hint_text));
            row.addView(tvQty);

            row.setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));

            ingredientsContainer.addView(row);
        }
    }

    private void populateInstructionsUI() {
        instructionsContainer.removeAllViews();
        for (JSONObject step : instructionsList) {
            int index = step.optInt("etape_index");
            String description = step.optString("description");

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 24);
            row.setLayoutParams(params);
            row.setPadding(24, 24, 24, 24);

            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            shape.setCornerRadius(16f);
            shape.setColor(ContextCompat.getColor(this, R.color.input_background));
            row.setBackground(shape);
            row.setGravity(android.view.Gravity.TOP);

            TextView tvIndex = new TextView(this);
            tvIndex.setText(String.valueOf(index));
            tvIndex.setTextSize(14f);
            tvIndex.setTextColor(ContextCompat.getColor(this, R.color.white));
            tvIndex.setTypeface(null, android.graphics.Typeface.BOLD);
            tvIndex.setGravity(android.view.Gravity.CENTER);
            
            android.graphics.drawable.GradientDrawable circle = new android.graphics.drawable.GradientDrawable();
            circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            circle.setColor(ContextCompat.getColor(this, R.color.primary_orange));
            tvIndex.setBackground(circle);

            LinearLayout.LayoutParams indexParams = new LinearLayout.LayoutParams(60, 60);
            indexParams.setMargins(0, 0, 24, 0);
            tvIndex.setLayoutParams(indexParams);
            row.addView(tvIndex);

            TextView tvDesc = new TextView(this);
            tvDesc.setText(description);
            tvDesc.setTextSize(15f);
            tvDesc.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
            tvDesc.setLineSpacing(0, 1.2f);
            
            LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvDesc.setLayoutParams(descParams);
            row.addView(tvDesc);

            instructionsContainer.addView(row);
        }
    }

    private void populateTipsUI() {
        tipsContainer.removeAllViews();

        addTipRow("Portions", servingsText, R.drawable.ic_account);

        for (String tip : tipsList) {
            addTipRow("Conseil du Chef", tip, R.drawable.ic_favorite);
        }
    }

    private void addTipRow(String title, String content, int iconRes) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        row.setLayoutParams(params);
        row.setPadding(24, 24, 24, 24);

        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(16f);
        shape.setColor(ContextCompat.getColor(this, R.color.input_background));
        row.setBackground(shape);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(ContextCompat.getColor(this, R.color.primary_orange));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(48, 48);
        iconParams.setMargins(0, 0, 24, 0);
        icon.setLayoutParams(iconParams);
        row.addView(icon);

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(12f);
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.hint_text));
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        textContainer.addView(tvTitle);

        TextView tvContent = new TextView(this);
        tvContent.setText(content);
        tvContent.setTextSize(15f);
        tvContent.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
        tvContent.setPadding(0, 4, 0, 0);
        textContainer.addView(tvContent);

        row.addView(textContainer);
        tipsContainer.addView(row);
    }

    private void generateRecipeImage(String recipeTitle) {
        tvLoadingText.setText("Touches finales...");

        GenerativeModel ai = FirebaseAI.getInstance(GenerativeBackend.googleAI()).generativeModel(
            "gemini-2.5-flash-image",
            new GenerationConfig.Builder()
                .setResponseModalities(Arrays.asList(ResponseModality.TEXT, ResponseModality.IMAGE))
                .build()
        );

        GenerativeModelFutures model = GenerativeModelFutures.from(ai);

        String promptText = "Photographie culinaire professionnelle de \"" + recipeTitle + "\", présentation soignée. Lumière naturelle et douce, très détaillé, appétissant, style magazine culinaire.";

        Content prompt = new Content.Builder()
                .addText(promptText)
                .build();

        Executor executor = ContextCompat.getMainExecutor(this);
        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                Bitmap generatedImage = null;
                if (result.getCandidates() != null && !result.getCandidates().isEmpty()) {
                    for (Part part : result.getCandidates().get(0).getContent().getParts()) {
                        if (part instanceof ImagePart) {
                            ImagePart imagePart = (ImagePart) part;
                            generatedImage = imagePart.getImage();
                            break;
                        }
                    }
                }

                if (generatedImage != null) {
                    headerBg.setBackground(new BitmapDrawable(getResources(), generatedImage));
                    imgFood.setVisibility(View.GONE);
                } else {
                    imgFood.setVisibility(View.VISIBLE);
                }
                
                loadingView.setVisibility(View.GONE);
                contentScrollView.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.VISIBLE);
                btnStart.setEnabled(true);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                loadingView.setVisibility(View.GONE);
                contentScrollView.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.VISIBLE);
                btnStart.setEnabled(true);
                imgFood.setVisibility(View.VISIBLE);
                Toast.makeText(RecipeGenerationActivity.this, "Image non générée, affichage de la recette.", Toast.LENGTH_SHORT).show();
            }
        }, executor);
    }

    private void handleError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
}