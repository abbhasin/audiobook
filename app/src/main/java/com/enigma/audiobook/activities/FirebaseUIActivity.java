package com.enigma.audiobook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.enigma.audiobook.R;
import com.enigma.audiobook.utils.ALog;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FirebaseUIActivity extends AppCompatActivity {
    private static final String TAG = "FirebaseUiActivity";
    LinearLayout signedUserDetailsLL;
    TextView signedInUserPhoneNumber;
    Button signOut;
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_uiactivity);
        signedUserDetailsLL = findViewById(R.id.firebaseUIDetailsInfo);
        signedInUserPhoneNumber = findViewById(R.id.firebaseUIPhoneNumberVal);
        signOut = findViewById(R.id.firebaseUISignOut);

        Optional<FirebaseUser> user = getCurrentUser();
        if (user.isPresent()) {
            updateUI(user.get());
        } else {
            signedUserDetailsLL.setVisibility(View.GONE);
            createSignInIntent();
        }
    }

    private void updateUI(FirebaseUser user) {
        signedUserDetailsLL.setVisibility(View.VISIBLE);
        signedInUserPhoneNumber.setText(user.getPhoneNumber());
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutUser();
            }
        });
    }

    public void createSignInIntent() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.PhoneBuilder()
                        .setAllowedCountries(Collections.singletonList("IN"))
                        .setDefaultCountryIso("IN")
                        .build());

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.temple_icon)
                .setTheme(androidx.appcompat.R.style.Base_Theme_AppCompat)
                .build();
        signInLauncher.launch(signInIntent);
    }

    // [START auth_fui_result]
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = getCurrentUser().get();
            updateUI(user);
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            ALog.i(TAG, "unable to sign-in, error code");
            Toast.makeText(this, "Failed to sign in, please try again!", Toast.LENGTH_SHORT).show();
        }
    }
    // [END auth_fui_result]

    public Optional<FirebaseUser> getCurrentUser() {
        return Optional.ofNullable(FirebaseAuth.getInstance().getCurrentUser());
    }

    public void signOutUser() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }


    public void themeAndLogo() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();

        // [START auth_fui_theme_logo]
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.temple_icon)
                .setTheme(androidx.appcompat.R.style.Base_Theme_AppCompat)
                .build();
        signInLauncher.launch(signInIntent);
        // [END auth_fui_theme_logo]
    }

    public void privacyAndTerms() {
        List<AuthUI.IdpConfig> providers = Collections.emptyList();

        // [START auth_fui_pp_tos]
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTosAndPrivacyPolicyUrls(
                        "https://example.com/terms.html",
                        "https://example.com/privacy.html")
                .build();
        signInLauncher.launch(signInIntent);
        // [END auth_fui_pp_tos]
    }


}