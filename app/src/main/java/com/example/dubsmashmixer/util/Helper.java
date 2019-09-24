package com.example.dubsmashmixer.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

public class Helper {
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
            minuteString = "" + minuteString;
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
//
//    public static String[] audioCmdBuilder(Bundle bundle) {
//        String string = "-y#-ss#00:" +
//                bundle.getString(Constants.MIX_BUNDLE_VIDEO_START_KEY) +
//                "#-t#00:" +
//                bundle.getString(Constants.MIX_BUNDLE_VIDEO_FINISH_KEY) +
//                "#-i#"
//                + bundle.getString(Constants.MIX_BUNDLE_VIDEO_PATH)
//                + "#-vn#-c:a#libmp3lame#"
//                + bundle.getString(Constants.MIX_BUNDLE_OUTPUT_PATH);
//        String[] array = string.split("#");
//        return array;
//    }

    public static String[] audioCmdBuilder(Bundle bundle) {
        String string = "-y#-ss#00:" +
                bundle.getString(Constants.MIX_BUNDLE_VIDEO_START_KEY) +
                "#-t#00:" +
                bundle.getString(Constants.MIX_BUNDLE_VIDEO_FINISH_KEY) +
                "#-i#"
                + bundle.getString(Constants.MIX_BUNDLE_VIDEO_PATH)
                + "#-vn#-c:a#libmp3lame#"
                + bundle.getString(Constants.MIX_BUNDLE_OUTPUT_PATH);
        String[] array = string.split("#");
        return array;
    }


}
