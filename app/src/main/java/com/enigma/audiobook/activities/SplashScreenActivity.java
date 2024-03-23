package com.enigma.audiobook.activities;

import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.enigma.audiobook.R;
import com.enigma.audiobook.backend.models.User;
import com.enigma.audiobook.proxies.RetrofitFactory;
import com.enigma.audiobook.proxies.UserRegistrationService;
import com.enigma.audiobook.utils.RetryHelper;
import com.enigma.audiobook.utils.SharedPreferencesHandler;
import com.enigma.audiobook.utils.Utils;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.components.Preconditions;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Optional;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreenActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView failedToInitMsg;
    private Button retryInit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);
        progressBar = findViewById(R.id.splashScreenProgressBar);
        failedToInitMsg = findViewById(R.id.splashScreenFailureMsg);
        retryInit = findViewById(R.id.splashScreenRetryInit);

        if (isUserIdRegistered()) {
            Intent intent = new Intent(SplashScreenActivity.this, DarshanActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        progressBar.setVisibility(VISIBLE);

        ensureSignedOutUser();

        UserRegistrationService userRegistrationService = RetrofitFactory.getInstance().createService(UserRegistrationService.class);
        Call<User> userRegCall = userRegistrationService.createUser();
        invokeInit(userRegCall);

        retryInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryInit.setVisibility(View.GONE);
                failedToInitMsg.setVisibility(View.GONE);
                progressBar.setVisibility(VISIBLE);
                invokeInit(userRegCall);
            }
        });
    }

    private void ensureSignedOutUser() {
        Utils.addTryCatch(() -> {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                        }
                    });
        }, "SplashScreenActivity");

    }

    private void invokeInit(Call<User> userRegCall) {
        RetryHelper.enqueueWithRetry(userRegCall.clone(), new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(SplashScreenActivity.this,
                            "unable to reach server, please check internet connection and reload app",
                            Toast.LENGTH_SHORT).show();
                    failedToInitMsg.setVisibility(VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    retryInit.setVisibility(VISIBLE);
                    return;
                }

                User user = response.body();

                FirebaseCrashlytics.getInstance().setUserId(user.getUserId());
                FirebaseAnalytics.getInstance(SplashScreenActivity.this).setUserId(user.getUserId());

                SharedPreferencesHandler.addUserId(SplashScreenActivity.this, user.getUserId());
                Preconditions.checkState(SharedPreferencesHandler.getUserId(SplashScreenActivity.this).isPresent(),
                        "userId not registered in shared preferences");

                Intent intent = new Intent(SplashScreenActivity.this, DarshanActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(SplashScreenActivity.this,
                        "unable to reach server, please check internet connection and reload app",
                        Toast.LENGTH_SHORT).show();
                failedToInitMsg.setVisibility(VISIBLE);
                progressBar.setVisibility(View.GONE);
                retryInit.setVisibility(VISIBLE);
            }
        });
    }

    private boolean isUserIdRegistered() {
        Optional<String> userId = SharedPreferencesHandler.getUserId(SplashScreenActivity.this);
        return userId.isPresent();
    }
}