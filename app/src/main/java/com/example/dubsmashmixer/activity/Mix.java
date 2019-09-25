package com.example.dubsmashmixer.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.util.Constants;
import com.example.dubsmashmixer.util.Helper;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class Mix extends AppCompatActivity {
    //log tag
    private static final String TAG = "Mix";

    //video card
    private TextView videoFileNameTextView;
    private VideoView mixVideoView;
    private ImageView mixVideoPlayImageView;
    private ImageView mixVideoStopImageView;
    private SeekBar mixVideoSeekBar;
    private ImageView loadVideoFab;
    private Button mixVideoFromButton;
    private Button mixVideoToButton;
    private ImageView mixVideoFrameImage;
    private FrameLayout mixVideoViewFrameLayout;


    //audio card
    private TextView audioFileNameTextView;
    private ImageView mixAudioPlayPauseImageView;
    private ImageView mixAudioStopImageView;
    private Button mixAudioFromButton;
    private Button mixAudioToButton;
    private SeekBar mixAudioSeekBar;
    private ImageView mixLoadAudioImageView;
    private ConstraintLayout mixAudioLayout;

    //start button
    private Button mixStartButton;

    //proggress
    private ProgressBar progressBar;
    //root
    private ConstraintLayout layout;

    //handler for updating seek bar
    private Handler handler = new Handler();
    Runnable audioRunnable = new Runnable() {
        @Override
        public void run() {
            mixAudioSeekBar.setMax(audioPlayer.getDuration());
            mixAudioSeekBar.setProgress(audioPlayer.getCurrentPosition());
            handler.postDelayed(this, 50);
        }
    };
    Runnable videoRunnable = new Runnable() {
        @Override
        public void run() {
            mixVideoSeekBar.setMax(mixVideoView.getDuration());
            mixVideoSeekBar.setProgress(mixVideoView.getCurrentPosition());
            handler.postDelayed(this, 50);
        }
    };

    //uri
    private Uri videoUri = Uri.EMPTY;
    private Uri audioUri = Uri.EMPTY;
    private Uri outputUri = Uri.EMPTY;

    private MediaPlayer audioPlayer = null;
    private boolean isMixVideoViewLoaded = false;
    private boolean isMixAudioLoaded = false;

    private Bundle bundle = new Bundle();

    // check
    private long audioFrom, audioTo, videoFrom, videoTo = 0;

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
        mixVideoPlayImageView = findViewById(R.id.mix_video_play_imageView);
        mixVideoStopImageView = findViewById(R.id.mix_video_stop_imageView);
        mixVideoFromButton = findViewById(R.id.mix_video_from_button);
        mixVideoToButton = findViewById(R.id.mix_video_to_button);
        mixVideoSeekBar = findViewById(R.id.mix_video_seekBar);
        loadVideoFab = findViewById(R.id.load_video_fab);
        mixVideoFrameImage = findViewById(R.id.mix_video_frame_image);
        mixVideoViewFrameLayout = findViewById(R.id.mix_videoView_frame);
        //init audio card
        audioFileNameTextView = findViewById(R.id.audio_file_name_textView);
        mixAudioPlayPauseImageView = findViewById(R.id.mix_audio_play_imageView);
        mixAudioStopImageView = findViewById(R.id.mix_audio_stop_imageView);
        mixAudioFromButton = findViewById(R.id.mix_audio_from_button);
        mixAudioToButton = findViewById(R.id.mix_audio_to_button);
        mixAudioSeekBar = findViewById(R.id.mix_audio_seekBar);
        mixLoadAudioImageView = findViewById(R.id.mix_load_audio);
        mixAudioLayout = findViewById(R.id.mix_audio_cardView);
        //init stat button
        mixStartButton = findViewById(R.id.mix_start_button);
        //progress
        progressBar = findViewById(R.id.mix_progressbar);
        //layout
        layout = findViewById(R.id.mix_layout);

        try {
            Drawable buttonBackground = Drawable.createFromStream(
                    getAssets().open("images/hazfe seda.png"), "");
            mixStartButton.setBackground(buttonBackground);
            Drawable mainBackground = Drawable.createFromStream(
                    getAssets().open("images/background1.jpg"), "");
            layout.setBackground(mainBackground);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Glide.with(this).load("file:///android_asset/images/play3.png").into(mixAudioPlayPauseImageView);
        Glide.with(this).load("file:///android_asset/images/play3.png").into(mixVideoPlayImageView);
        Glide.with(this).load("file:///android_asset/images/pause3.png").into(mixAudioStopImageView);
        Glide.with(this).load("file:///android_asset/images/pause3.png").into(mixVideoStopImageView);
        Glide.with(this).load("file:///android_asset/images/add video frame.png").into(mixVideoFrameImage);
        Glide.with(this).load("file:///android_asset/images/add.png").into(loadVideoFab);
        Glide.with(this).load("file:///android_asset/images/add file.png").into(mixLoadAudioImageView);

        if (isMixVideoViewLoaded) {
            mixVideoViewFrameLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        videoControl();
        audioControl();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (audioPlayer != null) {
            audioPlayer.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (audioPlayer != null) {
            audioPlayer.pause();
            audioPlayer.seekTo(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) {
            handler.removeCallbacks(videoRunnable);
            handler.removeCallbacks(audioRunnable);
            audioPlayer.release();
            audioPlayer = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.VIDEO_PICK_REQUEST_CODE:
                    try {
                        this.videoUri = data.getData();
                        Log.e(TAG, "onActivityResult: " + videoUri);
                        Log.e(TAG, "onActivityResult: " + data.getData());
                        Log.e(TAG, "onActivityResult: " + data.getData());
                        Log.e(TAG, "onActivityResult: " + data.getData().getPath());
                        Log.e(TAG, "onActivityResult: " + data.getData().getLastPathSegment());
                        Log.e(TAG, "onActivityResult: " + data.getData().getPathSegments());
                        mixVideoView.setVideoURI(data.getData());
                        mixVideoView.start();
                        mixVideoView.pause();
                        mixVideoView.seekTo(0);
                        handler.postDelayed(videoRunnable, 0);
                        isMixVideoViewLoaded = true;
                        mixVideoFrameImage.setVisibility(View.GONE);
                        loadVideoFab.setVisibility(View.GONE);
                        mixVideoViewFrameLayout.setVisibility(View.VISIBLE);
                        videoFileNameTextView.setText(new File(Helper.getRealPathFromURI(videoUri, getApplicationContext())).getName());
//                        mixStartButton.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e(TAG, "onActivityResult: " + e);
                    }
                    break;
                case Constants.AUDIO_PICK_REQUEST_CODE:
                    this.audioUri = data.getData();
                    try {
                        audioPlayer = new MediaPlayer();
                        audioPlayer.setDataSource(this, audioUri);
                        audioPlayer.prepare();
                        audioPlayer.start();
                        audioPlayer.pause();
                        audioPlayer.seekTo(0);
                        handler.postDelayed(audioRunnable, 0);
                        audioFileNameTextView.setText(new File(Helper.getRealPathFromURI(audioUri, getApplicationContext())).getName());
                        isMixAudioLoaded = true;
                        mixLoadAudioImageView.setVisibility(View.GONE);
                        mixAudioLayout.setVisibility(View.VISIBLE);

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

        mixVideoPlayImageView.setOnClickListener(v -> {
            //handler.removeCallbacks(runnable);
            if (!mixVideoView.isPlaying()) {
//                mixVideoPlayImageView.setImageResource(R.drawable.play_icon);
                Glide.with(this).load("file:///android_asset/images/play3.png").into(mixVideoPlayImageView);
                if (mixVideoView.getCurrentPosition() == 0)
                    mixVideoView.start();
                else {
                    mixVideoView.start();
                    mixVideoView.seekTo(mixVideoView.getCurrentPosition());
                }
                handler.postDelayed(videoRunnable, 0);
            } else {
//                mixVideoPlayImageView.setImageResource(R.drawable.pause_icon);
                Glide.with(this).load("file:///android_asset/images/pause3.png").into(mixVideoPlayImageView);
                mixVideoView.pause();
                mixVideoSeekBar.setProgress(mixVideoView.getCurrentPosition());
                handler.removeCallbacks(videoRunnable);
            }
        });
        mixVideoStopImageView.setOnClickListener(v -> {
            Glide.with(this).load("file:///android_asset/images/play3.png").into(mixVideoPlayImageView);
            mixVideoView.seekTo(0);
            mixVideoSeekBar.setProgress(0);
            mixVideoView.pause();
            handler.removeCallbacks(videoRunnable);
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
        mixVideoFromButton.setOnClickListener(v -> {
            if (isMixVideoViewLoaded) {
                videoFrom = mixVideoView.getCurrentPosition();
                String fromString = Helper.milliSecondsToTime(videoFrom);
                mixVideoFromButton.setText(fromString);
                bundle.putString(Constants.MIX_BUNDLE_VIDEO_START_KEY, fromString);
            }
        });
        mixVideoToButton.setOnClickListener(v -> {
            if (isMixVideoViewLoaded) {
                videoTo = mixVideoView.getCurrentPosition();
                String toString = Helper.milliSecondsToTime(videoTo);
                mixVideoToButton.setText(toString);
                bundle.putString(Constants.MIX_BUNDLE_VIDEO_FINISH_KEY, toString);
            }
        });
        mixVideoView.setOnCompletionListener(mp -> Glide.with(this).load("file:///android_asset/images/play3.png").into(mixVideoPlayImageView));
    }

    private void audioControl() {
        mixLoadAudioImageView.setOnClickListener(v -> {
            //load audio
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, Constants.AUDIO_PICK_REQUEST_CODE);
        });

        mixAudioPlayPauseImageView.setOnClickListener(v -> {
            //handler.removeCallbacks(runnable);
            if (audioPlayer != null)
                if (!audioPlayer.isPlaying()) {
                    if (audioPlayer.getCurrentPosition() == 0) {
                        audioPlayer.seekTo(0);
                        audioPlayer.start();
                        handler.postDelayed(audioRunnable, 0);
//                        mixAudioPlayPauseImageView.setImageResource(R.drawable.pause_icon);
                        Glide.with(this).load("file:///android_asset/images/play3.png").into(mixAudioPlayPauseImageView);
                    } else {
                        audioPlayer.start();
                        handler.postDelayed(audioRunnable, 0);
//                        mixAudioPlayPauseImageView.setImageResource(R.drawable.pause_icon);
                        Glide.with(this).load("file:///android_asset/images/pause3.png").into(mixAudioPlayPauseImageView);

                    }
                } else {
                    audioPlayer.pause();
                    mixAudioSeekBar.setProgress(audioPlayer.getCurrentPosition());
                    handler.removeCallbacks(audioRunnable);
//                    mixAudioPlayPauseImageView.setImageResource(R.drawable.play_icon);
                    Glide.with(this).load("file:///android_asset/images/play3.png").into(mixAudioPlayPauseImageView);
                }
        });

        mixAudioStopImageView.setOnClickListener(v -> {
            if (audioPlayer != null) {
                mixAudioSeekBar.setProgress(0);
                //audioPlayer.pause();
//            audioPlayer.seekTo(1);
//            audioPlayer.release();
                audioPlayer.pause();
                audioPlayer.seekTo(1);
                //audioPlayer = null;
                handler.removeCallbacks(audioRunnable);
                mixAudioPlayPauseImageView.setImageResource(R.drawable.play_icon);
            }
        });
        if (audioPlayer != null)
            audioPlayer.setOnCompletionListener(mp -> {
                mixAudioPlayPauseImageView.setImageResource(R.drawable.play_icon);
            });

        mixAudioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (audioPlayer != null)
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
            if (audioPlayer != null) {
                audioFrom = audioPlayer.getCurrentPosition();
                String fromString = Helper.milliSecondsToTime(audioFrom);
                mixAudioFromButton.setText(fromString);
                bundle.putString(Constants.MIX_BUNDLE_AUDIO_START_KEY, fromString);
            }
        });
        mixAudioToButton.setOnClickListener(v -> {
            if (audioPlayer != null) {
                audioTo = audioPlayer.getCurrentPosition();
                String toString = Helper.milliSecondsToTime(audioTo);
                mixAudioToButton.setText(toString);
                bundle.putString(Constants.MIX_BUNDLE_AUDIO_FINSIH_KEY, toString);
            }
        });
    }


    public void onStartClick(View v) {
        if (audioFrom < audioTo && videoFrom < videoTo) {
            File videoFile = new File(Helper.getRealPathFromURI(videoUri, getApplicationContext()));
            File audioFile = new File(Helper.getRealPathFromURI(audioUri, getApplicationContext()));
            bundle.putString(Constants.MIX_BUNDLE_VIDEO_PATH, videoFile.getAbsolutePath());
            bundle.putString(Constants.MIX_BUNDLE_AUDIO_PATH, audioFile.getAbsolutePath());
            File outPutFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MediaMaster");
            outPutFolder.mkdirs();
            String outPutFile = outPutFolder.getAbsolutePath() + "/out" + new Date().getTime() + ".mp4";
            outputUri = Uri.parse(outPutFile);
            bundle.putString(Constants.MIX_BUNDLE_OUTPUT_PATH, outPutFile);
            String[] cmd = Helper.mixCmdBuilder(bundle);
            try {
                FFmpeg.getInstance(this).execute(cmd, onExecuteBinaryResponseHandler());
            } catch (FFmpegCommandAlreadyRunningException e) {
                // do nothing for now
            }
        } else if (audioFrom > audioTo && videoFrom > videoTo) {
            Toast.makeText(this, R.string.mix_both_conflict, Toast.LENGTH_LONG).show();
        } else if (videoFrom > videoTo) {
            Toast.makeText(this, R.string.mix_video_conflict, Toast.LENGTH_LONG).show();
        } else if (audioFrom > audioTo) {
            Toast.makeText(this, R.string.mix_audio_conflict, Toast.LENGTH_LONG).show();
        } else Toast.makeText(this, R.string.mix_not_choosen_conflict, Toast.LENGTH_SHORT).show();
    }

    private ExecuteBinaryResponseHandler onExecuteBinaryResponseHandler() {
        return new ExecuteBinaryResponseHandler() {
            //@SuppressLint("RestrictedApi")
            @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
            @Override
            public void onStart() {
                super.onStart();
                progressBar.setVisibility(View.VISIBLE);
                mixStartButton.setVisibility(View.INVISIBLE);
                layout.setAlpha(.3f);
//                mixAudioPlayPauseImageView.setClickable(false);
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
                mixStartButton.setVisibility(View.VISIBLE);
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
                mixStartButton.setVisibility(View.VISIBLE);
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
