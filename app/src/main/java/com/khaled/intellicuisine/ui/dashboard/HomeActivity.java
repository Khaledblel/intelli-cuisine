package com.khaled.intellicuisine.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.khaled.intellicuisine.R;
import com.khaled.intellicuisine.ui.auth.LoginActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // --- 1. GESTION DE L'AFFICHAGE (EdgeToEdge) ---
        // Ce bloc permet d'éviter que le contenu ne soit caché par les barres système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- 2. LOGIQUE DE DÉCONNEXION ---
        Button btnLogout = findViewById(R.id.btnLogout);

        // Si btnLogout est null, vérifiez que l'ID dans le XML est bien @+id/btnLogout
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                // A. Déconnecter l'utilisateur de Firebase
                FirebaseAuth.getInstance().signOut();

                // B. Retourner à l'écran de connexion
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);

                // C. Nettoyer la pile d'activités
                // (Empêche l'utilisateur de revenir sur Home en faisant "Retour")
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);
                finish(); // Ferme proprement l'activité actuelle
            });
        }
    }
}