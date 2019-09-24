package com.example.dubsmashmixer.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class mic extends AppCompatActivity {
    private static final String TAG = "mic";

    Button start;
    Button stop;
    Button pause;
    Button play;

    MediaRecorder recorder = null;
    MediaPlayer player = null;

    File folder;
    File out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mic);
        start = findViewById(R.id.start1);
        stop = findViewById(R.id.stop1);
        pause = findViewById(R.id.pause1);
        play = findViewById(R.id.play1);

        folder=new File(Environment.getExternalStorageDirectory()+"/MediaMaster/test");
        folder.mkdirs();
        out = new File(folder.getAbsolutePath(),"out3gparmwb"+new Date().getTime()+".3gp");
    }

    public void onStart(View v){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        recorder.setOutputFile(out.getAbsolutePath());
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "startRecording: ", e);
        }
        recorder.start();
    }
    public void onStop(View v){
        recorder.release();
        recorder = null;
    }
    public void onPlay(View v){
        player = new MediaPlayer();
        try {
            player.setDataSource(out.getAbsolutePath());
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.start();
    }
    public void onPause(View v){
        player.pause();
    }
}
