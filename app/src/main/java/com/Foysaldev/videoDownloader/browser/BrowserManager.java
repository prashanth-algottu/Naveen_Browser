/*
 * Copyright (c) 2021.  Foysaldev Development Studios
 */

package com.Foysaldev.videoDownloader.browser;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.Foysaldev.videoDownloader.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BrowserManager extends VDFragment {
    private List<BrowserWindow> windows;
    private ArrayList<String> blockedWebsites;
    private List<String> whiteListSites;

    private Activity activity;

    public BrowserManager(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.d("debug", "Browser Manager added");
        windows = new ArrayList<>();
        blockedWebsites = new ArrayList<>();
       // blockedWebsites = Arrays.asList(getResources().getStringArray(R.array.blocked_sites));
       // whiteListSites = Arrays.asList(getResources().getStringArray(R.array.white_sites));
        closeAllWindow();
    }

    public void newWindow(String url) {
        if (blockedWebsites.contains(Utils.getBaseDomain(url))) {
            final Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.dialog_youtube);

            Button button = dialog.findViewById(R.id.btn_ok);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        } else {
            Bundle data = new Bundle();
            data.putString("url", url);
            BrowserWindow window = new BrowserWindow(activity);
            window.setArguments(data);
            getFragmentManager().beginTransaction()
                    .add(R.id.home_container, window, null)
                    .commit();
            windows.add(window);
           // getVDActivity().setOnBackPressedListener(window);
            if (windows.size() > 1) {
                window = windows.get(windows.size() - 2);
                if (window != null && window.getView() != null) {
                    window.getView().setVisibility(View.GONE);
                    window.onPause();
                }
            }
        }
    }

    public void closeWindow(BrowserWindow window) {
        final EditText searchBar = getVDActivity().findViewById(R.id.et_search_bar);
        windows.remove(window);
        getFragmentManager().beginTransaction().remove(window).commit();
        if (!windows.isEmpty()) {
            BrowserWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow != null && topWindow.getView() != null) {
                topWindow.onResume();
                topWindow.getView().setVisibility(View.VISIBLE);
            }
            if (topWindow != null) {

                searchBar.setText(topWindow.getUrl());
                searchBar.setSelection(searchBar.getText().length());
                //getVDActivity().setOnBackPressedListener(topWindow);
            }
        } else {
            searchBar.getText().clear();
            getVDActivity().setOnBackPressedListener(null);
        }
    }

    public void closeAllWindow() {
        if (!windows.isEmpty()) {
            for (Iterator<BrowserWindow> iterator = windows.iterator(); iterator.hasNext(); ) {
                BrowserWindow window = iterator.next();
                getFragmentManager().beginTransaction().remove(window).commit();
                iterator.remove();
            }
            getVDActivity().setOnBackPressedListener(null);
        } else {
            getVDActivity().setOnBackPressedListener(null);
        }
    }
    public void hideCurrentWindow() {
        if (!windows.isEmpty()) {
            BrowserWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow.getView() != null) {
                topWindow.getView().setVisibility(View.GONE);
            }
        }
    }

    public void unhideCurrentWindow() {
        if (!windows.isEmpty()) {
            BrowserWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow.getView() != null) {
                topWindow.getView().setVisibility(View.VISIBLE);
            }
        } else {
            getVDActivity().setOnBackPressedListener(null);
        }
    }
    public void pauseCurrentWindow() {
        if (!windows.isEmpty()) {
            BrowserWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow.getView() != null) {
                topWindow.onPause();
            }
        }
    }
    public void resumeCurrentWindow() {
        if (!windows.isEmpty()) {
            BrowserWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow.getView() != null) {
                topWindow.onResume();
            }
        } else {
            getVDActivity().setOnBackPressedListener(null);
        }
    }
}
