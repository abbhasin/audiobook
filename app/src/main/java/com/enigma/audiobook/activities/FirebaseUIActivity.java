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
import com.enigma.audiobook.backend.models.requests.UserRegistrationInfo;
import com.enigma.audiobook.backend.models.responses.UserAssociationResponse;
import com.enigma.audiobook.proxies.RetrofitFactory;
import com.enigma.audiobook.proxies.UserRegistrationService;
import com.enigma.audiobook.utils.ALog;
import com.enigma.audiobook.utils.RetryHelper;
import com.enigma.audiobook.utils.SharedPreferencesHandler;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.components.Preconditions;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirebaseUIActivity extends AppCompatActivity {
    private static final String TAG = "FirebaseUiActivity";
    LinearLayout signedUserDetailsLL, retrySignInLL;
    TextView signedInUserPhoneNumber, userIdSignedIn, userIdRetry;
    Button signOut, retrySignInBtn, numberHideShow;
    boolean hideShow = true;
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
        numberHideShow = findViewById(R.id.firebaseUIPhoneNumberHideShow);
        signOut = findViewById(R.id.firebaseUISignOut);
        userIdSignedIn = findViewById(R.id.firebaseUISignedInUserIdTxt);
        userIdRetry = findViewById(R.id.firebaseUIRetrySignInUserIdTxt);

        retrySignInLL = findViewById(R.id.firebaseUIRetrySignInLL);
        retrySignInBtn = findViewById(R.id.firebaseUIRetrySignInBtn);

        Optional<FirebaseUser> user = getCurrentUser();
        if (user.isPresent()) {
            updateSignedInUI(user.get());
        } else {
            signedUserDetailsLL.setVisibility(View.GONE);
            createSignInIntent();
        }
    }

    private void updateSignedInUI(FirebaseUser user) {
        signedUserDetailsLL.setVisibility(View.VISIBLE);
        retrySignInLL.setVisibility(View.GONE);
        if (hideShow) {
            signedInUserPhoneNumber.setText("***********");
        } else {
            signedInUserPhoneNumber.setText(user.getPhoneNumber());
        }
        userIdSignedIn.setText(SharedPreferencesHandler.getUserId(FirebaseUIActivity.this)
                .orElse("No UserId"));

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutUser();
            }
        });
        numberHideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideShow = !hideShow;
                if (hideShow) {
                    signedInUserPhoneNumber.setText("***********");
                } else {
                    signedInUserPhoneNumber.setText(user.getPhoneNumber());
                }

            }
        });
    }

    private void updateFailureToSignUi() {
        signedUserDetailsLL.setVisibility(View.GONE);
        retrySignInLL.setVisibility(View.VISIBLE);
        userIdRetry.setText(SharedPreferencesHandler.getUserId(FirebaseUIActivity.this)
                .orElse("No UserId"));
        retrySignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSignInIntent();
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
//                .setTosAndPrivacyPolicyUrls(
//                        "https://example.com/terms.html",
//                        "https://example.com/privacy.html")
                .setLogo(R.drawable.temple_icon)
//                .setTheme(androidx.appcompat.R.style.Base_Theme_AppCompat)
                .build();
        signInLauncher.launch(signInIntent);
    }

    // [START auth_fui_result]
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = getCurrentUser().get();
            updateUserRegistration(user);

            updateSignedInUI(user);
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            ALog.i(TAG, "unable to sign-in, error code");
            Toast.makeText(this, "Failed to sign in, please try again!", Toast.LENGTH_SHORT).show();
            updateFailureToSignUi();
        }
    }

    private void updateUserRegistration(FirebaseUser user) {
        UserRegistrationService userRegistrationService =
                RetrofitFactory.getInstance().createService(UserRegistrationService.class);

        Optional<String> userId = SharedPreferencesHandler.getUserId(FirebaseUIActivity.this);
        Preconditions.checkState(userId.isPresent(), "userId is not present");

        UserRegistrationInfo userRegistrationInfo = new UserRegistrationInfo();

        userRegistrationInfo.setUserId(userId.get());
        userRegistrationInfo.setAuthUserId(user.getUid());

        Call<UserAssociationResponse> userAssociationResCall =
                userRegistrationService.associateAuthenticatedUser(userRegistrationInfo);
        RetryHelper.enqueueWithRetry(userAssociationResCall, new Callback<UserAssociationResponse>() {
            @Override
            public void onResponse(Call<UserAssociationResponse> call, Response<UserAssociationResponse> response) {
                if (!response.isSuccessful()) {
                    ALog.i(TAG, "unable to update firebase login in user registry");
                    return;
                }
                UserAssociationResponse associationResponse = response.body();

//                if(associationResponse.getAssociationStatus().equals(
//                        UserAssociationResponse.UserAssociationStatus.FAILED_AUTH_USER_NOT_FOUND)) {
//                    ALog.i(TAG, "Auth user not found, need to retry login");
//                    // TODO: sign out and retry login
//                }

                if (associationResponse.getAssociationStatus().equals(
                        UserAssociationResponse.UserAssociationStatus.MAPPED_TO_EXISTING_USER)) {
                    SharedPreferencesHandler.addUserId(FirebaseUIActivity.this,
                            associationResponse.getUser().getUserId());
                }
            }

            @Override
            public void onFailure(Call<UserAssociationResponse> call, Throwable t) {
                ALog.i(TAG, "unable to update firebase login in user registry");
            }
        });
    }

    public void signOutUser() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseUIActivity.this.finish();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public static Optional<FirebaseUser> getCurrentUser() {
        return Optional.ofNullable(FirebaseAuth.getInstance().getCurrentUser());
    }

    /**
     * this is a blocking call, should not be called on main thread
     */
    public static Optional<String> getIdToken() {
        AtomicReference<String> idTokenRef = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        getCurrentUser().get()
                .getIdToken(true)
                .addOnCompleteListener(task -> {
                    try {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            // Use the ID token for authentication or other purposes
                            ALog.i(TAG, "ID Token: " + idToken);
                            idTokenRef.set(idToken);
                        } else {
                            // Handle error
                            ALog.e(TAG, "Error getting ID token: ", task.getException());
                        }
                    } finally {
                        countDownLatch.countDown();
                    }
                });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return Optional.ofNullable(idTokenRef.get());
    }

}