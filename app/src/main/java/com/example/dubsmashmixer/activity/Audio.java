package com.example.dubsmashmixer.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.ThemedSpinnerAdapter;

import com.example.dubsmashmixer.R;

public class Audio extends AppCompatActivity {
    private static final String TAG = "Audio";
ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dub_smash);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setMax(100);
        new Thread(() -> {
            int i = 0;
            while (i < 100){
                Log.i(TAG,String.valueOf(i));
                progressBar.setProgress(i++);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
