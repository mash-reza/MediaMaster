package com.example.dubsmashmixer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.adapter.FileAdapter;
import com.example.dubsmashmixer.util.Repo;

public class Files extends AppCompatActivity {
RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        recyclerView = findViewById(R.id.files_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        FileAdapter adapter = new FileAdapter(this, Repo.getFiles());
        recyclerView.setAdapter(adapter);
    }
}
