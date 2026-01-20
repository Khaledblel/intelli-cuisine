package com.khaled.intellicuisine.utils;

import android.content.Context;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthException;
import com.khaled.intellicuisine.R;

public class AuthUtils {

    public static String getReadableErrorMessage(Context context, Exception exception) {
        if (exception instanceof FirebaseNetworkException) {
            return context.getString(R.string.error_network_request_failed);
        }

        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();

            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                case "invalid-email":
                case "ERROR_MALFORMED_EMAIL":
                    return context.getString(R.string.error_invalid_email);

                case "ERROR_WRONG_PASSWORD":
                case "wrong-password":
                case "ERROR_INVALID_CREDENTIAL":
                case "invalid-credential":
                    return context.getString(R.string.error_wrong_password);

                case "ERROR_USER_NOT_FOUND":
                case "user-not-found":
                    return context.getString(R.string.error_user_not_found);

                case "ERROR_EMAIL_ALREADY_IN_USE":
                case "email-already-in-use":
                    return context.getString(R.string.error_email_already_in_use);

                case "ERROR_WEAK_PASSWORD":
                case "weak-password":
                    return context.getString(R.string.error_weak_password);

                case "ERROR_TOO_MANY_REQUESTS":
                case "too-many-requests":
                    return context.getString(R.string.error_too_many_requests);
            }
        }

        // Fallback for unknown errors
        return context.getString(R.string.error_unknown);
    }
}
