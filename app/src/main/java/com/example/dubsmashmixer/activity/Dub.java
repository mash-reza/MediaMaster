package com.example.dubsmashmixer.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.util.Constants;
import com.example.dubsmashmixer.util.Helper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.xml.validation.TypeInfoProvider;

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
    private SeekBar dubVideoSeekBar;

    //record
    private MediaRecorder mediaRecorder = null;

    //play
    private MediaPlayer player = null;

    //uri to output file - it is sent via intent to dubbed activity for final result
    Uri outputUri = Uri.EMPTY;
    Uri videoUri = Uri.EMPTY;
    File output;

    //flag for start fab
    boolean isRecording = false;

    //from and to
    long from = 0;
    long to = 0;

    //handler for playback
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            dubVideoSeekBar.setMax(dubVideoView.getDuration());
            dubVideoSeekBar.setProgress(dubVideoView.getCurrentPosition());
            handler.postDelayed(this, 50);
        }
    };

    //audio manger for muting video
    AudioManager audioManager;
    int currentMusicVolume = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dub);
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, Constants.AUDIO_RECORD_PERMISSION_REQUEST_CODE);
        }
        audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
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
        dubVideoSeekBar = findViewById(R.id.dub_video_seekBar);
    }

    public void onStartClick(View v) {
        if (from < to) {
            if (isRecording) {
                stopRecording();
                isRecording = false;
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentMusicVolume,0);
                handler.removeCallbacks(runnable);
                dubVideoView.pause();
                play();
            } else {
                startRecording();
                isRecording = true;
//                audioManager.setSpeakerphoneOn(false);
                currentMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                handler.removeCallbacks(runnable);
                dubVideoView.seekTo((int) from);
                dubVideoView.start();
                handler.postDelayed(runnable, 0);
            }
        } else
            Toast.makeText(this, this.getResources().getString(R.string.mix_audio_conflict), Toast.LENGTH_LONG).show();

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
        mediaRecorder.setMaxDuration((int) (to - from));
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
    }

    private void videoControl() {
        MediaController mediaController = new MediaController(this);
        mediaController.setMediaPlayer(dubVideoView);
        dubVideoPlayImageButton.setOnClickListener(v -> {
            if (dubVideoView.isPlaying()) {
                dubVideoPlayImageButton.setImageResource(R.drawable.play_icon);
                dubVideoView.pause();
                handler.removeCallbacks(runnable);
            } else {
                dubVideoPlayImageButton.setImageResource(R.drawable.pause_icon);
                dubVideoView.start();
                handler.postDelayed(runnable, 0);
            }
        });
        dubStopImageButton.setOnClickListener(v -> {
            dubVideoPlayImageButton.setImageResource(R.drawable.play_icon);
            dubVideoView.pause();
            dubVideoView.seekTo(0);
            dubVideoSeekBar.setProgress(0);
            handler.removeCallbacks(runnable);
        });
        dubVideoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    dubVideoView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        dubVideoView.setOnCompletionListener(mp -> {
            dubVideoPlayImageButton.setImageResource(R.drawable.play_icon);
        });
        dubLoadVideoFab.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, Constants.VIDEO_PICK_REQUEST_CODE);
        });
        dubFromRangeButton.setOnClickListener(v -> {
            from = dubVideoView.getCurrentPosition();
            dubFromRangeButton.setText(Helper.milliSecondsToTime(from));
        });
        dubToRangeButton.setOnClickListener(v -> {
            to = dubVideoView.getCurrentPosition();
            dubToRangeButton.setText(Helper.milliSecondsToTime(to));
        });
    }

    void play() {
        player = new MediaPlayer();
        try {
            player.setDataSource(output.getAbsolutePath());
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.start();
    }

    void stop() {
        player.stop();
        player.release();
        player = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                this.videoUri = data.getData();
                dubVideoView.setVideoURI(videoUri);
                dubVideoSeekBar.setMax(dubVideoView.getDuration());
            } catch (Exception e) {
                Log.e(TAG, "onActivityResult: " + e);
            }

        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        videoControl();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (output != null) {
            Log.i(TAG, "onDestroy: deleted audio internal file" + output.delete());
        }
    }
}
