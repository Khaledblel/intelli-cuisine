package com.khaled.intellicuisine.ui.auth;

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
import com.khaled.intellicuisine.R;

public class ForgotPasswordActivity extends AppCompatActivity {

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

        // Title
        TextView titleView = new TextView(this);
        titleView.setText(R.string.reset_password_title);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, 60, 0, 20);
        contentLayout.addView(titleView);

        // Subtitle
        TextView subtitleView = new TextView(this);
        subtitleView.setText(R.string.reset_password_subtitle);
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        subtitleView.setTextColor(Color.GRAY);
        subtitleView.setGravity(Gravity.CENTER);
        subtitleView.setPadding(0, 10, 0, 80);
        contentLayout.addView(subtitleView);

        // Email Input
        EditText emailInput = createStyledEditText(getString(R.string.email_hint), InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        contentLayout.addView(emailInput);

        addVerticalSpace(contentLayout, 60);

        // Reset Button
        Button resetBtn = new Button(this);
        resetBtn.setText(R.string.send_reset_link);
        resetBtn.setTextColor(Color.WHITE);
        resetBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        resetBtn.setTypeface(null, Typeface.BOLD);
        GradientDrawable btnBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {ContextCompat.getColor(this, R.color.primary_orange), ContextCompat.getColor(this, R.color.gradient_end_orange)});
        btnBg.setCornerRadius(30f);
        resetBtn.setBackground(btnBg);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        resetBtn.setLayoutParams(btnParams);
        resetBtn.setElevation(10f);
        contentLayout.addView(resetBtn);

        setContentView(scrollView);

        ViewCompat.setOnApplyWindowInsetsListener(contentLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(80, systemBars.top + 50, 80, systemBars.bottom + 50);
            return insets;
        });

        resetBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, R.string.fill_fields_error, Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, R.string.reset_email_sent, Toast.LENGTH_LONG).show();
                            finish(); // Go back to login
                        } else {
                            String error = getString(R.string.reset_email_error);
                            if (task.getException() != null) {
                                error += task.getException().getMessage();
                            }
                            Toast.makeText(ForgotPasswordActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    });
        });
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