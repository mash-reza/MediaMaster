package com.example.dubsmashmixer.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dubsmashmixer.R;
import com.example.dubsmashmixer.activity.Mixed;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.Inflater;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.MyHolder> {
    private static final String TAG = "FileAdapter";
    private Context context;
    private List<File> fileList = new ArrayList<>();

    public FileAdapter(Context context, File[] files) {
        this.context = context;
        fileList.addAll(Arrays.asList(files));
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(LayoutInflater.from(context).inflate(R.layout.file_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        if (fileList.get(position).getName().contains(".mp3")) {
            Glide.with(context).load("file:///android_asset/images/music.png").into(holder.imageView);
            holder.imageView.setBackgroundResource(R.drawable.list_item_imageview_background);
        } else if (fileList.get(position).getName().contains(".mp4")) {
            Glide.with(context).load(fileList.get(position).getAbsolutePath()).into(holder.imageView);
        }
        Glide.with(context).load("file:///android_asset/images/share1.png").into(holder.share);
        Glide.with(context).load("file:///android_asset/images/trash1.png").into(holder.delete);
        try {
            holder.layout.setBackground(Drawable.createFromStream(context.getAssets().open("images/list.png"), ""));
        } catch (IOException e) {
            Log.e(TAG, "onBindViewHolder: ", e);
        }
        holder.textView.setText(fileList.get(position).getName());
        holder.layout.setOnClickListener(v -> {
            Intent intent = new Intent(context, Mixed.class);
            intent.setData(Uri.fromFile(fileList.get(position)));
            context.startActivity(intent);
        });
        holder.delete.setOnClickListener(v -> {
            try {
                new AlertDialog.Builder(context).setMessage(R.string.delete_dialog_messege)
                        .setPositiveButton(R.string.delete_accepted, (dialog, id) -> {
                            boolean isDeleted = fileList.get(position).delete();
                            Log.i(TAG, "onMixDeleteButtonClick: " + isDeleted);
                            if (isDeleted) {
                                Toast.makeText(context, R.string.file_deleted, Toast.LENGTH_SHORT).show();
                                fileList.remove(position);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(R.string.delete_rejected, (dialog, id) -> {
                        }).create().show();

            } catch (Exception e) {
                Log.e(TAG, "onCreate: ", e);
            }
        });
        holder.share.setOnClickListener(v -> {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider"
                    , fileList.get(position)));
            shareIntent.setType("video/mp4");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, context.getResources().getString(R.string.share_intent_title)));
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        ConstraintLayout layout;
        TextView dateTextView;
        ImageView share;
        ImageView delete;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.item_name_text_view);
            imageView = itemView.findViewById(R.id.item_thumb_image_view);
            layout = itemView.findViewById(R.id.item_layout);
            dateTextView = itemView.findViewById(R.id.item_date_text_view);
            share = itemView.findViewById(R.id.item_share_image_view);
            delete = itemView.findViewById(R.id.item_delete_image_view);
        }
    }
}
