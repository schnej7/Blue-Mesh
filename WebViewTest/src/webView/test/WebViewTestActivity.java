package webView.test;

import java.util.ArrayList;
import java.util.UUID;

import blue.mesh.BlueMeshService;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

public class WebViewTestActivity extends Activity {

	private BlueMeshService bms;
	private Boolean TEST = false;
	private static final String TAG = "WebViewTestActivity";
	private ViewPager awesomePager;
	private static int NUM_AWESOME_VIEWS = 0;
	private Context cxt;
	private AwesomePagerAdapter awesomeAdapter;
	private Boolean stop = false;
	private ArrayList <String> slides;
	private ReadThread readThread;
	private boolean bluetoothWorking = false;
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		readThread.interrupt();
		if(bms != null){
			Log.d(TAG, "disconnecting");
			bms.disconnect();
		}
}
	
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
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		cxt = this;

		awesomeAdapter = new AwesomePagerAdapter();
		awesomePager = (ViewPager) findViewById(R.id.awesomepager);
		awesomePager.setAdapter(awesomeAdapter);
		slides = new ArrayList<String>();
		
		slides.add("<html><body><marquee>WELCOME</marquee></body></html>");
		NUM_AWESOME_VIEWS++;
		awesomeAdapter.notifyDataSetChanged();
		
		try{
			bms = new BlueMeshService(UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66"));
		}
		catch(NullPointerException e){
			Toast.makeText(cxt, "Bluetooth Not Enabeled", Toast.LENGTH_LONG).show();
			Log.e(TAG, "BlueMeshService Constructor failed");
			return;
		}
		bluetoothWorking = true;
		
		bms.launch();
	}
	
	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage( Message msg ){
			addSlide((byte[]) msg.obj);
		}
	};

	public void onStart(){
		super.onStart();
		if (!bluetoothWorking) return;
		readThread = new ReadThread();
		readThread.start();
	}
	
	private void addSlide(byte[] bytes){
		slides.add(new String(bytes) + NUM_AWESOME_VIEWS);
		awesomeAdapter.notifyDataSetChanged();
		NUM_AWESOME_VIEWS++;
		awesomePager.setCurrentItem(NUM_AWESOME_VIEWS - 1);
	}
	
	private class ReadThread extends Thread {
		
		public void run(){
			Looper.myLooper();
			Looper.prepare();
			while (true){
				if( this.isInterrupted()){
					stop = true;
				}
				if(stop){
					Log.d(TAG, "readThread interrupted");
					return;
				}
				byte bytes[] = null;
				bytes = bms.pull();
				if( bytes == null){
					//Sleep if nothing is received to avoid
					//pounding bms
					try {
						sleep(100);
					} catch (InterruptedException e) {
						Log.e(TAG, "sleep() failed", e);
					}
				}
				else{
					if( TEST && NUM_AWESOME_VIEWS > 10 ){ stop = true; }
					mHandler.obtainMessage(0, bytes.length, -1, bytes).sendToTarget();
				}
			}
		}
	}

	private class AwesomePagerAdapter extends PagerAdapter{


		@Override
		public int getCount() {
			return NUM_AWESOME_VIEWS;
		}

		@Override
		public Object instantiateItem(View collection, int position) {
			WebView wv = new WebView(cxt);
			String data;
			switch(position){
			default:
				data = slides.get(position);
				wv.loadData(data, "text/html", null);
				break;
			}

			((ViewPager) collection).addView(wv,0);

			return wv;
		}

		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager) collection).removeView((WebView) view);
		}



		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view==((WebView)object);
		}


		@Override
		public void finishUpdate(View arg0) {}


		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {}

	}


}
