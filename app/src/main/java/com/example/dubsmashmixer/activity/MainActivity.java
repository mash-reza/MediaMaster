package com.example.dubsmashmixer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.util.Constants;
import com.google.android.material.canvas.CanvasCompat;

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
        dub.setOnClickListener(v -> startActivity(new Intent(this, Dub.class)));
        checkPermission();
    }

    //check permission
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.READ_EXTERNAL_PERMISSION_REQUEST_CODE);
        }
    }
}
