package webView.test;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewTestActivity extends Activity {
	
	WebView webview;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        webview = (WebView) findViewById(R.id.webview);
        
        String html = "<html><body>some html here</body></html>"; 

        webview.loadData(html, "text/html", "UTF-8");
    }
    
    
}