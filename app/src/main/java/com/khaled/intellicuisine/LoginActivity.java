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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        mAuth = FirebaseAuth.getInstance();

        // --- 1. CONFIGURATION DU CONTENEUR PRINCIPAL (SCROLLVIEW) ---
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.WHITE);

        // Layout vertical à l'intérieur du ScrollView
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        contentLayout.setPadding(80, 100, 80, 100); // Grosses marges sur les côtés
        scrollView.addView(contentLayout);

        // --- 2. LOGO ---
        ImageView logoView = new ImageView(this);
        // Assurez-vous d'avoir votre logo dans res/drawable/ic_logo_intellicuisine
        // Si vous n'avez pas encore l'image, commentez la ligne setImageResource
        logoView.setImageResource(R.drawable.ic_logo_intellicuisine); // Remplacer par R.drawable.ic_logo_intellicuisine
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(300, 300);
        logoParams.setMargins(0, 50, 0, 30);
        logoView.setLayoutParams(logoParams);
        contentLayout.addView(logoView);

        // --- 3. TEXTES D'ACCUEIL ---
        TextView titleView = new TextView(this);
        titleView.setText("Bon retour !"); // "Welcome Back!"
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setTextColor(Color.parseColor("#1F1F1F"));
        titleView.setGravity(Gravity.CENTER);
        contentLayout.addView(titleView);

        TextView subtitleView = new TextView(this);
        subtitleView.setText("Connectez-vous pour continuer");
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        subtitleView.setTextColor(Color.GRAY);
        subtitleView.setGravity(Gravity.CENTER);
        subtitleView.setPadding(0, 10, 0, 80); // Espace avant les inputs
        contentLayout.addView(subtitleView);

        // --- 4. CHAMPS DE SAISIE (STYLE MODERNE) ---
        // Email
        EditText emailInput = createStyledEditText("Email", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        contentLayout.addView(emailInput);

        // Espace entre les champs
        addVerticalSpace(contentLayout, 40);

        // Password
        EditText passInput = createStyledEditText("Mot de passe", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        contentLayout.addView(passInput);

        // Forgot Password (Optionnel, juste visuel pour l'instant)
        TextView forgotPass = new TextView(this);
        forgotPass.setText("Mot de passe oublié ?");
        forgotPass.setTextColor(Color.parseColor("#FF9800")); // Orange
        forgotPass.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        forgotPass.setGravity(Gravity.END);
        LinearLayout.LayoutParams fpParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fpParams.setMargins(0, 20, 0, 60);
        forgotPass.setLayoutParams(fpParams);
        contentLayout.addView(forgotPass);

        // --- 5. BOUTON LOGIN (GRADIENT) ---
        Button loginBtn = new Button(this);
        loginBtn.setText("Se connecter");
        loginBtn.setTextColor(Color.WHITE);
        loginBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        loginBtn.setTypeface(null, Typeface.BOLD);
        // Création du background gradient (Orange vers Rouge)
        GradientDrawable btnBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {Color.parseColor("#FF9800"), Color.parseColor("#FF5722")});
        btnBg.setCornerRadius(30f); // Coins très ronds
        loginBtn.setBackground(btnBg);
        // Taille bouton
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        loginBtn.setLayoutParams(btnParams);
        // Ombre (Elevation)
        loginBtn.setElevation(10f);
        contentLayout.addView(loginBtn);

        // --- 6. FOOTER ---
        LinearLayout footerLayout = new LinearLayout(this);
        footerLayout.setOrientation(LinearLayout.HORIZONTAL);
        footerLayout.setGravity(Gravity.CENTER);
        footerLayout.setPadding(0, 60, 0, 0);

        TextView noAccountText = new TextView(this);
        noAccountText.setText("Pas encore de compte ? ");
        noAccountText.setTextColor(Color.GRAY);
        footerLayout.addView(noAccountText);

        TextView registerLink = new TextView(this);
        registerLink.setText("S'inscrire");
        registerLink.setTextColor(Color.parseColor("#FF9800"));
        registerLink.setTypeface(null, Typeface.BOLD);
        footerLayout.addView(registerLink);

        contentLayout.addView(footerLayout);

        // Définir la vue
        setContentView(scrollView);

        // --- GESTION DES WINDOW INSETS ---
        ViewCompat.setOnApplyWindowInsetsListener(contentLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(80, systemBars.top + 50, 80, systemBars.bottom + 50);
            return insets;
        });

        // --- LOGIQUE (Listeners) ---
        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passInput.getText().toString().trim();
            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(LoginActivity.this, "Remplissez les champs", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "Erreur: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    // --- HELPER METHODS POUR LE STYLE ---

    // Crée un EditText avec fond gris clair et coins ronds
    private EditText createStyledEditText(String hint, int inputType) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setInputType(inputType);
        editText.setBackground(createRoundedDrawable(Color.parseColor("#F5F6FA"), 30f)); // Fond gris très clair
        editText.setPadding(50, 40, 50, 40); // Padding interne confortable
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        editText.setHintTextColor(Color.parseColor("#A0A0A0"));
        return editText;
    }

    // Crée un drawable rectangle arrondi
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