package co.kr.skycall;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import static android.R.attr.versionName;
import static android.view.View.VISIBLE;

public class VideoPlayer extends Activity {
    private MyWebChromeClient mWebChromeClient = null;
    private View mCustomView;
    private RelativeLayout mContentView;
    private FrameLayout mCustomViewContainer;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_main);

        myWebView = (WebView) findViewById(R.id.webViewV);
        mWebChromeClient = new MyWebChromeClient();
        myWebView.setWebChromeClient(mWebChromeClient);
        myWebView.setBackgroundColor(Color.parseColor("#000000"));
        myWebView.getSettings().setPluginState(WebSettings.PluginState.ON);

        myWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 16) {
            //myWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

        myWebView.setWebChromeClient(new webViewChrome());
        Bundle b = getIntent().getExtras();
        String FF = b.getString("vfn");
        String[] TF = FF.split("/");
        String VFN = TF[TF.length - 1];
        Log.e("VFN","VFN :" + VFN + " / " + FF );
        String vfn = b.getString("vfn") + "?rel=0&vq=hd720" ; //"?controls=2&enablejsapi=1&autoplay=1&vq=hd720&hd=1";

        final String mimeType = "text/html";
        final String encoding = "UTF-8";
        String html = getHTML(VFN);
        Log.e("vfn","vfn :" + vfn );
        //myWebView.loadDataWithBaseURL("", html, mimeType, encoding, "");
        myWebView.loadUrl(vfn);
    }

    public String getHTML(String videoId) {

        String html =
                "<iframe class=\"youtube-player\" "
                        + "style=\"border: 0; width: 100%; height: 95%;"
                        + "padding:0px; margin:0px\" "
                        + "id=\"ytplayer\" type=\"text/html\" "
                        + "src=\"http://www.youtube.com/embed/" + videoId
                        + "?controls=2&enablejsapi=1&autoplay=1&&vq=hd720\" frameborder=\"0\" " + "allowfullscreen autobuffer "
                        + "controls >\n" + "</iframe>\n"
                        + "<script> function onPlayerReady(event){event.target.setPlaybackQuality('hd720');}</script>";


                String tts =
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <body style='width:100%;height:100%'>\n" +
                "    <div style='width:100%;height:100%' id=\"player\"></div>\n" +
                "    <script>\n" +
                "      var tag = document.createElement('script');\n" +
                "      tag.src = \"https://www.youtube.com/iframe_api\";\n" +
                "      var firstScriptTag = document.getElementsByTagName('script')[0];\n" +
                "      firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);\n" +
                "      var player;\n" +
                "      function onYouTubeIframeAPIReady() {\n" +
                "        player = new YT.Player('player', {\n" +
                "          height: '100%',\n" +
                "          width: '100%',\n" +
                "          videoId: '" + videoId + "',\n" +
                "          events: {\n" +
                "            'onReady': onPlayerReady,\n" +
                "            'onStateChange': onPlayerStateChange\n" +
                "          }\n" +
                "        });\n" +
                "      }\n" +
                "      function onPlayerReady(event) {\n" +
                "        event.target.setPlaybackQuality('hd720'); event.target.playVideo();\n" +
                "      }\n" +
                "      var done = false;\n" +
                "      function onPlayerStateChange(event) {\n" +
                "        if (event.data == YT.PlayerState.PLAYING && !done) {\n" +
                "          event.target.setPlaybackQuality('hd720'); setTimeout(stopVideo, 6000);\n" +
                "          done = true;\n" +
                "        }\n" +
                "      }\n" +
                "      function stopVideo() {\n" +
                "        player.stopVideo();\n" +
                "      }\n" +
                "    </script>\n" +
                "  </body>\n" +
                "</html>";

        return html;
    }

    private void initPageDo() {
        Log.e("initPageDo","웹뷰시작");
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        //show webview
                        findViewById(R.id.webViewV).setVisibility(VISIBLE);
                    }
                });
            }
        }).start();
    }
    class webViewChrome extends WebChromeClient {
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress < 100) {
                //mProgressBar.setProgress(newProgress * 100);
            } else {
                //mProgressBar.setVisibility(View.INVISIBLE);
                initPageDo();
                //mProgressBar.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }
    public class MyWebChromeClient extends WebChromeClient {

        FrameLayout.LayoutParams LayoutParameters = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            // if a view already exists then immediately terminate the new one
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            mContentView = (RelativeLayout) findViewById(R.id.video_xmain);
            mContentView.setVisibility(View.GONE);
            mCustomViewContainer = new FrameLayout(VideoPlayer.this);
            mCustomViewContainer.setLayoutParams(LayoutParameters);
            mCustomViewContainer.setBackgroundResource(android.R.color.black);
            view.setLayoutParams(LayoutParameters);
            mCustomViewContainer.addView(view);
            mCustomView = view;
            mCustomViewCallback = callback;
            mCustomViewContainer.setVisibility(View.VISIBLE);
            setContentView(mCustomViewContainer);
        }

        @Override
        public void onHideCustomView() {
            if (mCustomView == null) {
                return;
            } else {
                // Hide the custom view.
                mCustomView.setVisibility(View.GONE);
                // Remove the custom view from its container.
                mCustomViewContainer.removeView(mCustomView);
                mCustomView = null;
                mCustomViewContainer.setVisibility(View.GONE);
                mCustomViewCallback.onCustomViewHidden();
                // Show the content view.
                mContentView.setVisibility(View.VISIBLE);
                setContentView(mContentView);
            }
        }
    }
    @Override
    public void onBackPressed() {
        Log.e("Backkey", "st " + mCustomViewContainer);
        Log.e("canGoBack", "canGoBack " + myWebView.canGoBack());
        if (mCustomViewContainer != null)
            mWebChromeClient.onHideCustomView();
        else{
            myWebView.loadUrl("");
            finish();
            super.onBackPressed();
        }


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
