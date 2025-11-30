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
import androidx.core.content.ContextCompat;
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
        titleView.setText(R.string.welcome_back);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, 60, 0, 20);
        contentLayout.addView(titleView);

        TextView subtitleView = new TextView(this);
        subtitleView.setText(R.string.login_subtitle);
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        subtitleView.setTextColor(Color.GRAY);
        subtitleView.setGravity(Gravity.CENTER);
        subtitleView.setPadding(0, 10, 0, 80);
        contentLayout.addView(subtitleView);

        EditText emailInput = createStyledEditText(getString(R.string.email_hint), InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        contentLayout.addView(emailInput);

        addVerticalSpace(contentLayout, 40);

        EditText passInput = createStyledEditText(getString(R.string.password_hint), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        contentLayout.addView(passInput);

        TextView forgotPass = new TextView(this);
        forgotPass.setText(R.string.forgot_password);
        forgotPass.setTextColor(ContextCompat.getColor(this, R.color.primary_orange));
        forgotPass.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        forgotPass.setGravity(Gravity.END);
        LinearLayout.LayoutParams fpParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fpParams.setMargins(0, 20, 0, 60);
        forgotPass.setLayoutParams(fpParams);
        contentLayout.addView(forgotPass);

        Button loginBtn = new Button(this);
        loginBtn.setText(R.string.login_button);
        loginBtn.setTextColor(Color.WHITE);
        loginBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        loginBtn.setTypeface(null, Typeface.BOLD);
        GradientDrawable btnBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {ContextCompat.getColor(this, R.color.primary_orange), ContextCompat.getColor(this, R.color.gradient_end_orange)});
        btnBg.setCornerRadius(30f);
        loginBtn.setBackground(btnBg);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        loginBtn.setLayoutParams(btnParams);
        loginBtn.setElevation(10f);
        contentLayout.addView(loginBtn);

        LinearLayout footerLayout = new LinearLayout(this);
        footerLayout.setOrientation(LinearLayout.HORIZONTAL);
        footerLayout.setGravity(Gravity.CENTER);
        footerLayout.setPadding(0, 60, 0, 0);

        TextView noAccountText = new TextView(this);
        noAccountText.setText(R.string.no_account);
        noAccountText.setTextColor(Color.GRAY);
        footerLayout.addView(noAccountText);

        TextView registerLink = new TextView(this);
        registerLink.setText(R.string.register_link);
        registerLink.setTextColor(ContextCompat.getColor(this, R.color.primary_orange));
        registerLink.setTypeface(null, Typeface.BOLD);
        footerLayout.addView(registerLink);
        contentLayout.addView(footerLayout);
        setContentView(scrollView);

        ViewCompat.setOnApplyWindowInsetsListener(contentLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(80, systemBars.top + 50, 80, systemBars.bottom + 50);
            return insets;
        });

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passInput.getText().toString().trim();
            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(LoginActivity.this, R.string.fill_fields_error, Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    String errorMessage = getString(R.string.error_prefix) + "Inconnue";

                    if (task.getException() != null) {
                        errorMessage = getString(R.string.error_prefix) + " " + task.getException().getMessage();
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });

        registerLink.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }


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