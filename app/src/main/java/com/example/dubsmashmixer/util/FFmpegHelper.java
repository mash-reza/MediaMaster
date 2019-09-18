package com.example.dubsmashmixer.util;

import android.content.Context;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FFmpegHelper {
    private static final String TAG = "FFmpegHelper";
    private Context context;
    private FFmpeg ffmpeg;

    public FFmpegHelper(Context context) {
        this.context = context;
    }

    public void loadFFMpegBinary() {
        ffmpeg =FFmpeg.getInstance(context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.e(TAG, "onFailure: failed to load binary");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Log.e(TAG, "loadFFMpegBinary: ", e);
        }
    }

    public void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.e(TAG, "onFailure: execute " + s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.e(TAG, "onSuccess: execute " + s);
                }

                @Override
                public void onProgress(String s) {
                    Log.i(TAG, "onProgress: execute " + command);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "onStart: execute " + command);
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

    public void commandBuilder(String input,String output,
                               String videoStartTime,String videoFinishTime,
                               String audioStartTime,String audioFinishTime){

    }
}
