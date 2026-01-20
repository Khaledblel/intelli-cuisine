package com.khaled.intellicuisine.ui.dashboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.khaled.intellicuisine.ui.auth.LoginActivity;

import com.khaled.intellicuisine.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    TextView tvName, tvEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        View cardName = view.findViewById(R.id.cardName);
        View cardEmail = view.findViewById(R.id.cardEmail);
        View cardChangePassword = view.findViewById(R.id.cardChangePassword);
        View cardDeleteAccount = view.findViewById(R.id.cardDeleteAccount);

        updateUI();

        if (cardName != null) {
            cardName.setOnClickListener(v -> showEditNameDialog(tvName));
        }

        if (cardEmail != null) {
            cardEmail.setOnClickListener(v -> showEditEmailDialog(tvEmail));
        }

        if (cardChangePassword != null) {
            cardChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        if (cardDeleteAccount != null) {
            cardDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        }

        if (btnLogout != null) {
            styleButton(btnLogout);
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        }

        return view;
    }

    private void updateUI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.getEmail() != null) {
                tvEmail.setText(user.getEmail());
            }
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                tvName.setText(user.getDisplayName());
            } else {
                tvName.setText(R.string.placeholder_name);
            }
        }
    }

    private void showEditNameDialog(TextView tvNameView) {
        if (getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        try {
            layout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_bottom_sheet));
        } catch (Exception ex) {
            layout.setBackground(createRoundedDrawable(ContextCompat.getColor(getContext(), R.color.white), 24f));
        }

        TextView title = new TextView(getContext());
        title.setText("Modifier le nom");
        title.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));
        title.setTextSize(20); // match mot de passe
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL); // left align
        title.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, padding / 2); // bottom margin
        title.setLayoutParams(titleParams);
        layout.addView(title);

        EditText input = new EditText(getContext());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null) {
            input.setText(user.getDisplayName());
        }

        input.setBackground(createRoundedDrawable(ContextCompat.getColor(getContext(), R.color.input_background), 30f));
        input.setPadding(50, 40, 50, 40);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        input.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputParams.setMargins(0, 0, 0, padding);
        input.setLayoutParams(inputParams);
        layout.addView(input);

        Button btnSave = new Button(getContext());
        btnSave.setText(R.string.action_done);
        btnSave.setTextColor(Color.WHITE);
        btnSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        btnSave.setTypeface(null, Typeface.BOLD);

        styleButton(btnSave);

        btnSave.setOnClickListener(v -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                input.setError(getString(R.string.fields_required_error));
                return;
            }

            if (user != null) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                tvNameView.setText(newName);
                                dialog.dismiss();
                                Toast.makeText(getContext(), R.string.success_name_updated, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        layout.addView(btnSave);

        dialog.setContentView(layout);
        dialog.show();
    }

    private void showEditEmailDialog(TextView tvEmailView) {
        if (getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        try {
            layout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_bottom_sheet));
        } catch (Exception ex) {
            layout.setBackground(createRoundedDrawable(ContextCompat.getColor(getContext(), R.color.white), 24f));
        }

        TextView title = new TextView(getContext());
        title.setText("Modifier l'adresse mail");
        title.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));
        title.setTextSize(20); // match mot de passe
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL); // left align
        title.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, padding / 2); // bottom margin
        title.setLayoutParams(titleParams);
        layout.addView(title);

        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            input.setText(user.getEmail());
        }

        input.setBackground(createRoundedDrawable(ContextCompat.getColor(getContext(), R.color.input_background), 30f));
        input.setPadding(50, 40, 50, 40);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        input.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputParams.setMargins(0, 0, 0, padding);
        input.setLayoutParams(inputParams);
        layout.addView(input);

        Button btnSave = new Button(getContext());
        btnSave.setText(R.string.action_done);
        btnSave.setTextColor(Color.WHITE);
        btnSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        btnSave.setTypeface(null, Typeface.BOLD);

        styleButton(btnSave);

        btnSave.setOnClickListener(v -> {
            String newEmail = input.getText().toString().trim();
            if (newEmail.isEmpty()) {
                input.setError(getString(R.string.fields_required_error));
                return;
            }

            if (user != null) {
                String currentEmail = user.getEmail();
                if (currentEmail != null && currentEmail.equals(newEmail)) {
                    dialog.dismiss();
                    return;
                }

                user.verifyBeforeUpdateEmail(newEmail)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), R.string.success_email_verification_sent, Toast.LENGTH_LONG).show();
                                dialog.dismiss();

                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            } else {
                                if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                    Toast.makeText(getContext(), R.string.error_login_required, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        layout.addView(btnSave);

        dialog.setContentView(layout);
        dialog.show();
    }

    private void showChangePasswordDialog() {
        if (getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        try {
            layout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_bottom_sheet));
        } catch (Exception ex) {
            layout.setBackground(createRoundedDrawable(ContextCompat.getColor(getContext(), R.color.white), 24f));
        }

        TextView title = new TextView(getContext());
        title.setText(R.string.action_change_password);
        title.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, padding);
        layout.addView(title);

        EditText currentPassInput = new EditText(getContext());
        currentPassInput.setHint(R.string.current_password_hint);
        currentPassInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        currentPassInput.setBackground(createRoundedDrawable(ContextCompat.getColor(getContext(), R.color.input_background), 30f));
        currentPassInput.setPadding(50, 40, 50, 40);
        currentPassInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        currentPassInput.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, padding / 2);
        currentPassInput.setLayoutParams(params);
        layout.addView(currentPassInput);

        EditText newPassInput = new EditText(getContext());
        newPassInput.setHint(R.string.new_password_hint);
        newPassInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPassInput.setBackground(createRoundedDrawable(ContextCompat.getColor(getContext(), R.color.input_background), 30f));
        newPassInput.setPadding(50, 40, 50, 40);
        newPassInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        newPassInput.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));
        newPassInput.setLayoutParams(params);
        layout.addView(newPassInput);

        EditText confirmPassInput = new EditText(getContext());
        confirmPassInput.setHint(R.string.confirm_new_password_hint);
        confirmPassInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPassInput.setBackground(createRoundedDrawable(ContextCompat.getColor(getContext(), R.color.input_background), 30f));
        confirmPassInput.setPadding(50, 40, 50, 40);
        confirmPassInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        confirmPassInput.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));
        
        LinearLayout.LayoutParams lastParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lastParams.setMargins(0, 0, 0, padding);
        confirmPassInput.setLayoutParams(lastParams);
        layout.addView(confirmPassInput);

        Button btnSave = new Button(getContext());
        btnSave.setText(R.string.action_done);
        btnSave.setTextColor(Color.WHITE);
        btnSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        btnSave.setTypeface(null, Typeface.BOLD);
        styleButton(btnSave);

        btnSave.setOnClickListener(v -> {
            String currentPass = currentPassInput.getText().toString().trim();
            String newPass = newPassInput.getText().toString().trim();
            String confirmPass = confirmPassInput.getText().toString().trim();

            boolean hasError = false;

            if (currentPass.isEmpty()) {
                currentPassInput.setError(getString(R.string.fields_required_error));
                hasError = true;
            }

            if (newPass.isEmpty()) {
                newPassInput.setError(getString(R.string.fields_required_error));
                hasError = true;
            }

            if (confirmPass.isEmpty()) {
                confirmPassInput.setError(getString(R.string.fields_required_error));
                hasError = true;
            }

            if (hasError) {
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(getContext(), R.string.error_weak_password, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(getContext(), R.string.passwords_mismatch_error, Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getEmail() != null) {
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);
                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Toast.makeText(getContext(), R.string.success_password_updated, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            } else {
                                Toast.makeText(getContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), R.string.error_reauth_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        layout.addView(btnSave);

        dialog.setContentView(layout);
        dialog.show();
    }

    private void showDeleteAccountDialog() {
        if (getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);
        try {
            layout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_bottom_sheet));
        } catch (Exception ex) {
            layout.setBackground(createRoundedDrawable(ContextCompat.getColor(getContext(), R.color.white), 24f));
        }

        TextView title = new TextView(getContext());
        title.setText(R.string.action_delete_account);
        title.setTextColor(Color.RED);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, padding / 2);
        layout.addView(title);

        TextView warning = new TextView(getContext());
        warning.setText(R.string.delete_account_warning);
        warning.setTextColor(Color.GRAY);
        warning.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        warning.setPadding(0, 0, 0, padding);
        layout.addView(warning);

        EditText passInput = new EditText(getContext());
        passInput.setHint(R.string.password_hint);
        passInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passInput.setBackground(createRoundedDrawable(ContextCompat.getColor(getContext(), R.color.input_background), 30f));
        passInput.setPadding(50, 40, 50, 40);
        passInput.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        passInput.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_text));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, padding);
        passInput.setLayoutParams(params);
        layout.addView(passInput);

        Button btnDelete = new Button(getContext());
        btnDelete.setText(R.string.delete_button);
        btnDelete.setTextColor(Color.WHITE);
        btnDelete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        btnDelete.setTypeface(null, Typeface.BOLD);

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setShape(GradientDrawable.RECTANGLE);
        btnBg.setCornerRadius(30f);
        btnBg.setColor(Color.RED);
        btnDelete.setBackground(btnBg);
        btnDelete.setElevation(10f);

        btnDelete.setOnClickListener(v -> {
            String password = passInput.getText().toString().trim();
            if (password.isEmpty()) {
                passInput.setError(getString(R.string.fields_required_error));
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getEmail() != null) {
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.delete().addOnCompleteListener(deleteTask -> {
                            if (deleteTask.isSuccessful()) {
                                Toast.makeText(getContext(), R.string.success_account_deleted, Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            } else {
                                Toast.makeText(getContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), R.string.error_reauth_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        layout.addView(btnDelete);

        dialog.setContentView(layout);
        dialog.show();
    }

    private void styleButton(Button btn) {
        if (getContext() == null) return;
        GradientDrawable btnBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{
                        ContextCompat.getColor(getContext(), R.color.primary_orange),
                        ContextCompat.getColor(getContext(), R.color.gradient_end_orange)
                });
        btnBg.setCornerRadius(30f);
        btn.setBackground(btnBg);
        btn.setElevation(10f);
    }

    private GradientDrawable createRoundedDrawable(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(radius);
        shape.setColor(color);
        return shape;
    }
}