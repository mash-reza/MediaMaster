package com.example.dubsmashmixer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.example.dubsmashmixer.R;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

import javax.security.auth.login.LoginException;

public class Mixed extends AppCompatActivity {
    private static final String TAG = "Mixed";

    SimpleExoPlayer player;
    DataSource.Factory mediaDataSourceFactory;
    PlayerView playerView;
    ImageButton deleteButton;
    Uri uri = Uri.EMPTY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixed);
        playerView = findViewById(R.id.mixed_player_view);
        uri = getIntent().getData();
    }

    public void onMixDubShareButtonClick(View v) {
        File file = new File(uri.getPath());
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this,
                this.getApplicationContext().getPackageName() + ".provider"
                , file));
        shareIntent.setType("video/mp4");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_intent_title)));
    }

    public void onMixDeleteButtonClick(View v) {
        File file = new File(uri.getPath());
        try {
            new AlertDialog.Builder(this).setMessage(R.string.delete_dialog_messege)
                    .setPositiveButton(R.string.delete_accepted, (dialog, id) -> {
                        boolean isDeleted = file.delete();
                        Log.i(TAG, "onMixDeleteButtonClick: " + isDeleted);
                        finish();
                    })
                    .setNegativeButton(R.string.delete_rejected, (dialog, id) -> {
                    }).create().show();

        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initializePlayer() {

        player = ExoPlayerFactory.newSimpleInstance(this);

        mediaDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"));

        MediaSource mediaSource = new ExtractorMediaSource.Factory(mediaDataSourceFactory)
                .createMediaSource(uri);


        player.prepare(mediaSource, false, false);
        player.setPlayWhenReady(true);


        playerView.setShutterBackgroundColor(Color.TRANSPARENT);
        playerView.setPlayer(player);
        playerView.requestFocus();

    }

    private void releasePlayer() {
        player.release();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) initializePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23) initializePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) releasePlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) releasePlayer();
    }
}
