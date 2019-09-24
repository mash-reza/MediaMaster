package com.example.dubsmashmixer.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
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
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.google.android.exoplayer2.C;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.Date;

public class Audio extends AppCompatActivity {
    private static final String TAG = "Audio";

    private VideoView audioVideoView;
    private ImageButton audioVideoPlayImageButton;
    private ImageButton audioStopImageButton;
    private Button audioFromRangeButton;
    private Button audioToRangeButton;
    private FloatingActionButton audioLoadVideoFab;
    private ImageButton audioStartImageButton;
    private SeekBar audioVideoSeekBar;
    private ProgressBar audioProgressBar;
    private ConstraintLayout audioInnerLayout;
    private TextView audioVideoFileNameTextView;

    private long from = 0;
    private long to = 0;

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
        audioVideoPlayImageButton = findViewById(R.id.audio_video_play_imageButton);
        audioStopImageButton = findViewById(R.id.audio_video_stop_imageButton);
        audioFromRangeButton = findViewById(R.id.audio_from_range_button);
        audioToRangeButton = findViewById(R.id.audio_to_range_button);
        audioLoadVideoFab = findViewById(R.id.audio_load_video_fab);
        audioStartImageButton = findViewById(R.id.audio_start_image_button);
        audioVideoSeekBar = findViewById(R.id.audio_video_seekBar);
        audioProgressBar = findViewById(R.id.audio_progressbar);
        audioInnerLayout = findViewById(R.id.audio_inner_layout);
        audioVideoFileNameTextView = findViewById(R.id.audio_video_file_name_textView);
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
        if (from < to) {
            try {
                FFmpeg.getInstance(this).execute(Helper.audioCmdBuilder(bundle), new FFmpegExecuteResponseHandler() {
                    @Override
                    public void onSuccess(String message) {
                        Log.i(TAG, "onSuccess: "+message);
                        audioProgressBar.setVisibility(View.GONE);
                        audioInnerLayout.setAlpha(1);
                        Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.onsuccess_mix), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), Mixed.class);
                        intent.setData(Uri.parse(output));
                        startActivity(intent);
                    }

                    @Override
                    public void onProgress(String message) {
                    }

                    @Override
                    public void onFailure(String message) {
                        Log.e(TAG, "onFailure: "+message );
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
        } else
            Toast.makeText(this, this.getResources().getString(R.string.mix_video_conflict), Toast.LENGTH_LONG).show();

    }

    private void videoControl() {
        audioVideoPlayImageButton.setOnClickListener(v -> {
            if (audioVideoView.isPlaying()) {
                audioVideoPlayImageButton.setImageResource(R.drawable.play_icon);
                audioVideoView.pause();
                handler.removeCallbacks(runnable);
            } else {
                audioVideoPlayImageButton.setImageResource(R.drawable.pause_icon);
                audioVideoView.start();
                handler.postDelayed(runnable, 0);
            }
        });
        audioStopImageButton.setOnClickListener(v -> {
            audioVideoPlayImageButton.setImageResource(R.drawable.play_icon);
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
            audioVideoPlayImageButton.setImageResource(R.drawable.play_icon);
        });
        audioLoadVideoFab.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, Constants.VIDEO_PICK_REQUEST_CODE);
        });
        audioFromRangeButton.setOnClickListener(v -> {
            from = audioVideoView.getCurrentPosition();
            String fromString = Helper.milliSecondsToTime(from);
            audioFromRangeButton.setText(fromString);
            bundle.putString(Constants.MIX_BUNDLE_VIDEO_START_KEY, fromString);
        });
        audioToRangeButton.setOnClickListener(v -> {
            to = audioVideoView.getCurrentPosition();
            String toString = Helper.milliSecondsToTime(to);
            audioToRangeButton.setText(toString);
            bundle.putString(Constants.MIX_BUNDLE_VIDEO_FINISH_KEY, toString);
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
