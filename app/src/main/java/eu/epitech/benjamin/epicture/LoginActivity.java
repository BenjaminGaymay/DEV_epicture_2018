package eu.epitech.benjamin.epicture;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    Button button;
    WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        webview = findViewById(R.id.login_webview);
        webview.setVisibility(View.GONE);
        
        button = findViewById(R.id.login_button);
        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void onClick(View view) {

                webview.setVisibility(View.VISIBLE);
                button.setVisibility(View.GONE);
                webview.getSettings().setJavaScriptEnabled(true);
                webview.setWebViewClient(new WebViewClient());
                webview.loadUrl("https://api.imgur.com/oauth2/authorize?client_id=47ec1c4d2e5d41d&response_type=token");
                webview.getSettings().setSupportZoom(true);
                webview.getSettings().setBuiltInZoomControls(false);
                webview.getSettings().setPluginState(WebSettings.PluginState.ON);
                webview.getSettings().setSupportMultipleWindows(true);
                webview.getSettings().setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:61.0) Gecko/20100101 Firefox/61.0");
                webview.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        if (url.contains("www.getpostman.com/oauth2/callback#")) {
                            Intent intent = new Intent(getApplication(), MainActivity.class);

                            for (String line : url.split("#")[1].split("&")) {
                                if (line.contains("="))
                                    intent.putExtra(line.split("=")[0], line.split("=")[1]);
                            }

                            startActivity(intent);
                        }
                        super.onPageStarted(view, url, favicon);
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        return super.shouldOverrideUrlLoading(view, request);
                    }


                });
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (webview.getVisibility() == View.VISIBLE) {
            webview.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        } else
            super.onBackPressed();
    }
}
