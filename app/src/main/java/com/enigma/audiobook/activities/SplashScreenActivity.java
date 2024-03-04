package com.enigma.audiobook.activities;

import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.enigma.audiobook.R;
import com.enigma.audiobook.backend.models.User;
import com.enigma.audiobook.proxies.RetrofitFactory;
import com.enigma.audiobook.proxies.UserRegistrationService;
import com.enigma.audiobook.utils.RetryHelper;
import com.enigma.audiobook.utils.SharedPreferencesHandler;
import com.google.firebase.components.Preconditions;

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



//        SharedPreferencesHandler.addUserId(this, "65a7936792bb9e2f44a1ea47");

        if (isUserIdRegistered()) {
            Intent intent = new Intent(SplashScreenActivity.this, DarshanActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        progressBar.setVisibility(VISIBLE);

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