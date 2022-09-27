/*
 * Copyright (c) 2021.  Foysaldev Development Studios
 */

package com.Foysaldev.videoDownloader.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.Foysaldev.videoDownloader.activity.MainActivity;
import com.Foysaldev.videoDownloader.R;
import com.Foysaldev.videoDownloader.VDApp;
import com.Foysaldev.videoDownloader.view.CustomMediaController;
import com.Foysaldev.videoDownloader.view.CustomVideoView;
//import com.Foysaldev.videoDownloader.history.HistorySQLite;
//import com.Foysaldev.videoDownloader.history.VisitedPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class BrowserWindow extends VDFragment  {

   private String url;
    private View view;
    private TouchableWebView page;
    private SSLSocketFactory defaultSSLSF;

    private FrameLayout videoFoundTV;
    private CustomVideoView videoFoundView;
    private FloatingActionButton videosFoundHUD;


    private ProgressBar loadingPageProgress;

    private int orientation;
    private boolean loadedFirsTime;

    private ArrayList<String> blockedWebsites = new ArrayList<>();
    private BottomSheetDialog dialog;

    private Activity activity;
    private InterstitialAd mInterstitialAd;

    public BrowserWindow(Activity activity) {
        this.activity = activity;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        url = data.getString("url");
        defaultSSLSF = HttpsURLConnection.getDefaultSSLSocketFactory();
        blockedWebsites.add("youtube.com");
        blockedWebsites.add("flipkart.com");
                // = Arrays.asList(getResources().getStringArray(R.array.blocked_sites));
        setRetainInstance(true);
        AdRequest adRequest = new AdRequest.Builder().build();


        InterstitialAd.load(getContext(), getResources().getString(R.string.intersititial), adRequest, new InterstitialAdLoadCallback() {
//            @Override
//            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                // The mInterstitialAd reference will be null until
//                // an ad is loaded.
//                mInterstitialAd = interstitialAd;
//            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mInterstitialAd = null;
            }
        });
    }

    private void createVideosFoundTV() {
        videoFoundTV = view.findViewById(R.id.videoFoundTV);
        videoFoundView = view.findViewById(R.id.videoFoundView);
        CustomMediaController mediaFoundController = view.findViewById(R.id.mediaFoundController);
        mediaFoundController.setFullscreenEnabled();
        videoFoundView.setMediaController(mediaFoundController);
        videoFoundTV.setVisibility(View.GONE);
    }
    private void createFoundVideosWindow() {

        dialog = new BottomSheetDialog(activity);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.video_qualities_dialog);

        ImageView dismiss = dialog.findViewById(R.id.dismiss);

        assert dismiss != null;
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        if (view == null || getResources().getConfiguration().orientation != orientation) {
            int visibility = View.VISIBLE;
            if (view != null) {
                visibility = view.getVisibility();
            }
            view = inflater.inflate(R.layout.browser, container, false);
            view.setVisibility(visibility);
            if (page == null) {
                page = view.findViewById(R.id.page);
            } else {
                View page1 = view.findViewById(R.id.page);
                ((ViewGroup) view).removeView(page1);
                ((ViewGroup) page.getParent()).removeView(page);
                ((ViewGroup) view).addView(page);
                ((ViewGroup) view).bringChildToFront(view.findViewById(R.id.videosFoundHUD));
                ((ViewGroup) view).bringChildToFront(view.findViewById(R.id.foundVideosWindow));
            }
            loadingPageProgress = view.findViewById(R.id.loadingPageProgress);
            loadingPageProgress.setVisibility(View.GONE);

           //  createVideosFoundHUD();
            createVideosFoundTV();
            createFoundVideosWindow();
            //updateFoundVideosBar();
        }

        return view;
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        if (!loadedFirsTime) {
            page.getSettings().setJavaScriptEnabled(true);
            page.getSettings().setDomStorageEnabled(true);
            page.getSettings().setAllowUniversalAccessFromFileURLs(true);
            page.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            page.setWebViewClient(new WebViewClient() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    if (blockedWebsites.contains(Utils.getBaseDomain(request.getUrl().toString()))) {
                        Log.d("vdd", "URL : " + request.getUrl().toString());
                        new AlertDialog.Builder(activity)
                                .setMessage("url is not supported according to google policy.")
                                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view, request);
                }

                @Override
                public void onPageStarted(final WebView webview, final String url, Bitmap favicon) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            EditText urlBox = getVDActivity().findViewById(R.id.et_search_bar);
                            urlBox.setText(url);
                            urlBox.setSelection(urlBox.getText().length());
                            BrowserWindow.this.url = url;
                        }
                    });
                    view.findViewById(R.id.loadingProgress).setVisibility(View.GONE);
                    loadingPageProgress.setVisibility(View.VISIBLE);
                    super.onPageStarted(webview, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    loadingPageProgress.setVisibility(View.GONE);
                }

                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                    if (activity != null) {
                        Log.d("VDDebug", "Url: " + url);

                    }
                    return super.shouldInterceptRequest(view, url);
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getVDActivity() !=
                            null) {
                        return null;
                    } else {
                        return shouldInterceptRequest(view, request.getUrl().toString());
                    }
                }


            });
            page.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    loadingPageProgress.setProgress(newProgress);
                }

                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                }

                @Override
                public Bitmap getDefaultVideoPoster() {
                    return Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
                }
            });
            page.loadUrl(url);
            loadedFirsTime = true;
        } else {
            EditText urlBox = getVDActivity().findViewById(R.id.et_search_bar);
            urlBox.setText(url);
            urlBox.setSelection(urlBox.getText().length());
        }
    }

    @Override
    public void onDestroy() {
        page.stopLoading();
        page.destroy();
        super.onDestroy();
    }
//    @Override
//    public void onBackpressed() {
//        if (foundVideosWindow.getVisibility() == View.VISIBLE && !videoFoundView.isPlaying() && videoFoundTV.getVisibility() == View.GONE) {
//            foundVideosWindow.setVisibility(View.GONE);
//        } else if (videoFoundView.isPlaying() || videoFoundTV.getVisibility() == View.VISIBLE) {
//            videoFoundView.closePlayer();
//            videoFoundTV.setVisibility(View.GONE);
//        } else if (page.canGoBack()) {
//            page.goBack();
//        } else {
//            getVDActivity().getBrowserManager().closeWindow(BrowserWindow.this);
//        }
//    }

    public String getUrl() {
        return url;
    }

    @Override
    public void onPause() {
        super.onPause();
        page.onPause();
        Log.d("debug", "onPause: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        page.onResume();
        Log.d("debug", "onResume: ");
    }


}
