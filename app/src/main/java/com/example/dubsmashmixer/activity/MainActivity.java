package com.example.dubsmashmixer.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.dubsmashmixer.R;

public class MainActivity extends AppCompatActivity {

    Button dubSmash;
    Button mix;
    Button dub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dubSmash = findViewById(R.id.dubSmash);
        mix = findViewById(R.id.voiceMix);
        dub = findViewById(R.id.dubbing);
        dubSmash.setOnClickListener(v -> startActivity(new Intent(this, DubSmash.class)));
        mix.setOnClickListener(v -> startActivity(new Intent(this, Mix.class)));
        dub.setOnClickListener(v -> startActivity(new Intent(this,Dub.class)));
    }
}
