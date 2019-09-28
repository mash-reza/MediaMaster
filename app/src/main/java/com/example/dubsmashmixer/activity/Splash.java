package com.example.dubsmashmixer.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.dubsmashmixer.R;

public class Splash extends AppCompatActivity {

    ImageView splash;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splash = findViewById(R.id.splash);
        Glide.with(this).load("file:///android_asset/images/background1.jpg").into(splash);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 3000);
    }
}
