package com.example.dubsmashmixer.util;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Repo {

    public static File[] getFiles() {
        File folderFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/MediaMaster");
        folderFile.mkdirs();
        File[] fileList = folderFile.listFiles();
        return fileList;
    }
}
