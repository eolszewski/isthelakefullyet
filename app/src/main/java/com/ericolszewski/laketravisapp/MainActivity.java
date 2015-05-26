package com.ericolszewski.laketravisapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;


public class MainActivity extends Activity {

    //region Class Variables
    TextView donateTextView, waterLevelTextView, yesNoTextView;
    ProgressBar loadingProgressBar;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        yesNoTextView = (TextView) findViewById(R.id.yesNoTextView);
        waterLevelTextView = (TextView) findViewById(R.id.waterLevelTextView);
        donateTextView = (TextView) findViewById(R.id.donateTextView);
        loadingProgressBar = (ProgressBar)findViewById(R.id.loadingProgressBar);

        Typeface antonTypeface = Typeface.createFromAsset(getAssets(), "fonts/Anton.ttf");
        yesNoTextView.setTypeface(antonTypeface);

        Typeface openSansSemiBoleTypeface = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Semibold.ttf");
        waterLevelTextView.setTypeface(openSansSemiBoleTypeface);

        Typeface openSansTypeface = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        donateTextView.setTypeface(openSansTypeface);

        donateTextView.setMovementMethod(LinkMovementMethod.getInstance());
        donateTextView.setText(Html.fromHtml("<p>\n" +
                "            Like all Central Texas residents, we're excited to see the lake fill up, but all this rain means there's been some flooding too. Please consider <a style=\"color:white;\" href=\"http://www.adrntx.org/index.php/ways-to-give/central-tx-floods-relief-fund/\">donating to the Central TX Floods Relief Fund</a>.\n" +
                "          </p>"));

        if (isNetworkAvailable()) {
            loadingProgressBar.setVisibility(View.VISIBLE);
            GetWaterLevelJob job = new GetWaterLevelJob();
            job.execute("http://www.golaketravis.com/waterlevel/");
        }
        else {
            loadingProgressBar.setVisibility(View.GONE);
            yesNoTextView.setText("PLEASE CHECK YOUR NETWORK CONNECTIVITY");
        }
    }

    //region Asynchronous Network Task to Retrieve Current Water Level
    private class GetWaterLevelJob extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            try {
                Document doc = Jsoup.connect(params[0]).get();
                Element level = doc.select("p.nav").get(1);
                return level.text().replaceAll("[^\\d.]", "");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Error";
        }

        @Override
        protected void onPostExecute(String level) {
            if (level.equals("Error")) {
                yesNoTextView.setText("PROBLEMS CONNECTING TO golaketravis.com, PLEASE TRY AGAIN LATER");
            }
            else {
                double decimalWaterLevel = Double.parseDouble(level);
                if ((int)(681 - decimalWaterLevel) > 0) {
                    yesNoTextView.setText("NOPE");
                    waterLevelTextView.setText(String.format("%d MORE FEET TO GO", Math.round(681 - decimalWaterLevel)));
                }
                else {
                    yesNoTextView.setText("YES!");
                }
            }
            loadingProgressBar.setVisibility(View.GONE);
        }
    }
    //endregion

    //region Helper Methods

    //Determines Network Availability For Current Device
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    //endregion
}