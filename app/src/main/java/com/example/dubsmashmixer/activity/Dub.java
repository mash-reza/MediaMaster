package com.example.dubsmashmixer.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.util.Constants;
import com.example.dubsmashmixer.util.Helper;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
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
    private SeekBar dubVideoSeekBar;
    private ProgressBar progressBar;
    private ImageView visualiser;
    private ConstraintLayout innerLayout;

    //record
    private MediaRecorder mediaRecorder = null;

    //play
    private MediaPlayer player = null;

    //uri to output file - it is sent via intent to dubbed activity for final result
    Uri outputUri= Uri.EMPTY;
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

    //bundle fpr ffmpeg
    Bundle bundle = new Bundle();

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
        Glide.with(this).load(R.drawable.audio_visual).into(visualiser);
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
        visualiser = findViewById(R.id.visual);
        progressBar = findViewById(R.id.dub_progressbar);
        innerLayout = findViewById(R.id.dub_inner_layout);
    }

    public void onStartClick(View v) {
        if (from < to) {
            if (isRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        } else
            Toast.makeText(this, this.getResources().getString(R.string.mix_video_conflict), Toast.LENGTH_LONG).show();

    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        output = new File(getFilesDir(), "audio.3gp");
        bundle.putString(Constants.MIX_BUNDLE_AUDIO_PATH,output.getAbsolutePath());
        mediaRecorder.setOutputFile(output.getAbsolutePath());
        mediaRecorder.setMaxDuration((int) (to - from));
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "startRecording: ", e);
        }
        mediaRecorder.start();

        currentMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        handler.removeCallbacks(runnable);
        dubVideoView.seekTo((int) from);
        dubVideoView.start();
        handler.postDelayed(runnable, 0);
        progressBar.setVisibility(View.GONE);
        visualiser.setVisibility(View.VISIBLE);
        Toast.makeText(this, R.string.recooding_started, Toast.LENGTH_SHORT).show();
        isRecording = true;
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        File outPutFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MediaMaster/Dub");
        outPutFolder.mkdirs();
        String outPutFile = outPutFolder.getAbsolutePath() + "/out" + new Date().getTime() + ".mp4";
        outputUri = Uri.parse(outPutFile);
        bundle.putString(Constants.MIX_BUNDLE_OUTPUT_PATH, outPutFile);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentMusicVolume, 0);
        handler.removeCallbacks(runnable);
        dubVideoView.pause();
        try {
            FFmpeg.getInstance(this).execute(Helper.dubCmdBuilder(bundle),
                    new ExecuteBinaryResponseHandler() {
                        //@SuppressLint("RestrictedApi")
                        @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
                        @Override
                        public void onStart() {
                            super.onStart();
                            //disable clicks
                            visualiser.setVisibility(View.GONE);
                            progressBar.setVisibility(View.VISIBLE);
                            innerLayout.setAlpha(.3f);
                            Toast.makeText(Dub.this,R.string.preparing_ouput, Toast.LENGTH_LONG).show();
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
                            Intent intent = new Intent(getApplicationContext(), Mixed.class);
                            intent.setData(outputUri);
                            startActivity(intent);
                        }

                        @SuppressLint("RestrictedApi")
                        @Override
                        //@SuppressLint("RestrictedApi")
                        public void onFailure(String message) {
                            Toast.makeText(getApplicationContext(),R.string.onfailure_mix, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "onFailure: "+message);
                        }

                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public void onFinish() {
                            super.onFinish();
                            progressBar.setVisibility(View.GONE);
                            innerLayout.setAlpha(1);
                        }
                    }
            );
        }catch (FFmpegCommandAlreadyRunningException e){
            Log.e(TAG, "onStartClick: ",e );
        }
        isRecording = false;
    }

    private void videoControl() {
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
            String fromString = Helper.milliSecondsToTime(from);
            dubFromRangeButton.setText(fromString);
            bundle.putString(Constants.MIX_BUNDLE_VIDEO_START_KEY, fromString);
        });
        dubToRangeButton.setOnClickListener(v -> {
            to = dubVideoView.getCurrentPosition();
            String toString = Helper.milliSecondsToTime(to);
            dubToRangeButton.setText(toString);
            bundle.putString(Constants.MIX_BUNDLE_VIDEO_FINISH_KEY, toString);
        });
    }

//    void play() {
//        player = new MediaPlayer();
//        try {
//            player.setDataSource(output.getAbsolutePath());
//            player.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        player.start();
//    }

//    void stop() {
//        player.stop();
//        player.release();
//        player = null;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                this.videoUri = data.getData();
                dubVideoView.setVideoURI(videoUri);
                dubVideoSeekBar.setMax(dubVideoView.getDuration());
                //set
                bundle.putString(Constants.MIX_BUNDLE_VIDEO_PATH,
                        new File(Helper.getRealPathFromURI(videoUri, getApplicationContext())).getAbsolutePath());
            } catch (Exception e) {
                Log.e(TAG, "onActivityResult: " + e);
            }

        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        videoControl();
        handler.post(() -> {
            if((dubVideoView.getCurrentPosition()==to)&&isRecording)
                stopRecording();
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (output != null) {
            Log.i(TAG, "onDestroy: deleted audio internal file" + output.delete());
        }
    }
}
