package com.khaled.intellicuisine.ui.dashboard;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
import com.khaled.intellicuisine.services.TimerService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

public class CookingModeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private TextView tvStepCounter;
    private TextView tvRecipeTitleHeader;
    private View loadingView;
    private List<JSONObject> stepsList = new ArrayList<>();
    private String recipeTitle = "";
    private String recipeId;
    private Bitmap finalRecipeBitmap;
    private CookingStepAdapter adapter;

    private TimerService timerService;
    private boolean isBound = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
            timerService = binder.getService();
            isBound = true;
            updateTimerUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    private final BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TimerService.BROADCAST_TICK.equals(intent.getAction())) {
                updateTimerUI();
            } else if (TimerService.BROADCAST_FINISH.equals(intent.getAction())) {
                updateTimerUI();
                Toast.makeText(context, "Minuteur terminé !", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cooking_mode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkNotificationPermission();

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        viewPager = findViewById(R.id.viewPager);
        progressBar = findViewById(R.id.progressBar);
        tvStepCounter = findViewById(R.id.tvStepCounter);
        tvRecipeTitleHeader = findViewById(R.id.tvRecipeTitleHeader);
        loadingView = findViewById(R.id.loadingView);

        recipeTitle = getIntent().getStringExtra("RECIPE_TITLE");
        if (recipeTitle != null) {
            tvRecipeTitleHeader.setText(recipeTitle);
        }

        recipeId = getIntent().getStringExtra("RECIPE_ID");

        String imagePath = getIntent().getStringExtra("RECIPE_IMAGE_PATH");
        if (imagePath != null) {
            finalRecipeBitmap = BitmapFactory.decodeFile(imagePath);
        } else {
            String base64Image = getIntent().getStringExtra("RECIPE_IMAGE_BASE64");
            if (base64Image != null && !base64Image.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    finalRecipeBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String stepsJson = getIntent().getStringExtra("STEPS_DATA");
        if (stepsJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(stepsJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    stepsList.add(jsonArray.getJSONObject(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (stepsList.isEmpty()) {
            Toast.makeText(this, "Aucune étape trouvée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setMax(stepsList.size());
        updateProgress(0);

        adapter = new CookingStepAdapter(stepsList, new TimerActionListener() {
            @Override
            public void onNext() {
                int current = viewPager.getCurrentItem();
                if (current < stepsList.size() - 1) {
                    viewPager.setCurrentItem(current + 1, true);
                } else {
                    Toast.makeText(CookingModeActivity.this, "Bon appétit ! Recette terminée.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onStartTimer(long duration, int stepIndex) {
                if (isBound && timerService != null) {
                    Intent intent = new Intent(CookingModeActivity.this, TimerService.class);
                    startService(intent); // Ensure service is started
                    timerService.startTimer(duration, stepIndex, recipeTitle);
                }
            }

            @Override
            public void onPauseTimer() {
                if (isBound && timerService != null) {
                    timerService.pauseTimer();
                }
            }

            @Override
            public void onResetTimer() {
                if (isBound && timerService != null) {
                    timerService.stopTimer();
                }
            }
        });

        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateProgress(position);
            }
        });

        if (recipeId != null) {
            loadStepsFromFirestore();
        } else {
            startImageGeneration();
        }
    }

    private void loadStepsFromFirestore() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            startImageGeneration();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("recipes")
                .document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> steps = (List<Map<String, Object>>) documentSnapshot.get("steps");
                        if (steps != null && !steps.isEmpty()) {
                            stepsList.clear();
                            for (Map<String, Object> stepMap : steps) {
                                stepsList.add(new JSONObject(stepMap));
                            }
                            adapter.notifyDataSetChanged();
                            
                            startImageGeneration();
                        } else {
                            startImageGeneration();
                        }
                    } else {
                        startImageGeneration();
                    }
                })
                .addOnFailureListener(e -> startImageGeneration());
    }

    private void startImageGeneration() {
        loadingView.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.INVISIBLE);

        JSONObject step0 = stepsList.get(0);
        if (step0.has("image_base64") && !step0.optString("image_base64").isEmpty()) {
            String base64 = step0.optString("image_base64");
            Bitmap bmp = decodeBase64(base64);
            if (bmp != null) {
                adapter.setImage(0, bmp);
            }
            
            loadingView.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);

            for (int i = 1; i < stepsList.size(); i++) {
                checkAndGenerateStepImage(i);
            }
        } else {
            generateStepImage(0, () -> {
                loadingView.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);

                for (int i = 1; i < stepsList.size(); i++) {
                    checkAndGenerateStepImage(i);
                }
            });
        }
    }

    private void checkAndGenerateStepImage(int index) {
        JSONObject step = stepsList.get(index);
        if (step.has("image_base64") && !step.optString("image_base64").isEmpty()) {
            String base64 = step.optString("image_base64");
            Bitmap bmp = decodeBase64(base64);
            if (bmp != null) {
                adapter.setImage(index, bmp);
            }
        } else {
            generateStepImage(index, null);
        }
    }

    private Bitmap decodeBase64(String base64) {
        try {
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (Exception e) {
            return null;
        }
    }

    private void generateStepImage(int stepIndex, Runnable onComplete) {
        if (stepIndex >= stepsList.size()) return;

        JSONObject step = stepsList.get(stepIndex);
        String description = step.optString("description");
        
        String promptText = "Here is the final dish image for reference. Generate a realistic cooking step image for step " + (stepIndex + 1) + " of the recipe '" + recipeTitle + "': " + description + ". The visual style, ingredients, and lighting must be consistent with the final dish shown in the reference image.";

        GenerativeModel ai = FirebaseAI.getInstance(GenerativeBackend.googleAI()).generativeModel(
                "gemini-2.5-flash-image",
                new GenerationConfig.Builder()
                        .setResponseModalities(Arrays.asList(ResponseModality.TEXT, ResponseModality.IMAGE))
                        .build()
        );

        GenerativeModelFutures model = GenerativeModelFutures.from(ai);
        
        Content.Builder contentBuilder = new Content.Builder();
        if (finalRecipeBitmap != null) {
            contentBuilder.addImage(finalRecipeBitmap);
        }
        contentBuilder.addText(promptText);
        Content prompt = contentBuilder.build();

        Executor executor = ContextCompat.getMainExecutor(this);
        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                Bitmap generatedImage = null;
                if (result.getCandidates() != null && !result.getCandidates().isEmpty()) {
                    for (Part part : result.getCandidates().get(0).getContent().getParts()) {
                        if (part instanceof ImagePart) {
                            generatedImage = ((ImagePart) part).getImage();
                            break;
                        }
                    }
                }

                if (generatedImage != null) {
                    adapter.setImage(stepIndex, generatedImage);
                    saveStepImageToFirestore(stepIndex, generatedImage);
                }

                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                if (onComplete != null) onComplete.run();
            }
        }, executor);
    }

    private void saveStepImageToFirestore(int stepIndex, Bitmap bitmap) {
        if (recipeId == null || FirebaseAuth.getInstance().getCurrentUser() == null) return;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();
        String base64Image = Base64.encodeToString(data, Base64.DEFAULT);

        try {
            stepsList.get(stepIndex).put("image_base64", base64Image);
        } catch (Exception e) { e.printStackTrace(); }

        List<Map<String, Object>> updatedSteps = new ArrayList<>();
        for (JSONObject step : stepsList) {
            updatedSteps.add(stepJsonToMap(step));
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("recipes")
                .document(recipeId)
                .update("steps", updatedSteps);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("favorites")
                .document(recipeId)
                .update("steps", updatedSteps)
                .addOnFailureListener(e -> { });
    }

    private Map<String, Object> stepJsonToMap(JSONObject step) {
        Map<String, Object> map = new HashMap<>();
        map.put("etape_index", step.optInt("etape_index"));
        map.put("description", step.optString("description"));
        map.put("image_base64", step.optString("image_base64"));

        JSONArray ingArr = step.optJSONArray("ingredients_used");
        if (ingArr != null) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < ingArr.length(); i++) list.add(ingArr.optString(i));
            map.put("ingredients_used", list);
        }

        JSONArray eqArr = step.optJSONArray("equipment_used");
        if (eqArr != null) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < eqArr.length(); i++) list.add(eqArr.optString(i));
            map.put("equipment_used", list);
        }

        JSONObject timerObj = step.optJSONObject("timer");
        if (timerObj != null) {
            Map<String, Object> timerMap = new HashMap<>();
            timerMap.put("active", timerObj.optBoolean("active"));
            timerMap.put("duree_secondes", timerObj.optLong("duree_secondes"));
            map.put("timer", timerMap);
        }
        
        return map;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TimerService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(TimerService.BROADCAST_TICK);
        filter.addAction(TimerService.BROADCAST_FINISH);
        LocalBroadcastManager.getInstance(this).registerReceiver(timerReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerReceiver);
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void updateProgress(int position) {
        int currentStep = position + 1;
        progressBar.setProgress(currentStep);
        tvStepCounter.setText(currentStep + "/" + stepsList.size());
    }

    private void updateTimerUI() {
        if (!isBound || timerService == null) return;

        int activeStepIndex = timerService.getCurrentStepIndex();
        long timeLeft = timerService.getTimeLeft();
        boolean isRunning = timerService.isRunning();

        // Find the ViewHolder for the active step
        RecyclerView rv = (RecyclerView) viewPager.getChildAt(0);
        if (rv != null) {
            RecyclerView.ViewHolder vh = rv.findViewHolderForAdapterPosition(activeStepIndex);
            if (vh instanceof CookingStepAdapter.StepViewHolder) {
                ((CookingStepAdapter.StepViewHolder) vh).updateTimerState(timeLeft, isRunning);
            }
        }
    }

    interface TimerActionListener {
        void onNext();

        void onStartTimer(long duration, int stepIndex);

        void onPauseTimer();

        void onResetTimer();
    }

    private class CookingStepAdapter extends RecyclerView.Adapter<CookingStepAdapter.StepViewHolder> {

        private final List<JSONObject> steps;
        private final TimerActionListener listener;
        private final Map<Integer, Bitmap> stepImages = new HashMap<>();

        public CookingStepAdapter(List<JSONObject> steps, TimerActionListener listener) {
            this.steps = steps;
            this.listener = listener;
        }

        public void setImage(int position, Bitmap bitmap) {
            stepImages.put(position, bitmap);
            notifyItemChanged(position);
        }

        @NonNull
        @Override
        public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cooking_step, parent, false);
            return new StepViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
            JSONObject step = steps.get(position);
            holder.bind(step, position, listener);

            if (stepImages.containsKey(position)) {
                holder.imgStep.setImageBitmap(stepImages.get(position));
                holder.imgStep.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.imgStep.setImageTintList(null);
            } else {
                holder.imgStep.setImageResource(R.drawable.ic_inventory);
                holder.imgStep.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                holder.imgStep.setImageTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.hint_text));
            }

            // Check if this step has the running timer
            if (isBound && timerService != null && timerService.getCurrentStepIndex() == position) {
                holder.updateTimerState(timerService.getTimeLeft(), timerService.isRunning());
            } else {
                // Reset to default state for this step
                JSONObject timerObj = step.optJSONObject("timer");
                if (timerObj != null) {
                    long duration = timerObj.optLong("duree_secondes", 0) * 1000;
                    holder.updateTimerState(duration, false);
                }
            }
        }

        @Override
        public int getItemCount() {
            return steps.size();
        }

        class StepViewHolder extends RecyclerView.ViewHolder {
            TextView tvStepNumber, tvStepTitle, tvDescription, tvTimer;
            ImageView imgStep;
            ChipGroup chipGroupIngredients, chipGroupEquipment;
            LinearLayout ingredientsSection, equipmentSection, timerContainer;
            Button btnNextStep, btnTimerAction, btnTimerReset;

            long originalDurationInMillis = 0;

            public StepViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
                tvStepTitle = itemView.findViewById(R.id.tvStepTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                imgStep = itemView.findViewById(R.id.imgStep);
                chipGroupIngredients = itemView.findViewById(R.id.chipGroupIngredients);
                chipGroupEquipment = itemView.findViewById(R.id.chipGroupEquipment);
                ingredientsSection = itemView.findViewById(R.id.ingredientsSection);
                equipmentSection = itemView.findViewById(R.id.equipmentSection);
                timerContainer = itemView.findViewById(R.id.timerContainer);
                tvTimer = itemView.findViewById(R.id.tvTimer);
                btnTimerAction = itemView.findViewById(R.id.btnTimerAction);
                btnTimerReset = itemView.findViewById(R.id.btnTimerReset);
                btnNextStep = itemView.findViewById(R.id.btnNextStep);
            }

            public void bind(JSONObject step, int position, TimerActionListener listener) {
                tvStepNumber.setText(String.valueOf(position + 1));
                tvStepTitle.setText("Étape " + (position + 1));
                tvDescription.setText(step.optString("description"));

                JSONArray ingredients = step.optJSONArray("ingredients_used");
                chipGroupIngredients.removeAllViews();
                if (ingredients != null && ingredients.length() > 0) {
                    ingredientsSection.setVisibility(View.VISIBLE);
                    for (int i = 0; i < ingredients.length(); i++) {
                        addChip(chipGroupIngredients, ingredients.optString(i));
                    }
                } else {
                    ingredientsSection.setVisibility(View.GONE);
                }

                JSONArray equipment = step.optJSONArray("equipment_used");
                chipGroupEquipment.removeAllViews();
                if (equipment != null && equipment.length() > 0) {
                    equipmentSection.setVisibility(View.VISIBLE);
                    for (int i = 0; i < equipment.length(); i++) {
                        addChip(chipGroupEquipment, equipment.optString(i));
                    }
                } else {
                    equipmentSection.setVisibility(View.GONE);
                }

                JSONObject timerObj = step.optJSONObject("timer");
                if (timerObj != null && timerObj.optBoolean("active", false)) {
                    timerContainer.setVisibility(View.VISIBLE);
                    long durationSec = timerObj.optLong("duree_secondes", 0);
                    originalDurationInMillis = durationSec * 1000;

                    // Default display
                    updateTimerText(originalDurationInMillis);
                    btnTimerAction.setText("Démarrer");

                    btnTimerAction.setOnClickListener(v -> {
                        if (btnTimerAction.getText().toString().equals("Pause")) {
                            listener.onPauseTimer();
                        } else {
                            if (btnTimerAction.getText().toString().equals("Reprendre")) {
                                listener.onStartTimer(originalDurationInMillis, position);
                            } else {
                                listener.onStartTimer(originalDurationInMillis, position);
                            }
                        }
                    });

                    btnTimerReset.setOnClickListener(v -> listener.onResetTimer());
                } else {
                    timerContainer.setVisibility(View.GONE);
                }

                btnNextStep.setText(position == steps.size() - 1 ? "Terminer la recette" : "Étape suivante");
                btnNextStep.setOnClickListener(v -> listener.onNext());
            }

            public void updateTimerState(long timeLeft, boolean isRunning) {
                updateTimerText(timeLeft);
                if (timeLeft == 0 && !isRunning) {
                     btnTimerAction.setText("Terminé");
                     btnTimerAction.setEnabled(false);
                } else if (isRunning) {
                    btnTimerAction.setText("Pause");
                    btnTimerAction.setEnabled(true);
                } else {
                    if (timeLeft < originalDurationInMillis && timeLeft > 0) {
                        btnTimerAction.setText("Reprendre");
                    } else {
                        btnTimerAction.setText("Démarrer");
                    }
                    btnTimerAction.setEnabled(true);
                }
            }

            private void updateTimerText(long millis) {
                int minutes = (int) (millis / 1000) / 60;
                int seconds = (int) (millis / 1000) % 60;
                String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                tvTimer.setText(timeFormatted);
            }

            private void addChip(ChipGroup group, String text) {
                Chip chip = new Chip(itemView.getContext());
                chip.setText(text);
                chip.setChipBackgroundColorResource(R.color.input_background);
                chip.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.dark_text));
                group.addView(chip);
            }
        }
    }
}