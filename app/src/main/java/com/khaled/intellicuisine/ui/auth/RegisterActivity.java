package com.khaled.intellicuisine.ui.auth;

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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.khaled.intellicuisine.ui.dashboard.HomeActivity;
import com.khaled.intellicuisine.R;

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
        logoView.setImageResource(R.drawable.ic_logo_intellicuisine);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(200, 200);
        logoParams.setMargins(0, 10, 0, 10);
        logoView.setLayoutParams(logoParams);
        contentLayout.addView(logoView);

        TextView appNameView = new TextView(this);
        appNameView.setText(R.string.app_name);
        appNameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        appNameView.setTypeface(null, Typeface.BOLD);
        appNameView.setTextColor(ContextCompat.getColor(this, R.color.primary_orange));
        appNameView.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams appNameParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        appNameParams.setMargins(0, 0, 0, 50);
        appNameView.setLayoutParams(appNameParams);

        contentLayout.addView(appNameView);

        TextView titleView = new TextView(this);
        titleView.setText(R.string.create_account_title);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, 60, 0, 20);
        contentLayout.addView(titleView);

        TextView subtitleView = new TextView(this);
        subtitleView.setText(R.string.register_subtitle);
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        subtitleView.setTextColor(Color.GRAY);
        subtitleView.setGravity(Gravity.CENTER);
        subtitleView.setPadding(0, 0, 0, 80);
        contentLayout.addView(subtitleView);

        EditText nameInput = createStyledEditText(getString(R.string.name_hint), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        contentLayout.addView(nameInput);

        addVerticalSpace(contentLayout, 40);

        EditText emailInput = createStyledEditText(getString(R.string.email_address_hint), InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        contentLayout.addView(emailInput);

        addVerticalSpace(contentLayout, 40);

        EditText passInput = createStyledEditText(getString(R.string.password_hint), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        contentLayout.addView(passInput);

        addVerticalSpace(contentLayout, 40);

        EditText confirmPassInput = createStyledEditText(getString(R.string.confirm_password_hint), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        contentLayout.addView(confirmPassInput);

        addVerticalSpace(contentLayout, 80);

        Button registerBtn = new Button(this);
        registerBtn.setText(R.string.register_link);
        registerBtn.setTextColor(Color.WHITE);
        registerBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        registerBtn.setTypeface(null, Typeface.BOLD);

        GradientDrawable btnBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {ContextCompat.getColor(this, R.color.primary_orange), ContextCompat.getColor(this, R.color.gradient_end_orange)});
        btnBg.setCornerRadius(30f);
        registerBtn.setBackground(btnBg);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        registerBtn.setLayoutParams(btnParams);
        registerBtn.setElevation(10f);
        contentLayout.addView(registerBtn);

        LinearLayout footerLayout = new LinearLayout(this);
        footerLayout.setOrientation(LinearLayout.HORIZONTAL);
        footerLayout.setGravity(Gravity.CENTER);
        footerLayout.setPadding(0, 60, 0, 0);

        TextView hasAccountText = new TextView(this);
        hasAccountText.setText(R.string.already_member);
        hasAccountText.setTextColor(Color.GRAY);
        footerLayout.addView(hasAccountText);

        TextView loginLink = new TextView(this);
        loginLink.setText(R.string.login_button);
        loginLink.setTextColor(ContextCompat.getColor(this, R.color.primary_orange));
        loginLink.setTypeface(null, Typeface.BOLD);
        footerLayout.addView(loginLink);

        contentLayout.addView(footerLayout);

        setContentView(scrollView);

        ViewCompat.setOnApplyWindowInsetsListener(contentLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(80, systemBars.top + 50, 80, systemBars.bottom + 50);
            return insets;
        });

        registerBtn.setOnClickListener(v -> {

            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String pass = passInput.getText().toString().trim();
            String confirm = confirmPassInput.getText().toString().trim();


            if(name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, R.string.fields_required_error, Toast.LENGTH_SHORT).show();
                return;
            }
            if(!pass.equals(confirm)) {
                Toast.makeText(this, R.string.passwords_mismatch_error, Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> updateUI(user));
                    }
                } else {
                    String errorMsg = getString(R.string.error_prefix) + "Inconnue";
                    if (task.getException() != null) {
                        errorMsg = getString(R.string.error_prefix) + " " + task.getException().getMessage();
                    }
                    Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

    // Méthodes Helper
    private EditText createStyledEditText(String hint, int inputType) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setInputType(inputType);
        editText.setBackground(createRoundedDrawable(ContextCompat.getColor(this, R.color.input_background), 30f));
        editText.setPadding(50, 40, 50, 40);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        editText.setHintTextColor(ContextCompat.getColor(this, R.color.hint_text));
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