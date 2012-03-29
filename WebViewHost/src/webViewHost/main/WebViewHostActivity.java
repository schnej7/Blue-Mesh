package webViewHost.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import blue.mesh.BlueMeshService;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

public class WebViewHostActivity extends Activity {
	
	private static final String TAG = "WebViewHostActivity";
	private int NumSlides = 0;
	private int currentSlide = 0;
	private ArrayList <String> slides;
	private BlueMeshService bms;
	private Context cxt;
	private Boolean bluetoothWorking = false;
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    //Menu Click Event
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.quit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	cxt = this;
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        slides = new ArrayList<String>();
        
        byte[] numSlidesBuffer = new byte[3];
        
        //Use this line to read from files
        InputStream inputStream = null;
		try {
			inputStream = getAssets().open("show_info");
		} catch (IOException e) {
			Log.e(TAG, "open() failed", e);
		}
        try {
			inputStream.read(numSlidesBuffer);
		} catch (IOException e) {
			Log.e(TAG, "read() failed", e);
		}
        try {
			inputStream.close();
		} catch (IOException e) {
			Log.e(TAG, "close() failed", e);
		}
        
        //NumSlides = Integer.valueOf(numSlidesBuffer.toString());
        NumSlides = 3;
        
        for( int i = 0; i < NumSlides; i++ ){
        	String fileName = "slide_" + (i+1) + ".html";
        	try {
				inputStream = getAssets().open(fileName);
			} catch (IOException e) {
				Log.e(TAG, "open() failed", e);
			}
        	byte[] slideBuffer = new byte[1024];
        	int bytesRead = 0;
        	try {
				bytesRead = inputStream.read(slideBuffer);
			} catch (IOException e) {
				Log.e(TAG, "read() failed", e);
			}
        	try {
				inputStream.close();
			} catch (IOException e) {
				Log.e(TAG, "close() failed", e);
			}
        	byte[] smallerSlideBuffer = new byte[bytesRead];
        	for( int j = 0; j < bytesRead; j++ ){
        		smallerSlideBuffer[j] = slideBuffer[j];
        	}
        	slides.add( new String(smallerSlideBuffer) );
        }
        
        final WebView wv = (WebView) findViewById(R.id.wvDisplay);
        wv.loadData(slides.get(0), "text/html", null);
        
        //button listener back
        final Button buttonBack = (Button) findViewById(R.id.btnBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(currentSlide > 0){
                	currentSlide--;
                	wv.loadData(slides.get(currentSlide), "text/html", null);
                }
            }
        });
        
        //button listener next
        final Button buttonNext = (Button) findViewById(R.id.btnNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(currentSlide < NumSlides - 1){
	            	currentSlide++;
	            	wv.loadData(slides.get(currentSlide), "text/html", null);
            	}
            }
        });
        
        //button listener send
        final Button buttonSend = (Button) findViewById(R.id.btnSend);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if( bluetoothWorking ){
            		bms.write(slides.get(currentSlide).getBytes());
            	}
            }
        });
        
    	try{
    		bms = new BlueMeshService();
    	}
    	catch(NullPointerException e){
    		Toast.makeText(cxt, "Bluetooth Not Enabeled", Toast.LENGTH_LONG).show();
    		Log.e(TAG, "Bluetooth not enabeled");
    		return;
    	}
    	bluetoothWorking = true;
    	bms.launch();
    }
    
    @Override
	protected void onDestroy(){
    	super.onDestroy();
    	bms.disconnect();
    }
}