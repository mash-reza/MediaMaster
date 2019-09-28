package com.example.dubsmashmixer.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class Audio extends AppCompatActivity {
    private static final String TAG = "Audio";

    private VideoView audioVideoView;
    private ImageView audioVideoPlayImageView;
    private ImageView audioStopImageView;
    private ImageView audioLoadImageView;
    private Button audioStartImageView;
    private SeekBar audioVideoSeekBar;
    private ProgressBar audioProgressBar;
    private ConstraintLayout audioInnerLayout;
    private TextView audioVideoFileNameTextView;
    private FrameLayout audioVideoViewFrameLayout;
    ImageView audioVideoFrameImage;


    private Uri videoUri = Uri.EMPTY;
    private String output;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            audioVideoSeekBar.setMax(audioVideoView.getDuration());
            audioVideoSeekBar.setProgress(audioVideoView.getCurrentPosition());
            handler.postDelayed(this, 50);
        }
    };

    private Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        initUI();
    }

    private void initUI() {
        audioVideoView = findViewById(R.id.audio_videoView);
        audioVideoPlayImageView = findViewById(R.id.audio_video_play_image_view);
        audioStopImageView = findViewById(R.id.audio_video_stop_image_view);
        audioLoadImageView = findViewById(R.id.audio_load_video);
        audioStartImageView = findViewById(R.id.audio_start_image_view);
        audioVideoSeekBar = findViewById(R.id.audio_video_seekBar);
        audioProgressBar = findViewById(R.id.audio_progressbar);
        audioInnerLayout = findViewById(R.id.audio_inner_layout);
        audioVideoFileNameTextView = findViewById(R.id.audio_video_file_name_textView);
        audioVideoViewFrameLayout = findViewById(R.id.audio_videoView_frame);
        audioVideoFrameImage = findViewById(R.id.audio_video_frame_image);


        try {
            audioInnerLayout.setBackground(Drawable.createFromStream(
                    getAssets().open("images/background1.jpg"), ""));

            audioStartImageView.setBackground(Drawable.createFromStream(
                    getAssets().open("images/hazfe seda.png"), ""));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Glide.with(this).load("file:///android_asset/images/play3.png").into(audioVideoPlayImageView);
        Glide.with(this).load("file:///android_asset/images/pause3.png").into(audioStopImageView);
        Glide.with(this).load("file:///android_asset/images/add video frame.png").into(audioVideoFrameImage);
        Glide.with(this).load("file:///android_asset/images/add.png").into(audioLoadImageView);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                this.videoUri = data.getData();
                audioVideoView.setVideoURI(videoUri);
                audioVideoView.start();
                audioVideoView.pause();
                handler.postDelayed(runnable, 0);
                audioVideoSeekBar.setMax(audioVideoView.getDuration());
                //set
                bundle.putString(Constants.MIX_BUNDLE_VIDEO_PATH,
                        new File(Helper.getRealPathFromURI(videoUri, getApplicationContext())).getAbsolutePath());
                audioVideoFileNameTextView.setText(new File(Helper.getRealPathFromURI(videoUri, getApplicationContext())).getName());
                audioVideoFrameImage.setVisibility(View.GONE);
                audioLoadImageView.setVisibility(View.GONE);
                audioVideoViewFrameLayout.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Log.e(TAG, "onActivityResult: " + e);
            }

        }
    }

    public void onAudioStartClick(View v) {
        File outPutFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MediaMaster");
        outPutFolder.mkdirs();
        output = outPutFolder.getAbsolutePath() + "/out" + new Date().getTime() + ".mp3";
        bundle.putString(Constants.MIX_BUNDLE_OUTPUT_PATH, output);
        try {
            FFmpeg.getInstance(this).execute(Helper.audioCmdBuilder(bundle), new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    Log.i(TAG, "onSuccess: " + message);
                    audioProgressBar.setVisibility(View.GONE);
                    audioInnerLayout.setAlpha(1);
                    Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.onsuccess_mix), Toast.LENGTH_LONG).show();
//                    audioVideoPlayImageView.setVisibility(View.INVISIBLE);
//                    audioVideoSeekBar.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(getApplicationContext(), Mixed.class);
                    intent.setData(Uri.parse(output));
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onProgress(String message) {
                }

                @Override
                public void onFailure(String message) {
                    Log.e(TAG, "onFailure: " + message);
                    audioProgressBar.setVisibility(View.GONE);
                    audioInnerLayout.setAlpha(1);
                    Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.onfailure_mix), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onStart() {
                    Log.i(TAG, "onStart: ");
                    audioProgressBar.setVisibility(View.VISIBLE);
                    audioInnerLayout.setAlpha(.3f);
                    Toast.makeText(getApplicationContext(), R.string.preparing_ouput, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "onFinish: ");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }

    }

    private void videoControl() {
        audioVideoPlayImageView.setOnClickListener(v -> {
            if (!audioVideoView.isPlaying()) {
                Glide.with(this).load("file:///android_asset/images/play3.png").into(audioVideoPlayImageView);
                if (audioVideoView.getCurrentPosition() == 0)
                    audioVideoView.start();
                else {
                    audioVideoView.start();
                    audioVideoView.seekTo(audioVideoView.getCurrentPosition());
                }
                handler.postDelayed(runnable, 0);
            } else {
                Glide.with(this).load("file:///android_asset/images/pause3.png").into(audioVideoPlayImageView);
                audioVideoView.pause();
                audioVideoSeekBar.setProgress(audioVideoView.getCurrentPosition());
                handler.removeCallbacks(runnable);
            }
        });
        audioStopImageView.setOnClickListener(v -> {
            Glide.with(this).load("file:///android_asset/images/play3.png").into(audioVideoPlayImageView);
            audioVideoView.pause();
            audioVideoView.seekTo(0);
            audioVideoSeekBar.setProgress(0);
            handler.removeCallbacks(runnable);
        });
        audioVideoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    audioVideoView.seekTo(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        audioVideoView.setOnCompletionListener(mp -> {
            Glide.with(this).load("file:///android_asset/images/play3.png").into(audioVideoPlayImageView);
        });
        audioLoadImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, Constants.VIDEO_PICK_REQUEST_CODE);
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        videoControl();
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }
}
