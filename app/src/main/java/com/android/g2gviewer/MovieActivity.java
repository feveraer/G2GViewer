package com.android.g2gviewer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MovieActivity extends Activity {

    private ProgressDialog progressDialog;
    private String movieLink;
    private WebView webView;
    private LinearLayout contentView;
    private FrameLayout customViewContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;
    FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        Intent intent = getIntent();
        String[] movieRef = intent.getStringArrayExtra(MainActivity.EXTRA_MESSAGE);

        contentView = (LinearLayout) findViewById(R.id.movie_linearlayout);
        webView = (WebView) findViewById(R.id.movie_view);
        customViewContainer = (FrameLayout) findViewById(R.id.fullscreen_content);

        WebSettings webSettings = webView.getSettings();
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        //ActionBar ab = getActionBar();
        //ab.setTitle(movieRef[0]);
        movieLink = MainActivity.LINK_G2G + movieRef[1];

        DownloadTask task = new DownloadTask();
        task.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.movie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DownloadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MovieActivity.this);
            progressDialog.setTitle(getString(R.string.progress_wait));
            progressDialog.setMessage(getString(R.string.progress_loading));
            progressDialog.setIndeterminate(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... urls) {
            try {
                final Document doc = Jsoup.connect(movieLink).get();
                final Elements iFrame = doc.select(".postcontent iframe");
                final Document doc2 = Jsoup.connect(iFrame.get(0).attr("src"))
                        .referrer(movieLink)
                        .get();
                final Elements iFrame2 = doc2.select("iframe");
                final Document doc3 = Jsoup.connect(iFrame2.get(0).attr("src"))
                        .referrer(iFrame.get(0).attr("src"))
                        .get();
                final Elements iFrameGoogle = doc3.select("iframe");
                iFrameGoogle.attr("width", "100%");
                iFrameGoogle.attr("allowfullscreen", "true");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public boolean shouldOverrideUrlLoading(WebView webview, String url) {
                                webview.setWebChromeClient(new WebChromeClient() {

                                    private View mCustomView;

                                    @Override
                                    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
                                        // if a view already exists then immediately terminate the new one
                                        if (mCustomView != null) {
                                            callback.onCustomViewHidden();
                                            return;
                                        }

                                        // Add the custom view to its container.
                                        customViewContainer.addView(view, COVER_SCREEN_GRAVITY_CENTER);
                                        mCustomView = view;
                                        customViewCallback = callback;

                                        // hide main browser view
                                        contentView.setVisibility(View.GONE);

                                        // Finally show the custom view container.
                                        customViewContainer.setVisibility(View.VISIBLE);
                                        customViewContainer.bringToFront();
                                    }

                                });
                                webView.loadUrl(url);

                                return true;
                            }
                        });
                        webView.loadUrl(iFrameGoogle.get(0).attr("src"));
                        //webView.loadData(iFrameGoogle.get(0).outerHtml(), "text/html", "UTF-8");
                        //Toast.makeText(MovieActivity.this, iFrameGoogle.get(0).outerHtml(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
        }
    }
}
