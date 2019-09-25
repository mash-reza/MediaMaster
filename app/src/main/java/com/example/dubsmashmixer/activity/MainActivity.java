package com.example.dubsmashmixer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.util.Constants;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    ImageView ads, gifts, menu, image, menubar, home, album,mainMenuDialog;
    TextView mainMenuSuggest,main_menu_privacy,main_menu_rate_us;
    ConstraintLayout layout;
    View divider1,divider2;

    Button audio;
    Button mix;
    Button dub;

    boolean menuClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
        checkPermission();
        loadFFmpeg();

    }


    private void initUi() {
        ads = findViewById(R.id.main_ads);
        gifts = findViewById(R.id.main_gifts);
        menu = findViewById(R.id.main_menu);
        image = findViewById(R.id.main_image);
        layout = findViewById(R.id.main_layout);
        audio = findViewById(R.id.audio);
        mix = findViewById(R.id.voiceMix);
        dub = findViewById(R.id.dubbing);
        menubar = findViewById(R.id.main_menu_bar);
        home = findViewById(R.id.main_home_black);
        album = findViewById(R.id.main_album_regular);
        mainMenuDialog = findViewById(R.id.main_menu_dialog);
        mainMenuSuggest = findViewById(R.id.main_menu_suggest);
        main_menu_privacy = findViewById(R.id.main_menu_privacy);
        main_menu_rate_us = findViewById(R.id.main_menu_rate_us);
        divider1 = findViewById(R.id.divider1);
        divider2 = findViewById(R.id.divider2);

        Glide.with(this).load("file:///android_asset/ads.png").into(ads);
        Glide.with(this).load("file:///android_asset/gift.png").into(gifts);
        Glide.with(this).load("file:///android_asset/menu.png").into(menu);
        Glide.with(this).load("file:///android_asset/Asset 9.png").into(image);
        Glide.with(this).load("file:///android_asset/images/menubar1.png").into(menubar);
        Glide.with(this).load("file:///android_asset/images/home1.png").into(home);
        Glide.with(this).load("file:///android_asset/images/album1.png").into(album);
        Glide.with(this).load("file:///android_asset/images/menu2.png").into(mainMenuDialog);
        try {
            layout.setBackground(Drawable.createFromStream(
                    getAssets().open("images/" + "background1.jpg"), ""));
            Drawable drawable = Drawable.createFromStream(
                    getAssets().open("images/" + "Asset 13.png"), "");
            audio.setBackground(drawable);
            mix.setBackground(drawable);
            dub.setBackground(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void eventControl() {
        album.setOnClickListener(v -> {
            startActivity(new Intent(this, Files.class));
        });
        audio.setOnClickListener(v -> startActivity(new Intent(this, Audio.class)));
        mix.setOnClickListener(v -> startActivity(new Intent(this, Mix.class)));
        dub.setOnClickListener(v -> startActivity(new Intent(this, Dub.class)));
        menu.setOnClickListener(v -> {
            if (!menuClicked){
                mainMenuSuggest.setVisibility(View.VISIBLE);
                main_menu_rate_us.setVisibility(View.VISIBLE);
                main_menu_privacy.setVisibility(View.VISIBLE);
                mainMenuDialog.setVisibility(View.VISIBLE);
                divider1.setVisibility(View.VISIBLE);
                divider2.setVisibility(View.VISIBLE);
                menuClicked = true;
            }else {
                mainMenuSuggest.setVisibility(View.GONE);
                main_menu_rate_us.setVisibility(View.GONE);
                main_menu_privacy.setVisibility(View.GONE);
                mainMenuDialog.setVisibility(View.GONE);
                divider1.setVisibility(View.GONE);
                divider2.setVisibility(View.GONE);
                menuClicked = false;
            }

        });
        image.setOnClickListener(v -> {
            if (menuClicked){
                mainMenuSuggest.setVisibility(View.GONE);
                main_menu_rate_us.setVisibility(View.GONE);
                main_menu_privacy.setVisibility(View.GONE);
                mainMenuDialog.setVisibility(View.GONE);
                divider1.setVisibility(View.GONE);
                divider2.setVisibility(View.GONE);
                menuClicked = false;
            }
        });



        //TODO
        ads.setOnClickListener(v -> {
            //ads page
        });
        gifts.setOnClickListener(v -> {
            //gifts page
        });
        mainMenuSuggest.setOnClickListener(v -> {
            //suggest
        });
        main_menu_rate_us.setOnClickListener(v -> {
            //rate
        });
        main_menu_privacy.setOnClickListener(v -> {
            //privacy & policy
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        eventControl();
    }

    private void loadFFmpeg() {
        try {
            FFmpeg.getInstance(this).loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.i(TAG, "onFailure: loadBinary");
                }

                @Override
                public void onSuccess() {
                    Log.i(TAG, "onSuccess: loadBinary");
                }

                @Override
                public void onStart() {
                    Log.i(TAG, "onStart: loadBinary");
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "onFinish: loadBinary");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
                (((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) ||
                        (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED))) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO},
                    Constants.READ_EXTERNAL_PERMISSION_REQUEST_CODE);
        }
    }
}
