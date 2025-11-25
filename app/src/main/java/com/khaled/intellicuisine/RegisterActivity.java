package com.khaled.intellicuisine;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mAuth = FirebaseAuth.getInstance();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.WHITE);

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        contentLayout.setPadding(80, 100, 80, 100);
        scrollView.addView(contentLayout);

        ImageView logoView = new ImageView(this);
        logoView.setImageResource(R.drawable.ic_logo_intellicuisine); // Votre image
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(200, 200); // Un peu plus petit que sur le Login
        logoParams.setMargins(0, 10, 0, 10);
        logoView.setLayoutParams(logoParams);
        contentLayout.addView(logoView);

        TextView appNameView = new TextView(this);
        appNameView.setText("IntelliCuisine");
        appNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        appNameView.setTypeface(null, Typeface.BOLD);
        appNameView.setTextColor(Color.parseColor("#FF9800"));
        appNameView.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams appNameParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        appNameParams.setMargins(0, 0, 0, 50); // Espace avant le titre suivant
        appNameView.setLayoutParams(appNameParams);

        contentLayout.addView(appNameView);

        // --- TITRES ---
        // On n'est pas obligé de remettre le logo ici, ou alors plus petit
        TextView titleView = new TextView(this);
        titleView.setText("Créer un compte");
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setTextColor(Color.parseColor("#1F1F1F"));
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, 60, 0, 20);
        contentLayout.addView(titleView);

        TextView subtitleView = new TextView(this);
        subtitleView.setText("Rejoignez IntelliCuisine aujourd'hui");
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        subtitleView.setTextColor(Color.GRAY);
        subtitleView.setGravity(Gravity.CENTER);
        subtitleView.setPadding(0, 0, 0, 80);
        contentLayout.addView(subtitleView);

        // --- INPUTS ---
        EditText emailInput = createStyledEditText("Adresse Email", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        contentLayout.addView(emailInput);

        addVerticalSpace(contentLayout, 40);

        EditText passInput = createStyledEditText("Mot de passe", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        contentLayout.addView(passInput);

        addVerticalSpace(contentLayout, 40);

        // Confirmation (Optionnel mais recommandé)
        EditText confirmPassInput = createStyledEditText("Confirmer mot de passe", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        contentLayout.addView(confirmPassInput);

        addVerticalSpace(contentLayout, 80);

        // --- BOUTON ---
        Button registerBtn = new Button(this);
        registerBtn.setText("S'inscrire");
        registerBtn.setTextColor(Color.WHITE);
        registerBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        registerBtn.setTypeface(null, Typeface.BOLD);

        GradientDrawable btnBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {Color.parseColor("#FF9800"), Color.parseColor("#FF5722")});
        btnBg.setCornerRadius(30f);
        registerBtn.setBackground(btnBg);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        registerBtn.setLayoutParams(btnParams);
        registerBtn.setElevation(10f);
        contentLayout.addView(registerBtn);

        // --- FOOTER ---
        LinearLayout footerLayout = new LinearLayout(this);
        footerLayout.setOrientation(LinearLayout.HORIZONTAL);
        footerLayout.setGravity(Gravity.CENTER);
        footerLayout.setPadding(0, 60, 0, 0);

        TextView hasAccountText = new TextView(this);
        hasAccountText.setText("Déjà membre ? ");
        hasAccountText.setTextColor(Color.GRAY);
        footerLayout.addView(hasAccountText);

        TextView loginLink = new TextView(this);
        loginLink.setText("Se connecter");
        loginLink.setTextColor(Color.parseColor("#FF9800"));
        loginLink.setTypeface(null, Typeface.BOLD);
        footerLayout.addView(loginLink);

        contentLayout.addView(footerLayout);

        setContentView(scrollView);

        ViewCompat.setOnApplyWindowInsetsListener(contentLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(80, systemBars.top + 50, 80, systemBars.bottom + 50);
            return insets;
        });

        // --- LOGIQUE ---
        registerBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String pass = passInput.getText().toString().trim();
            String confirm = confirmPassInput.getText().toString().trim();

            if(email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Champs requis", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!pass.equals(confirm)) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    updateUI(mAuth.getCurrentUser());
                } else {
                    Toast.makeText(this, "Erreur: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        loginLink.setOnClickListener(v -> finish());
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    // Méthodes Helper copiées (Ou à mettre dans une classe utilitaire Utils.java)
    private EditText createStyledEditText(String hint, int inputType) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setInputType(inputType);
        editText.setBackground(createRoundedDrawable(Color.parseColor("#F5F6FA"), 30f));
        editText.setPadding(50, 40, 50, 40);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        editText.setHintTextColor(Color.parseColor("#A0A0A0"));
        return editText;
    }

    private GradientDrawable createRoundedDrawable(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(radius);
        shape.setColor(color);
        return shape;
    }

    private void addVerticalSpace(LinearLayout layout, int height) {
        View space = new View(this);
        layout.addView(space, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
    }
}