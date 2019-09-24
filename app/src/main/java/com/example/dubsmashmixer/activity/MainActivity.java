package com.example.dubsmashmixer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.util.Constants;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    Button dubSmash;
    Button mix;
    Button dub;
    Button history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dubSmash = findViewById(R.id.dubSmash);
        mix = findViewById(R.id.voiceMix);
        dub = findViewById(R.id.dubbing);
        history = findViewById(R.id.history);
        history.setOnClickListener(v -> {
            startActivity(new Intent(this,mic.class));
        });
        dubSmash.setOnClickListener(v -> startActivity(new Intent(this, Audio.class)));
        mix.setOnClickListener(v -> startActivity(new Intent(this, Mix.class)));
        dub.setOnClickListener(v -> startActivity(new Intent(this, Dub.class)));
        checkPermission();
        try {
            FFmpeg.getInstance(this).loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.i(TAG, "onFailure: loadBinary");
                }

                @Override
                public void onSuccess() {
                    Log.i(TAG, "onSuccess: loadBinary");
                }

                @Override
                public void onStart() {
                    Log.i(TAG, "onStart: loadBinary");
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "onFinish: loadBinary");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
        try {
            FFmpeg.getInstance(this).execute(new String[]{"--enable","-libopencore","-amrwb"}, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    Log.i(TAG, "onSuccess: "+message);
                }

                @Override
                public void onProgress(String message) {
                    Log.i(TAG, "onProgress: "+message);
                }

                @Override
                public void onFailure(String message) {
                    Log.i(TAG, "onFailure: "+message);
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    //check permission
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.READ_EXTERNAL_PERMISSION_REQUEST_CODE);
        }
    }
}
