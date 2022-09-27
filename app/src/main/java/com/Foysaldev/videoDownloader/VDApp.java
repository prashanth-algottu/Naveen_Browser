/*
 * Copyright (c) 2021.  Foysaldev Development Studios
 */

package com.Foysaldev.videoDownloader;

import android.app.Application;
import android.content.Intent;

import com.google.android.gms.ads.MobileAds;
import com.Foysaldev.videoDownloader.activity.MainActivity;
//import com.Foysaldev.videoDownloader.download.DownloadManager;

public class VDApp extends Application {
    private static VDApp instance;

    private MainActivity.OnBackPressedListener onBackPressedListener;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        MobileAds.initialize(this);
    }
    public static VDApp getInstance() {
        return instance;
    }

    public MainActivity.OnBackPressedListener getOnBackPressedListener() {
        return onBackPressedListener;
    }

    public void setOnBackPressedListener(MainActivity.OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }
}
