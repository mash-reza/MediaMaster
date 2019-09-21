package com.example.dubsmashmixer.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.example.dubsmashmixer.R;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
public class FFmpegHelper{
    private static final String TAG = "FFmpegHelper";
    private Context context;

    public FFmpegHelper(Context context) {
        this.context = context;
    }

    public void execute(final String[] command) {
        try {
            FFmpeg.getInstance(context).execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.e(TAG, "onFailure: execute " + s);
                    Toast.makeText(context,context.getResources().getString(R.string.onfailure_mix), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(String s) {
                    Log.i(TAG, "onSuccess: execute " + s);
                    Toast.makeText(context,context.getResources().getString(R.string.onsuccess_mix), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "onProgress: execute " + s);
                }

                @Override
                public void onStart() {
                    Log.i(TAG, "onStart: execute " + command);
                    Toast.makeText(context,context.getResources().getString(R.string.onstart_mix), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "onFinish: execute " + command);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }
}
