package com.example.dubsmashmixer.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.util.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import java.io.IOException;

public class Mix extends AppCompatActivity {
    //log tag
    private static final String TAG = "Mix";

    //video card
    TextView videoFileNameTextView;
    VideoView mixVideoView;
    ImageButton mixVideoPlayImageButton;
    ImageButton mixVideoStopImageButton;
    ImageButton mixVideoPauseImageButton;
    TextView mixRangeTimeTextView;
    RangeSeekBar mixVideoRangeSeekBar;
    SeekBar mixVideoSeekBar;
    FloatingActionButton loadVideoFab;
    ImageView mixVideoViewRangeBackgroundImageView;
    VideoView mixRangeVideoView;

    //audio card
    TextView audioFileNameTextView;
    ImageButton mixAudioPlayImageButton;
    ImageButton mixAudioStopImageButton;
    ImageButton mixAudioPauseImageButton;
    Button mixAudioFromButton;
    Button mixAudioToButton;
    SeekBar mixAudioSeekBar;
    FloatingActionButton loadAudioFab;

    //start button
    FloatingActionButton mixStartFab;

    //handler for updating seek bar
    Handler handler = new Handler();

    //uri
    Uri videoUri = Uri.EMPTY;
    Uri audioUri = Uri.EMPTY;

    MediaPlayer audioPlayer = new MediaPlayer();

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
        mixRangeTimeTextView = findViewById(R.id.mix_range_time_textView);
        mixVideoRangeSeekBar = findViewById(R.id.mix_video_range_seekBar);
        mixVideoSeekBar = findViewById(R.id.mix_video_seekBar);
        loadVideoFab = findViewById(R.id.load_video_fab);
        mixVideoViewRangeBackgroundImageView = findViewById(R.id.mix_videoView_range_background_imageView);
        mixRangeVideoView = findViewById(R.id.mix_range_videoView);
        //init audio card
        audioFileNameTextView = findViewById(R.id.audio_file_name_textView);
        mixAudioPlayImageButton = findViewById(R.id.mix_audio_play_imageButton);
        mixAudioStopImageButton = findViewById(R.id.mix_audio_stop_imageButton);
        mixAudioPauseImageButton = findViewById(R.id.mix_audio_pause_imageButton);
        mixAudioFromButton = findViewById(R.id.mix_audio_from_button);
        mixAudioToButton = findViewById(R.id.mix_audio_to_button);
        mixAudioSeekBar = findViewById(R.id.mix_audio_seekBar);
        loadAudioFab = findViewById(R.id.load_audio_fab);
        //init stat button
        mixStartFab = findViewById(R.id.mix_start_fab);

        videoControl();
        audioControl();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.VIDEO_PICK_REQUEST_CODE:
                    try {
                        this.videoUri = data.getData();
                        mixVideoView.setVideoURI(data.getData());
                        mixRangeVideoView.setVideoURI(data.getData());
                    } catch (Exception e) {
                        Log.e(TAG, "onActivityResult: " + e);
                    }
                    break;
                case Constants.AUDIO_PICK_REQUEST_CODE:
                    this.audioUri = data.getData();
                    try {
                        audioPlayer.setDataSource(this, audioUri);
                    } catch (IOException e) {
                        Log.e(TAG, "onActivityResult: ", e);
                    }
                    break;
            }
        }
    }

    private String milliSecondsToTime(long milliseconds) {
        String finalTimerString = "";
        String minuteString = "";
        String secondsString;

        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
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

        return finalTimerString;
    }

    private void videoControl() {
        loadVideoFab.setOnClickListener(v -> {
            //load video view
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, Constants.VIDEO_PICK_REQUEST_CODE);
        });
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
            boolean leftVal = false;

            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                if (mixVideoView.getDuration() > 0) {
                    mixVideoRangeSeekBar.setRange(0, mixVideoView.getDuration());
                } else {
                    mixVideoRangeSeekBar.setRange(0, 100);
                }

                if (leftVal == true) {
                    mixVideoViewRangeBackgroundImageView.setVisibility(View.VISIBLE);
                    mixRangeVideoView.setVisibility(View.VISIBLE);
                    mixRangeTimeTextView.setVisibility(View.VISIBLE);
                    mixRangeVideoView.seekTo((int) leftValue);
                    mixRangeTimeTextView.setText(milliSecondsToTime((long) leftValue));
                } else {
                    mixVideoViewRangeBackgroundImageView.setVisibility(View.VISIBLE);
                    mixRangeVideoView.setVisibility(View.VISIBLE);
                    mixRangeTimeTextView.setVisibility(View.VISIBLE);
                    mixRangeVideoView.seekTo((int) rightValue);
                    mixRangeTimeTextView.setText(milliSecondsToTime((long) rightValue));

                }
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {
                if (isLeft) {
                    leftVal = true;

                    Log.i(TAG, "onStartTrackingTouch: " + leftVal);
                } else leftVal = false;

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {
                //mixVideoFirstScreenshotImageView.setVisibility(View.GONE);
                //mixVideoLastScreenshotImageView.setVisibility(View.GONE);
                mixVideoViewRangeBackgroundImageView.setVisibility(View.GONE);
                mixRangeVideoView.setVisibility(View.GONE);
                mixRangeTimeTextView.setVisibility(View.GONE);
            }
        });
    }

    private void audioControl() {
        loadAudioFab.setOnClickListener(v -> {
            //load audio
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, Constants.AUDIO_PICK_REQUEST_CODE);
        });

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mixAudioSeekBar.setMax(audioPlayer.getDuration());
                mixAudioSeekBar.setProgress(audioPlayer.getCurrentPosition());
                handler.postDelayed(this, 50);
            }
        };

        mixAudioPlayImageButton.setOnClickListener(v -> {
            //handler.removeCallbacks(runnable);
            if (!audioPlayer.isPlaying()) {
                if (audioPlayer.getCurrentPosition() == 0) {
                    try {
                        audioPlayer.prepare();
                    } catch (IOException e) {
                        Log.e(TAG, "audioControl: ", e);
                    }
                    audioPlayer.seekTo(0);
                    audioPlayer.start();
                    handler.postDelayed(runnable, 0);
                }else {
                    audioPlayer.start();
                    handler.postDelayed(runnable, 0);
                }
            }
        });
        mixAudioPauseImageButton.setOnClickListener(v -> {
            if (audioPlayer.isPlaying()) {
                audioPlayer.pause();
                mixAudioSeekBar.setProgress(audioPlayer.getCurrentPosition());
                handler.removeCallbacks(runnable);
            }
        });
        mixAudioStopImageButton.setOnClickListener(v -> {
            mixAudioSeekBar.setProgress(0);
            audioPlayer.pause();
            audioPlayer.seekTo(1);
            handler.removeCallbacks(runnable);
        });
        mixAudioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mixAudioFromButton.setOnClickListener(v -> {
           mixAudioFromButton.setText(milliSecondsToTime(audioPlayer.getCurrentPosition()));
        });

        mixAudioToButton.setOnClickListener(v -> {
            mixAudioToButton.setText(milliSecondsToTime(audioPlayer.getCurrentPosition()));
        });

    }
}
