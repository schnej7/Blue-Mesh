package webViewHost.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class WebViewHostActivity extends Activity {
	
	private int NumSlides = 0;
	private int currentSlide = 0;
	private ArrayList <String> slides;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        slides = new ArrayList<String>();
        
        byte[] numSlidesBuffer = new byte[3];
        
        //Use this line to read from files
        InputStream inputStream = null;
		try {
			inputStream = getAssets().open("show_info");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try {
			inputStream.read(numSlidesBuffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			inputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //NumSlides = Integer.valueOf(numSlidesBuffer.toString());
        NumSlides = 3;
        
        for( int i = 0; i < NumSlides; i++ ){
        	String fileName = "slide_" + (i+1) + ".html";
        	try {
				inputStream = getAssets().open(fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	byte[] slideBuffer = new byte[1024];
        	int bytesRead = 0;
        	try {
				bytesRead = inputStream.read(slideBuffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
                
            }
        });
    }
}