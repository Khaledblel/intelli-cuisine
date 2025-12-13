package com.khaled.intellicuisine.ui.dashboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.khaled.intellicuisine.R;
import com.khaled.intellicuisine.models.Recipe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class RecipeGenerationActivity extends AppCompatActivity {

    private TextView tabIngredients, tabInstructions, tabTips, tvContent;
    private TextView tvRecipeTitle, tvItemsCount, tvTime, tvDifficulty;
    private View headerBg;
    private ImageView imgFood;
    private ImageView btnFavorite;
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

    private String currentTitle;
    private String currentDifficulty;
    private int currentTime;
    private String currentServings;
    private String currentImageUrl;
    private String currentRecipeId;
    private boolean isFavorite = false;

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

        btnFavorite = findViewById(R.id.btnFavorite);
        btnFavorite.setOnClickListener(v -> toggleFavorite());

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

        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(RecipeGenerationActivity.this, CookingModeActivity.class);
            
            JSONArray stepsArray = new JSONArray();
            for (JSONObject step : instructionsList) {
                try {
                    JSONObject cleanStep = new JSONObject(step.toString());
                    stepsArray.put(cleanStep);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            
            intent.putExtra("STEPS_DATA", stepsArray.toString());
            intent.putExtra("RECIPE_TITLE", currentTitle);
            intent.putExtra("RECIPE_IMAGE_URL", currentImageUrl);
            
            // Still pass local path for immediate loading if available (optional optimization)
            // Save image to cache and pass path to avoid TransactionTooLargeException
            // (We can keep this logic if we have the bitmap in memory, but primarily rely on URL now)
             if (headerBg.getBackground() instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) headerBg.getBackground()).getBitmap();
                String imagePath = saveImageToCache(bitmap);
                if (imagePath != null) {
                    intent.putExtra("RECIPE_IMAGE_PATH", imagePath);
                }
            }
            
            intent.putExtra("RECIPE_ID", currentRecipeId);
            startActivity(intent);
        });

        String recipeId = getIntent().getStringExtra("RECIPE_ID");
        if (recipeId != null) {
            currentRecipeId = recipeId;
            loadRecipeFromFirestore(recipeId);
            checkIfFavorite();
        } else {
            generateRecipe();
        }
    }

    private String saveImageToCache(Bitmap bitmap) {
        try {
            java.io.File cachePath = new java.io.File(getCacheDir(), "images");
            if (!cachePath.exists()) {
                cachePath.mkdirs();
            }
            java.io.File file = new java.io.File(cachePath, "temp_recipe_image.jpg");
            java.io.FileOutputStream stream = new java.io.FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            stream.close();
            return file.getAbsolutePath();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
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

    private void loadRecipeFromFirestore(String recipeId) {
        loadingView.setVisibility(View.VISIBLE);
        contentScrollView.setVisibility(View.GONE);
        btnStart.setVisibility(View.GONE);
        tvLoadingText.setText("Chargement de la recette...");

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .collection("recipes").document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Recipe recipe = documentSnapshot.toObject(Recipe.class);
                    if (recipe != null) {
                        displayLoadedRecipe(recipe);
                    } else {
                        handleError("Recette introuvable.");
                    }
                })
                .addOnFailureListener(e -> handleError("Erreur de chargement."));
    }

    private void displayLoadedRecipe(Recipe recipe) {
        currentTitle = recipe.getTitle();
        currentDifficulty = recipe.getDifficulty();
        currentTime = recipe.getTimeMinutes();
        currentServings = recipe.getServings();
        currentImageUrl = recipe.getImageUrl();

        tvRecipeTitle.setText(currentTitle);
        tvDifficulty.setText(currentDifficulty);
        tvTime.setText(currentTime + " Min");
        
        ingredientsList.clear();
        if (recipe.getIngredients() != null) {
            for (Map<String, Object> map : recipe.getIngredients()) {
                ingredientsList.add(new JSONObject(map));
            }
        }
        tvItemsCount.setText(ingredientsList.size() + " Items");
        populateIngredientsUI();

        instructionsList.clear();
        if (recipe.getSteps() != null) {
            for (Map<String, Object> map : recipe.getSteps()) {
                instructionsList.add(new JSONObject(map));
            }
        }
        populateInstructionsUI();

        tipsList.clear();
        if (recipe.getTips() != null) {
            tipsList.addAll(recipe.getTips());
        }
        servingsText = currentServings;
        populateTipsUI();

        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(currentImageUrl);
                    Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    runOnUiThread(() -> {
                        headerBg.setBackground(new BitmapDrawable(getResources(), bmp));
                        headerBg.setBackgroundTintList(null);
                        imgFood.setVisibility(View.GONE);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        loadingView.setVisibility(View.GONE);
        contentScrollView.setVisibility(View.VISIBLE);
        btnStart.setVisibility(View.VISIBLE);
        selectTab(tabInstructions);

        checkIfFavorite();
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

            currentTitle = json.optString("titre", "Recette Mystère");
            currentDifficulty = json.optString("difficulte", "Moyen");
            currentTime = json.optInt("temps_total_minutes", 30);
            currentServings = json.optString("servings", "N/A");
            servingsText = currentServings;
            
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

            tvRecipeTitle.setText(currentTitle);
            tvDifficulty.setText(currentDifficulty);
            tvTime.setText(currentTime + " Min");
            tvItemsCount.setText(itemsCount + " Items");

            selectTab(tabInstructions);
            
            generateRecipeImage(currentTitle);

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
                    headerBg.setBackgroundTintList(null);
                    imgFood.setVisibility(View.GONE);

                    String path = "recipes/" + System.currentTimeMillis() + ".jpg";
                    uploadImageToStorage(generatedImage, path, url -> {
                        currentImageUrl = url;
                        saveRecipeToFirestore();
                        
                        loadingView.setVisibility(View.GONE);
                        contentScrollView.setVisibility(View.VISIBLE);
                        btnStart.setVisibility(View.VISIBLE);
                        btnStart.setEnabled(true);
                    });
                } else {
                    imgFood.setVisibility(View.VISIBLE);
                    currentImageUrl = "";
                    saveRecipeToFirestore();
                    
                    loadingView.setVisibility(View.GONE);
                    contentScrollView.setVisibility(View.VISIBLE);
                    btnStart.setVisibility(View.VISIBLE);
                    btnStart.setEnabled(true);
                }
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
                
                currentImageUrl = "";
                saveRecipeToFirestore();
            }
        }, executor);
    }

    private void uploadImageToStorage(Bitmap bitmap, String path, OnImageUploadListener listener) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(path);

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                if (listener != null) listener.onSuccess(downloadUri.toString());
            } else {
                Toast.makeText(RecipeGenerationActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onSuccess("");
            }
        });
    }

    interface OnImageUploadListener {
        void onSuccess(String url);
    }

    private void saveRecipeToFirestore() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        Recipe recipe = new Recipe();
        recipe.setTitle(currentTitle);
        recipe.setDifficulty(currentDifficulty);
        recipe.setTimeMinutes(currentTime);
        recipe.setServings(currentServings);
        recipe.setImageUrl(currentImageUrl);
        recipe.setCreatedAt(System.currentTimeMillis());
        recipe.setTips(tipsList);

        List<Map<String, Object>> ingList = new ArrayList<>();
        for (JSONObject obj : ingredientsList) {
            Map<String, Object> map = new HashMap<>();
            map.put("nom", obj.optString("nom"));
            map.put("quantite", obj.optString("quantite"));
            ingList.add(map);
        }
        recipe.setIngredients(ingList);

        List<Map<String, Object>> stepList = new ArrayList<>();
        for (JSONObject obj : instructionsList) {
            Map<String, Object> map = new HashMap<>();
            map.put("etape_index", obj.optInt("etape_index"));
            map.put("description", obj.optString("description"));

            JSONArray ingUsed = obj.optJSONArray("ingredients_used");
            if (ingUsed != null) {
                List<String> list = new ArrayList<>();
                for(int i=0; i<ingUsed.length(); i++) list.add(ingUsed.optString(i));
                map.put("ingredients_used", list);
            }

            JSONArray eqUsed = obj.optJSONArray("equipment_used");
            if (eqUsed != null) {
                List<String> list = new ArrayList<>();
                for(int i=0; i<eqUsed.length(); i++) list.add(eqUsed.optString(i));
                map.put("equipment_used", list);
            }

            JSONObject timer = obj.optJSONObject("timer");
            if (timer != null) {
                Map<String, Object> timerMap = new HashMap<>();
                timerMap.put("active", timer.optBoolean("active"));
                timerMap.put("duree_secondes", timer.optLong("duree_secondes"));
                map.put("timer", timerMap);
            }

            stepList.add(map);
        }
        recipe.setSteps(stepList);

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .collection("recipes")
                .add(recipe)
                .addOnSuccessListener(documentReference -> {
                    currentRecipeId = documentReference.getId();
                    checkIfFavorite();
                });
    }

    private void checkIfFavorite() {
        if (currentRecipeId == null) return;
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .collection("favorites").document(currentRecipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isFavorite = documentSnapshot.exists();
                    updateFavoriteIcon();
                });
    }

    private void toggleFavorite() {
        if (currentRecipeId == null) {
            Toast.makeText(this, "Veuillez attendre la fin de la sauvegarde...", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        if (isFavorite) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .collection("favorites").document(currentRecipeId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = false;
                        updateFavoriteIcon();
                        Toast.makeText(this, "Retiré des favoris", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Recipe recipe = new Recipe();
            recipe.setId(currentRecipeId);
            recipe.setTitle(currentTitle);
            recipe.setDifficulty(currentDifficulty);
            recipe.setTimeMinutes(currentTime);
            recipe.setServings(currentServings);
            recipe.setImageUrl(currentImageUrl);
            recipe.setCreatedAt(System.currentTimeMillis());
            recipe.setTips(tipsList);

            List<Map<String, Object>> ingList = new ArrayList<>();
            for (JSONObject obj : ingredientsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("nom", obj.optString("nom"));
                map.put("quantite", obj.optString("quantite"));
                ingList.add(map);
            }
            recipe.setIngredients(ingList);

            List<Map<String, Object>> stepList = new ArrayList<>();
            for (JSONObject obj : instructionsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("etape_index", obj.optInt("etape_index"));
                map.put("description", obj.optString("description"));

                JSONArray ingUsed = obj.optJSONArray("ingredients_used");
                if (ingUsed != null) {
                    List<String> list = new ArrayList<>();
                    for(int i=0; i<ingUsed.length(); i++) list.add(ingUsed.optString(i));
                    map.put("ingredients_used", list);
                }

                JSONArray eqUsed = obj.optJSONArray("equipment_used");
                if (eqUsed != null) {
                    List<String> list = new ArrayList<>();
                    for(int i=0; i<eqUsed.length(); i++) list.add(eqUsed.optString(i));
                    map.put("equipment_used", list);
                }

                JSONObject timer = obj.optJSONObject("timer");
                if (timer != null) {
                    Map<String, Object> timerMap = new HashMap<>();
                    timerMap.put("active", timer.optBoolean("active"));
                    timerMap.put("duree_secondes", timer.optLong("duree_secondes"));
                    map.put("timer", timerMap);
                }

                stepList.add(map);
            }
            recipe.setSteps(stepList);

            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .collection("favorites").document(currentRecipeId)
                    .set(recipe)
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = true;
                        updateFavoriteIcon();
                        Toast.makeText(this, "Ajouté aux favoris", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite);
            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.primary_orange));
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite);
            btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.dark_text));
        }
    }

    private void handleError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
}