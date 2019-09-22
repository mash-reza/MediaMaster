package com.example.dubsmashmixer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.VideoView;

import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.util.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class Dub extends AppCompatActivity {
    private static final String TAG = "Dub";

    //Ui
    private VideoView dubVideoView;
    private ImageButton dubVideoPlayImageButton;
    private ImageButton dubStopImageButton;
    private Button dubFromRangeButton;
    private Button dubToRangeButton;
    private FloatingActionButton dubLoadVideoFab;
    private FloatingActionButton dubStartFab;

    //record
    private MediaRecorder mediaRecorder = null;

    //play
    private MediaPlayer player = null;

    //uri to output file - it is sent via intent to dubbed activity for final result
    Uri outputUri = Uri.EMPTY;
    File output;

    //flag for start fab
    boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dub);
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, Constants.AUDIO_RECORD_PERMISSION_REQUEST_CODE);
        }
        initUI();
    }

    private void initUI() {
        dubVideoView = findViewById(R.id.dub_videoView);
        dubVideoPlayImageButton = findViewById(R.id.dub_video_play_imageButton);
        dubStopImageButton = findViewById(R.id.dub_video_stop_imageButton);
        dubFromRangeButton = findViewById(R.id.dub_from_range_button);
        dubToRangeButton = findViewById(R.id.dub_to_range_button);
        dubLoadVideoFab = findViewById(R.id.dub_load_video_fab);
        dubStartFab = findViewById(R.id.dub_start_fab);
    }

    public void onStartClick(View v) {
        if (isRecording) {
            stopRecording();
            isRecording = false;
        } else {
            startRecording();
            isRecording = true;
        }
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        //File outputFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MediaMaster/Dub/");
        //Log.i(TAG, "startRecording: creating output file " + outputFolder.mkdirs());
        //File output = new File(outputFolder.getAbsolutePath()+"out" + new Date().getTime() + ".3gp");
        output = new File(getFilesDir(), "audio.3gp");
        mediaRecorder.setOutputFile(output.getAbsolutePath());
        mediaRecorder.setMaxDuration(20000);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "startRecording: ", e);
        }
        mediaRecorder.start();
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        play();
    }

    private void videoControl() {

    }

    void play(){
        player = new MediaPlayer();
        try {
            player.setDataSource(output.getAbsolutePath());
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.start();
    }
    void stop(){
        player.stop();
        player.release();
        player = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (output != null) {
            Log.i(TAG, "onDestroy: deleted audio internal file" + output.delete());
        }
    }
}
