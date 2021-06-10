package co.kr.jbbuller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;

import org.json.JSONException;

import java.sql.SQLException;

public class popup_page extends Activity {
    private popup_page.MyWebChromeClient mWebChromeClient = null;
    private View mCustomView;
    private RelativeLayout mContentView;
    private FrameLayout mCustomViewContainer;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private DbOpenHelper PopmDbOpenHelper = null;
    private WebView PopUpmyWebView;
    private Context MainContext;
    private WebSettings webViewSetting;
    String authority = "kr.jbbuller.voicerecorder.myContentProvider";
    boolean isBadCoInstall = false;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_main);
        MainContext = this.getApplicationContext();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        PopUpmyWebView = (WebView) findViewById(R.id.webViewV);
        PopUpmyWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= 19) {
            PopUpmyWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        PopUpmyWebView.getSettings().setJavaScriptEnabled(true);
        PopUpmyWebView.setWebChromeClient(new webViewChrome());
        PopUpmyWebView.setWebViewClient(new MyWebViewClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PopUpmyWebView.setWebContentsDebuggingEnabled(true);
        }
        ////////////////////////////////////////////////////////////

        PopUpmyWebView.addJavascriptInterface(new popUpJsInter(), "popUpJsInter");
        PopmDbOpenHelper = new DbOpenHelper(MainContext);
        try {
            PopmDbOpenHelper.init_DB(0);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String[] getDbInfo = PopmDbOpenHelper.getSettingvalue();
        String goUrl = getDbInfo[10];
        Log.e("창열었음", goUrl);
        PopUpmyWebView.loadUrl(goUrl);
    }
    public String PopUp_BadCompny_RecentCall_listGetfromDB(){
        Cursor c = getContentResolver().query(Uri.parse("content://"+authority+"/callLogTable_list"), null, null, null, null);
        if(c == null) return "NOTINSTALLED";
        isBadCoInstall = true;
        String retStr = "";
        while(c.moveToNext()) {
            //String str = c.getString(0) + "↕" + c.getString(1)+"↕"+c.getString(2)+"↕"+c.getString(3)+"↕"+c.getString(4);
            String str = c.getString(1) + "↕" + c.getString(3)+"↕"+c.getString(4)+
                    "↕"+c.getString(2)+"↕"+c.getString(0);
            System.out.println(str);
            retStr += str + "|";

            //304↕01090056204↕오현택↕수신↕2019-12-03 13:44:39
        }
        c.close();
        return retStr;
    }
    final class popUpJsInter {
        popUpJsInter() {
        }
        @JavascriptInterface
        public void sendSelectnumClose(String userName, String phoneNumber) {
            Log.e("sendSelectnumClose", "userName:" + userName + "/ phoneNumber:" + phoneNumber);
            final Intent launchIntent = new Intent(popup_page.this, MainActivity.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Bundle b = new Bundle();
            //b.putString("IntentJob", "multiupload" );

            b.putString("actionjob", "recent_phone_seldone" );
            b.putString("IntentJob", "recent_phone_seldone" );
            b.putString("username", userName);
            b.putString("phoneNumber", phoneNumber);

            launchIntent.putExtras(b);
            //startActivityForResult(launchIntent, 33);
            startActivityForResult(launchIntent, 7458);
            finish();
        }
        @JavascriptInterface
        public void Pop_get_Call_log_list_App() {
            String retStr = PopUp_BadCompny_RecentCall_listGetfromDB();
            Log.e("최근통화호출", retStr);
            final String goStrurl = "javascript:Pop_phone_RecentCallListPush('" + retStr + "')";
            Log.e("최근연락처" ,goStrurl );
            PopUpmyWebView.post(new Runnable() {
                @Override
                public void run() {
                    PopUpmyWebView.loadUrl(goStrurl);
                }
            });
        }


    }
    private void initPageDo() {
        Log.e("initPageDo","웹뷰시작");
    }
    private class MyWebViewClient extends WebViewClient {
        public static final String INTENT_PROTOCOL_START = "intent:";
        public static final String INTENT_PROTOCOL_INTENT = "#Intent;";
        public static final String INTENT_PROTOCOL_END = ";end;";
        public static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";
        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event){
            Log.e("MyWebViewClient", "onUnhandledKeyEvent :" + event.toString());
            return;
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, final String url) {
            //Log.e("webviewfinished", "URL :" + url + " 로딩완료");

        }
    }
    class webViewChrome extends WebChromeClient {
        public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
            Log.e("userGesture" , "G" + userGesture);
            WebView newWebView = new WebView(MainContext);
            view.addView(newWebView);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();
            newWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                    browserIntent.setData(Uri.parse(url));
                    startActivity(browserIntent);
                    return true;
                }
            });
            return true;
        }
        public void onProgressChanged(WebView view, int newProgress) {
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
            mCustomViewContainer = new FrameLayout(popup_page.this);
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
        Log.e("canGoBack", "canGoBack " + PopUpmyWebView.canGoBack());
        if (mCustomViewContainer != null)
            mWebChromeClient.onHideCustomView();
        else{
            PopUpmyWebView.loadUrl("");
            finish();
            super.onBackPressed();
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}

