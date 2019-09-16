package com.example.dubsmashmixer.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;

import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.util.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Mix extends AppCompatActivity {
    //log tag
    private static final String TAG = "Mix";

    //video card
    TextView videoFileNameTextView;
    VideoView mixVideoView;
    ImageButton mixVideoPlayImageButton;
    ImageButton mixVideoStopImageButton;
    TextView mixVideoRangeStartTextView;
    TextView mixVideoRangeFinishTextView;
    CrystalRangeSeekbar mixVideoRangeSeekBar;
    FloatingActionButton loadVideoFab;

    //audio card
    TextView audioFileNameTextView;
    ImageButton mixAudioPlayImageButton;
    ImageButton mixAudioStopImageButton;
    TextView mixAudioRangeStartTextView;
    TextView mixAudioRangeFinishTextView;
    CrystalRangeSeekbar mixAudioRangeSeekBar;
    FloatingActionButton loadAudioFab;

    //start button
    FloatingActionButton mixStartFab;

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
        mixVideoRangeStartTextView = findViewById(R.id.mix_video_range_start_textView);
        mixVideoRangeFinishTextView = findViewById(R.id.mix_video_range_finish_textView);
        mixVideoRangeSeekBar = findViewById(R.id.mix_video_range_seekBar);
        loadVideoFab = findViewById(R.id.load_video_fab);

        //init audio card
        audioFileNameTextView = findViewById(R.id.audio_file_name_textView);
        mixAudioPlayImageButton = findViewById(R.id.mix_audio_play_imageButton);
        mixAudioStopImageButton = findViewById(R.id.mix_audio_stop_imageButton);
        mixAudioRangeStartTextView = findViewById(R.id.mix_audio_range_start_textView);
        mixAudioRangeFinishTextView = findViewById(R.id.mix_audio_range_finish_textView);
        mixAudioRangeSeekBar = findViewById(R.id.mix_audio_range_seekBar);
        loadAudioFab = findViewById(R.id.load_audio_fab);

        //init stat button
        mixStartFab = findViewById(R.id.mix_start_fab);

        //load video view
        loadVideoFab.setOnClickListener(v -> {
            //load video view
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,Constants.VIDEO_PICK_REQUEST_CODE);
        });

        // video view controlling
        mixVideoPlayImageButton.setOnClickListener(v -> {
            if (mixVideoView.isPlaying()) {
                mixVideoPlayImageButton.setImageResource(R.drawable.pause_icon);
                mixVideoView.pause();
            } else {
                mixVideoPlayImageButton.setImageResource(R.drawable.play_icon);
                mixVideoView.resume();
            }
        });
        mixVideoStopImageButton.setOnClickListener(v -> mixVideoView.stopPlayback());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.VIDEO_PICK_REQUEST_CODE) {
            try {
                mixVideoView.setVideoURI(data.getData());
            }catch (NullPointerException e){
                Log.e(TAG, "onActivityResult: null pointer exception");
            }
        }
    }
}
