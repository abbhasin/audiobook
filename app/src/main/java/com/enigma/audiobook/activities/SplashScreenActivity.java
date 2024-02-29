package com.enigma.audiobook.activities;

import static android.view.View.VISIBLE;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    Handler handlerCheckUserRegistered;
    Runnable runnableCheckUserRegistered;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        handlerCheckUserRegistered = new Handler();
        progressBar = findViewById(R.id.splashScreenProgressBar);
        failedToInitMsg = findViewById(R.id.splashScreenFailureMsg);

        progressBar.setVisibility(VISIBLE);

        UserRegistrationService userRegistrationService = RetrofitFactory.getInstance().createService(UserRegistrationService.class);
        Call<User> userRegCall = userRegistrationService.createUser();
        RetryHelper.enqueueWithRetry(userRegCall, new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(SplashScreenActivity.this,
                            "unable to reach server, please check internet connection and reload app",
                            Toast.LENGTH_SHORT).show();
                    failedToInitMsg.setVisibility(VISIBLE);
                    progressBar.setVisibility(View.GONE);
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
            }
        });

        runnableCheckUserRegistered = new Runnable() {
            @Override
            public void run() {
                if (!isUserIdRegistered()) {
                    handlerCheckUserRegistered.postDelayed(this, 2000);
                }
            }
        };
        handlerCheckUserRegistered.post(runnableCheckUserRegistered);
    }

    private boolean isUserIdRegistered() {
        Optional<String> userId = SharedPreferencesHandler.getUserId(SplashScreenActivity.this);
        return userId.isPresent();
    }
}