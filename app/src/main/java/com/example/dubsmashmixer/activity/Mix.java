package com.example.dubsmashmixer.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaCodec;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
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
import com.example.dubsmashmixer.util.FFmpegHelper;
import com.example.dubsmashmixer.util.Helper;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.logging.FileHandler;

import processing.ffmpeg.videokit.Command;
import processing.ffmpeg.videokit.LogLevel;
import processing.ffmpeg.videokit.VideoKit;

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

    Bundle bundle = new Bundle();

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
        mixStartFab.setOnClickListener(v -> {
            File videoFile = new File(Helper.getRealPathFromURI(videoUri,getApplicationContext()));
            File audioFile = new File(Helper.getRealPathFromURI(audioUri,getApplicationContext()));
            bundle.putString(Constants.MIX_BUNDLE_VIDEO_PATH,videoFile.getAbsolutePath());
            bundle.putString(Constants.MIX_BUNDLE_AUDIO_PATH,audioFile.getAbsolutePath());
            bundle.putString(Constants.MIX_BUNDLE_OUTPUT_PATH,
                    Environment.getExternalStorageDirectory().getAbsolutePath()+"/Movies/out"+new Date().getTime()+".mp4");
            try {
                FFmpeg.getInstance(this).execute(Helper.cmdBuilder(bundle),new FFmpegExecuteResponseHandler() {
                    @Override
                    public void onSuccess(String message) {
                        Log.i(TAG, "onSuccess: execute");
                    }

                    @Override
                    public void onProgress(String message) {
                        Log.i(TAG, "onProgress: execute");
                    }

                    @Override
                    public void onFailure(String message) {
                        Log.i(TAG, "onFailure: execute");
                    }

                    @Override
                    public void onStart() {
                        Log.i(TAG, "onStart: execute");
                    }

                    @Override
                    public void onFinish() {
                        Log.i(TAG, "onFinish: execute");
                    }
                });
            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }
        });
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
                    String start = Helper.milliSecondsToTime((long) leftValue);
                    mixRangeTimeTextView.setText(start);
                    bundle.putString(Constants.MIX_BUNDLE_VIDEO_START_KEY,start);
                } else {
                    mixVideoViewRangeBackgroundImageView.setVisibility(View.VISIBLE);
                    mixRangeVideoView.setVisibility(View.VISIBLE);
                    mixRangeTimeTextView.setVisibility(View.VISIBLE);
                    mixRangeVideoView.seekTo((int) rightValue);
                    String finish = Helper.milliSecondsToTime((long) rightValue);
                    mixRangeTimeTextView.setText(finish);
                    bundle.putString(Constants.MIX_BUNDLE_VIDEO_FINISH_KEY,finish);
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
                } else {
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
            String start = Helper.milliSecondsToTime(audioPlayer.getCurrentPosition());
            mixAudioFromButton.setText(start);
            bundle.putString(Constants.MIX_BUNDLE_AUDIO_START_KEY,start);
        });

        mixAudioToButton.setOnClickListener(v -> {
            String finish= Helper.milliSecondsToTime(audioPlayer.getCurrentPosition());
            mixAudioToButton.setText(finish);
            bundle.putString(Constants.MIX_BUNDLE_AUDIO_FINSIH_KEY,finish);
        });

    }

}
