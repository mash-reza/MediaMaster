package com.example.dubsmashmixer.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Helper {
    private static final String TAG = "Helper";

    public static String getRealPathFromURI(Uri contentURI, Context context) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public static String getRealAudioPathFromURI(Uri contentURI, Context context) {
        InputStream in = null;
        OutputStream out = null;
        File outPutFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MediaMaster");
        outPutFolder.mkdirs();
        String outPutFilePath = outPutFolder.getAbsolutePath() + "audioTempFile.mp3";
        try {
            in = context.getContentResolver().openInputStream(contentURI);

            out = new FileOutputStream(new File(outPutFilePath));
            byte[] array = new byte[1024];
            int length;
            while ((length = in.read(array)) > 0) {
                out.write(array, 0, length);
            }

        } catch (IOException e) {
            Log.e(TAG, "getRealPathFromURI1: " + e.getMessage());
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return outPutFilePath;
    }

    public static String milliSecondsToTime(long milliseconds) {
        String finalTimerString = "";
        String minuteString = "";
        String secondsString;

        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }
        if (minutes < 10) {
            minuteString = "0" + minutes;
        } else {
            minuteString = "" + minutes;
        }

        finalTimerString = minuteString + ":" + secondsString;

        return finalTimerString;
    }

    public static String[] mixCmdBuilder(Bundle bundle) {
        String string = "-y#-ss#00:" +
                bundle.getString(Constants.MIX_BUNDLE_VIDEO_START_KEY) +
                "#-t#00:" +
                bundle.getString(Constants.MIX_BUNDLE_VIDEO_FINISH_KEY) +
                "#-i#"
                + bundle.getString(Constants.MIX_BUNDLE_VIDEO_PATH)
                + "#-ss#00:" +
                bundle.getString(Constants.MIX_BUNDLE_AUDIO_START_KEY) +
                "#-t#00:" +
                bundle.getString(Constants.MIX_BUNDLE_AUDIO_FINSIH_KEY) +
                "#-i#"
                + bundle.getString(Constants.MIX_BUNDLE_AUDIO_PATH)
                + "#-c#copy#-map#0:v:0#-map#1:a:0#-shortest#"
                + bundle.getString(Constants.MIX_BUNDLE_OUTPUT_PATH);
        String[] array = string.split("#");
        return array;
    }


    public static String[] dubCmdBuilder(Bundle bundle) {
        String string = "-y#-ss#00:" +
                bundle.getString(Constants.MIX_BUNDLE_VIDEO_START_KEY) +
                "#-t#00:" +
                bundle.getString(Constants.MIX_BUNDLE_VIDEO_FINISH_KEY) +
                "#-i#"
                + bundle.getString(Constants.MIX_BUNDLE_VIDEO_PATH)
                + "#-i#"
                + bundle.getString(Constants.MIX_BUNDLE_AUDIO_PATH)
                + "#-c:v#copy#-map#0:v:0#-c:a#copy#-map#1:a:0#-movflags#faststart#-shortest#"
                + bundle.getString(Constants.MIX_BUNDLE_OUTPUT_PATH);
        String[] array = string.split("#");
        return array;
    }


    public static String[] audioCmdBuilder(Bundle bundle) {
        String string = "-y#-i#"
                + bundle.getString(Constants.MIX_BUNDLE_VIDEO_PATH)
                + "#-c:a#libmp3lame#"
                + bundle.getString(Constants.MIX_BUNDLE_OUTPUT_PATH);
        String[] array = string.split("#");
        return array;
    }

}
