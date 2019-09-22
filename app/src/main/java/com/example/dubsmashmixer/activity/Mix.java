package com.example.dubsmashmixer.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.util.Constants;
import com.example.dubsmashmixer.util.Helper;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;

import java.io.File;
import java.io.IOException;
import java.util.Date;

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
    ImageButton mixAudioPlayPauseImageButton;
    ImageButton mixAudioStopImageButton;
    Button mixAudioFromButton;
    Button mixAudioToButton;
    SeekBar mixAudioSeekBar;
    FloatingActionButton loadAudioFab;

    //start button
    FloatingActionButton mixStartFab;

    //proggress
    ProgressBar progressBar;
    //root
    ConstraintLayout layout;
    //handler for updating seek bar
    Handler handler = new Handler();

    //uri
    Uri videoUri = Uri.EMPTY;
    Uri audioUri = Uri.EMPTY;
    Uri outputUri = Uri.EMPTY;

    MediaPlayer audioPlayer = new MediaPlayer();

    Bundle bundle = new Bundle();

    //from and to audio check
    private long from = 0;
    private long to = 1;
    boolean firstAudioRange = true;

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
        mixAudioPlayPauseImageButton = findViewById(R.id.mix_audio_play_imageButton);
        mixAudioStopImageButton = findViewById(R.id.mix_audio_stop_imageButton);
        mixAudioFromButton = findViewById(R.id.mix_audio_from_button);
        mixAudioToButton = findViewById(R.id.mix_audio_to_button);
        mixAudioSeekBar = findViewById(R.id.mix_audio_seekBar);
        loadAudioFab = findViewById(R.id.load_audio_fab);
        //init stat button
        mixStartFab = findViewById(R.id.mix_start_fab);
        //progress
        progressBar = findViewById(R.id.mix_progressbar);
        //layout
        layout = findViewById(R.id.mix_layout);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
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
                    bundle.putString(Constants.MIX_BUNDLE_VIDEO_START_KEY, start);
                } else {
                    mixVideoViewRangeBackgroundImageView.setVisibility(View.VISIBLE);
                    mixRangeVideoView.setVisibility(View.VISIBLE);
                    mixRangeTimeTextView.setVisibility(View.VISIBLE);
                    mixRangeVideoView.seekTo((int) rightValue);
                    String finish = Helper.milliSecondsToTime((long) rightValue);
                    mixRangeTimeTextView.setText(finish);
                    bundle.putString(Constants.MIX_BUNDLE_VIDEO_FINISH_KEY, finish);
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

        mixAudioPlayPauseImageButton.setOnClickListener(v -> {
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
                    mixAudioPlayPauseImageButton.setImageResource(R.drawable.pause_icon);
                } else {
                    audioPlayer.start();
                    handler.postDelayed(runnable, 0);
                    mixAudioPlayPauseImageButton.setImageResource(R.drawable.pause_icon);
                }
            } else {
                audioPlayer.pause();
                mixAudioSeekBar.setProgress(audioPlayer.getCurrentPosition());
                handler.removeCallbacks(runnable);
                mixAudioPlayPauseImageButton.setImageResource(R.drawable.play_icon);
            }
        });

        mixAudioStopImageButton.setOnClickListener(v -> {
            mixAudioSeekBar.setProgress(0);
            //audioPlayer.pause();
//            audioPlayer.seekTo(1);
//            audioPlayer.release();
            audioPlayer.pause();
            audioPlayer.seekTo(1);
            //audioPlayer = null;
            handler.removeCallbacks(runnable);
            mixAudioPlayPauseImageButton.setImageResource(R.drawable.play_icon);
        });
        audioPlayer.setOnCompletionListener(mp -> {
            mixAudioPlayPauseImageButton.setImageResource(R.drawable.play_icon);
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
            long now = audioPlayer.getCurrentPosition();
            if (now <= to && firstAudioRange) {
                from =  now;
                String start = Helper.milliSecondsToTime(now);
                mixAudioFromButton.setText(start);
                bundle.putString(Constants.MIX_BUNDLE_AUDIO_START_KEY, start);
                firstAudioRange = false;
            } else
                Toast.makeText(this, getResources().getString(R.string.mix_audio_conflict1), Toast.LENGTH_SHORT).show();
        });
        mixAudioToButton.setOnClickListener(v -> {
            long now = audioPlayer.getCurrentPosition();
            if (now >= from) {
                to = now;
                String finish = Helper.milliSecondsToTime(now);
                mixAudioToButton.setText(finish);
                bundle.putString(Constants.MIX_BUNDLE_AUDIO_FINSIH_KEY, finish);
            } else
                Toast.makeText(this, getResources().getString(R.string.mix_audio_conflict2), Toast.LENGTH_SHORT).show();
        });

    }

    public void onStartClick(View v) {

        File videoFile = new File(Helper.getRealPathFromURI(videoUri, getApplicationContext()));
        File audioFile = new File(Helper.getRealPathFromURI(audioUri, getApplicationContext()));
        bundle.putString(Constants.MIX_BUNDLE_VIDEO_PATH, videoFile.getAbsolutePath());
        bundle.putString(Constants.MIX_BUNDLE_AUDIO_PATH, audioFile.getAbsolutePath());
        File outPutFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MediaMaster");
        outPutFolder.mkdirs();
        String outPutFile = outPutFolder.getAbsolutePath() + "/out" + new Date().getTime() + ".mp4";
        outputUri = Uri.parse(outPutFile);
        bundle.putString(Constants.MIX_BUNDLE_OUTPUT_PATH, outPutFile);
        String[] cmd = Helper.cmdBuilder(bundle);
        try {
            FFmpeg.getInstance(this).execute(cmd, onExecuteBinaryResponseHandler());
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

    private ExecuteBinaryResponseHandler onExecuteBinaryResponseHandler() {
        return new ExecuteBinaryResponseHandler() {
            //@SuppressLint("RestrictedApi")
            @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
            @Override
            public void onStart() {
                super.onStart();
                progressBar.setVisibility(View.VISIBLE);
                mixStartFab.setVisibility(View.INVISIBLE);
                layout.setAlpha(.3f);
//                mixAudioPlayPauseImageButton.setClickable(false);
                disableClickable();
            }

            @Override
            public void onProgress(String message) {
                super.onProgress(message);
            }

            @SuppressLint("RestrictedApi")
            @Override
            //@SuppressLint("RestrictedApi")
            public void onSuccess(String message) {
                super.onSuccess(message);
                progressBar.setVisibility(View.GONE);
                mixStartFab.setVisibility(View.VISIBLE);
                layout.setAlpha(1);
                Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.onsuccess_mix), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), Mixed.class);
                intent.setData(outputUri);
                startActivity(intent);
            }

            @SuppressLint("RestrictedApi")
            @Override
            //@SuppressLint("RestrictedApi")
            public void onFailure(String message) {
                super.onFailure(message);
                progressBar.setVisibility(View.GONE);
                mixStartFab.setVisibility(View.VISIBLE);
                layout.setAlpha(1);
                Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.onfailure_mix), Toast.LENGTH_LONG).show();

            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onFinish() {
                super.onFinish();
            }
        };
    }

    private void disableClickable() {

    }
}
