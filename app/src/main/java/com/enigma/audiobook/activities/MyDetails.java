package com.enigma.audiobook.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.enigma.audiobook.R;
import com.enigma.audiobook.utils.SharedPreferencesHandler;

import java.util.Optional;

public class MyDetails extends AppCompatActivity {

    TextView userIdTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_details);
        userIdTV = findViewById(R.id.myDetailsUserId);

        Optional<String> userIdStr = SharedPreferencesHandler.getUserId(this);
        userIdTV.setText(userIdStr.orElse("No user Id"));
    }
}