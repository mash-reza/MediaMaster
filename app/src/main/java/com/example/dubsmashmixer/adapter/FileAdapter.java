package com.example.dubsmashmixer.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.activity.Mixed;

import java.io.File;
import java.util.zip.Inflater;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyHolder> {

    private Context context;
    private File[] files;

    public FileAdapter(Context context, File[] files){
        this.context = context;

        this.files = files;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(context).inflate(R.layout.file_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Glide.with(context).load(files[position].getAbsolutePath()).into(holder.imageView);
        holder.textView.setText(files[position].getName());
        holder.layout.setOnClickListener(v -> {
            Intent intent = new Intent(context, Mixed.class);
            intent.setData(Uri.fromFile(files[position]));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return files.length;
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        ConstraintLayout layout;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.item_name_text_view);
            imageView =itemView.findViewById(R.id.item_thumb_image_view);
            layout = itemView.findViewById(R.id.item_layout);
        }
    }
}
