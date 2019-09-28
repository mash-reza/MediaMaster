package com.example.dubsmashmixer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.adapter.FileAdapter;
import com.example.dubsmashmixer.util.Repo;

import java.io.IOException;

public class Files extends AppCompatActivity {
RecyclerView recyclerView;
ConstraintLayout filesBackgroundLayout;
ImageView menubar, home, album;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        recyclerView = findViewById(R.id.files_recycler_view);
        filesBackgroundLayout = findViewById(R.id.filesBackgroundLayout);
        menubar = findViewById(R.id.files_menu_bar);
        home = findViewById(R.id.files_home_black);
        album = findViewById(R.id.files_album_regular);
        Glide.with(this).load("file:///android_asset/images/menubar1.png").into(menubar);
        Glide.with(this).load("file:///android_asset/images/home2.png").into(home);
        Glide.with(this).load("file:///android_asset/images/album2.png").into(album);
        try {
            filesBackgroundLayout.setBackground(Drawable.createFromStream(
                    getAssets().open("images/" + "background1.jpg"), ""));
        } catch (IOException e) {
            e.printStackTrace();
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        FileAdapter adapter = new FileAdapter(this, Repo.getFiles());
        recyclerView.setAdapter(adapter);
        home.setOnClickListener(v -> finish());
    }
}
