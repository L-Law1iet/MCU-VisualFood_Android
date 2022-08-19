package com.imvision.mcu.visualfood_blockchain;

import android.annotation.SuppressLint;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class Network_Fitbit extends AppCompatActivity {
    private static final String TAG = "AuthenticationActivity";

    @SuppressLint("JavascriptInterface")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_fitbit);

        final WebView wvAuthorise = (WebView) findViewById(R.id.wvAuthorise);
        wvAuthorise.getSettings().setJavaScriptEnabled(true);
        wvAuthorise.getSettings().setLoadWithOverviewMode(true);
        wvAuthorise.getSettings().setUseWideViewPort(true);
        wvAuthorise.getSettings().setBuiltInZoomControls(true);
        wvAuthorise.setWebChromeClient(new WebChromeClient());
        wvAuthorise.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.v(TAG, url);
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                wvAuthorise.loadUrl(url);

                if(url.contains("https://finished/")){      //CallBack URL


                    String access_token = getString(url,"a");
                    String user_id = getString(url,"b");
                    // remember to decide if you want the first or last parameter with the same name
                    // If you want the first call setPreferFirstRepeatedParameter(true);
                    Log.d(TAG, "devon check code = " + access_token);
                    Log.d(TAG, "devon check user_id = " + user_id);

                    Intent it = new Intent();
                    Bundle bundle=new Bundle();
                    bundle.putString("ACCESS_TOKEN",access_token);
                    bundle.putString("USER_ID",user_id);

                    it.putExtras(bundle);
                    setResult(RESULT_OK,it);
                    finish();
                }
                return true;
            }
        });
        //TODO:please provide your personal client_id  here.

        wvAuthorise.loadUrl("https://www.fitbit.com/oauth2/authorize?response_type=token&client_id=XXXXXX"+
                "&scope=activity%20heartrate%20location%20nutrition%20profile%20settings%20sleep%20social%20weight");//&expires_in=604800
    }

    public static String getString(String url,String tag) {
        String ans="";

        String[] spl = url.split("#");
        String new1 = spl[1];
        String[] newspl = new1.split("=");

        String new2 = newspl[1];
        String[] newspl1 = new2.split("&");

        String new3 = newspl[2];
        String[] newspl2 = new3.split("&");

        if(tag=="a"){
            ans=newspl1[0];
        }
        if(tag=="b"){
            ans=newspl2[0];
        }


        return  ans;
    }

}
