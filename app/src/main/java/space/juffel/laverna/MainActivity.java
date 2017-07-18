package space.juffel.laverna;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.activity_main_webview);
        WebView.setWebContentsDebuggingEnabled(true);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDomStorageEnabled(true);

        // Force links and redirects to open in the WebView instead of in a browser
        // mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient(this));

        mWebView.loadUrl("file:///android_asset/index.html");
    }

    // plug in file upload handler
    // inspired by this SO-answer: https://stackoverflow.com/a/29545290/1870317
    public static final int REQUEST_SELECT_FILE = 100;
    public ValueCallback<Uri[]> uploadMessage;
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_SELECT_FILE) {
            if (uploadMessage == null) return;
            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            uploadMessage = null;
        }
    }
    public class MyWebChromeClient extends WebChromeClient {
        private MainActivity myActivity;

        public MyWebChromeClient(MainActivity activity) {
            myActivity = activity;
        }

        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            // make sure there is no existing message
            if (myActivity.uploadMessage != null) {
                myActivity.uploadMessage.onReceiveValue(null);
                myActivity.uploadMessage = null;
            }

            myActivity.uploadMessage = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            try {
                myActivity.startActivityForResult(intent, MainActivity.REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                myActivity.uploadMessage = null;
                Toast.makeText(myActivity, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                return false;
            }

            return true;
        }
    }
    // end plug in file upload handler
}
