package com.example.dubsmashmixer.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.util.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import java.io.File;
import java.text.DecimalFormat;

public class Mix extends AppCompatActivity {
    //log tag
    private static final String TAG = "Mix";

    //video card
    TextView videoFileNameTextView;
    VideoView mixVideoView;
    ImageButton mixVideoPlayImageButton;
    ImageButton mixVideoStopImageButton;
    ImageButton mixVideoPauseImageButton;
    TextView mixVideoRangeStartTextView;
    TextView mixVideoRangeFinishTextView;
    RangeSeekBar mixVideoRangeSeekBar;
    SeekBar mixVideoSeekBar;
    FloatingActionButton loadVideoFab;

    //audio card
    TextView audioFileNameTextView;
    ImageButton mixAudioPlayImageButton;
    ImageButton mixAudioStopImageButton;
    ImageButton mixAudioPauseImageButton;
    TextView mixAudioRangeStartTextView;
    TextView mixAudioRangeFinishTextView;
    CrystalRangeSeekbar mixAudioRangeSeekBar;
    SeekBar mixAudioSeekBar;
    FloatingActionButton loadAudioFab;

    //start button
    FloatingActionButton mixStartFab;

    //handler for updating seek bar
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mix);
        initUI();
    }

    private void initUI() {
        //init video card
        videoFileNameTextView = findViewById(R.id.video_file_name_textView);
        mixVideoView = findViewById(R.id.mix_videoView);
        mixVideoPlayImageButton = findViewById(R.id.mix_video_play_imageButton);
        mixVideoStopImageButton = findViewById(R.id.mix_video_stop_imageButton);
        mixVideoPauseImageButton = findViewById(R.id.mix_video_pause_imageButton);
        mixVideoRangeStartTextView = findViewById(R.id.mix_video_range_start_textView);
        mixVideoRangeFinishTextView = findViewById(R.id.mix_video_range_finish_textView);
        mixVideoRangeSeekBar = findViewById(R.id.mix_video_range_seekBar);
        mixVideoSeekBar = findViewById(R.id.mix_video_seekBar);
        loadVideoFab = findViewById(R.id.load_video_fab);

        //init audio card
        audioFileNameTextView = findViewById(R.id.audio_file_name_textView);
        mixAudioPlayImageButton = findViewById(R.id.mix_audio_play_imageButton);
        mixAudioStopImageButton = findViewById(R.id.mix_audio_stop_imageButton);
        mixAudioPauseImageButton = findViewById(R.id.mix_audio_pause_imageButton);
        mixAudioRangeStartTextView = findViewById(R.id.mix_audio_range_start_textView);
        mixAudioRangeFinishTextView = findViewById(R.id.mix_audio_range_finish_textView);
        mixAudioRangeSeekBar = findViewById(R.id.mix_audio_range_seekBar);
        mixAudioSeekBar = findViewById(R.id.mix_audio_seekBar);
        loadAudioFab = findViewById(R.id.load_audio_fab);

        //init stat button
        mixStartFab = findViewById(R.id.mix_start_fab);

        //load video view
        loadVideoFab.setOnClickListener(v -> {
            //load video view
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, Constants.VIDEO_PICK_REQUEST_CODE);
        });

        // video view controlling

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mixVideoSeekBar.setMax(mixVideoView.getDuration());
                mixVideoSeekBar.setProgress(mixVideoView.getCurrentPosition());
                handler.postDelayed(this, 50);
            }
        };

        mixVideoPlayImageButton.setOnClickListener(v -> {
            //handler.removeCallbacks(runnable);
            if (!mixVideoView.isPlaying()) {
                if (mixVideoView.getCurrentPosition() == 0)
                    mixVideoView.start();
                else {
                    mixVideoView.start();
                    mixVideoView.seekTo(mixVideoView.getCurrentPosition());
                }
                handler.postDelayed(runnable, 0);
            }
        });
        mixVideoPauseImageButton.setOnClickListener(v -> {
            if (mixVideoView.isPlaying()) {
                mixVideoView.pause();
                mixVideoSeekBar.setProgress(mixVideoView.getCurrentPosition());
                handler.removeCallbacks(runnable);
            }
            //handler.postDelayed(runnable,50);
        });
        mixVideoStopImageButton.setOnClickListener(v -> {
            mixVideoView.seekTo(0);
            mixVideoSeekBar.setProgress(0);
            mixVideoView.pause();
            handler.removeCallbacks(runnable);

//            handler.post(runnable);
        });

        mixVideoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mixVideoView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mixVideoRangeSeekBar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                if (mixVideoView.getDuration() > 0) {
                    mixVideoRangeSeekBar.setRange(0, mixVideoView.getDuration());
                } else {
                    mixVideoRangeSeekBar.setRange(0,100);
                }
                Log.i(TAG, "min: " + leftValue);
                Log.i(TAG, "max: " + rightValue);
                //DecimalFormat df = new DecimalFormat("00.00");
                mixVideoRangeStartTextView.setText(milliSecondsToTime((long) leftValue));
                mixVideoRangeFinishTextView.setText(milliSecondsToTime((long) rightValue));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.VIDEO_PICK_REQUEST_CODE) {
            try {
                mixVideoView.setVideoURI(data.getData());
            } catch (Exception e) {
                Log.e(TAG, "onActivityResult: " + e);
            }
        }
    }

    private String milliSecondsToTime(long milliseconds) {
        String finalTimerString = "";
        String minuteString = "";
        String secondsString = "";

        // Convert total duration into time
//            int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
//            if (hours > 0) {
//                finalTimerString = hours + ":";
//            }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }
        if (minutes < 10) {
            minuteString = "0" + minutes;
        } else {
            minuteString = "" + minuteString;
        }

        finalTimerString = minuteString + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }
}
