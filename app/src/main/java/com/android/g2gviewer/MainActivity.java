package com.android.g2gviewer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class MainActivity extends Activity {

    private LinearLayout linearLayout;
    public final static String LINK_G2G = "http://g2g.fm/";
    private ProgressDialog progressDialog;
    public final static String EXTRA_MESSAGE = "com.android.g2gviewer.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        linearLayout = (LinearLayout) findViewById(R.id.main_linear_layout);
        DownloadTask task = new DownloadTask();
        task.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle(getString(R.string.progress_wait));
            progressDialog.setMessage(getString(R.string.progress_loading));
            progressDialog.setIndeterminate(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... urls) {
            try {
                Document document = Jsoup.connect(LINK_G2G).get();
                final Elements movieLinks = document.select(".topic_head > a[href]");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (final Element e : movieLinks) {
                            Button btnMovie = new Button(MainActivity.this);
                            btnMovie.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            btnMovie.setText(e.text());
                            btnMovie.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    goToMovie(view, e.attr("href"));
                                }
                            });
                            linearLayout.addView(btnMovie);
                        }
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

        public void goToMovie(View view, String link) {
            Intent intent = new Intent(MainActivity.this, MovieActivity.class);
            Button b = (Button) view;
            intent.putExtra(EXTRA_MESSAGE, new String[]{b.getText().toString(), link});
            startActivity(intent);
        }
    }
}
