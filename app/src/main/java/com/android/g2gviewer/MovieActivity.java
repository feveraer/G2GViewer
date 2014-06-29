package com.android.g2gviewer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class MovieActivity extends Activity {

    private ProgressDialog progressDialog;
    private String movieLink;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        Intent intent = getIntent();
        String[] movieRef = intent.getStringArrayExtra(MainActivity.EXTRA_MESSAGE);
        textView = (TextView) findViewById(R.id.movie_textview);
        ActionBar ab = getActionBar();
        ab.setTitle(movieRef[0]);
        movieLink = MainActivity.LINK_G2G + movieRef[1];
        textView.setText(movieLink);

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
                final Document document = Jsoup.connect(movieLink).get();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(textView.getText().toString() + "\n\n"
                                + document.body().text());
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
