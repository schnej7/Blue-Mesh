package webView.test;

import java.util.ArrayList;

import blue.mesh.BlueMeshService;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

public class WebViewTestActivity extends Activity {

	private static final Boolean EMULATOR = true;
	private static final String TAG = "WebViewTestActivity";
	private ViewPager awesomePager;
	private static int NUM_AWESOME_VIEWS = 0;
	private Context cxt;
	private AwesomePagerAdapter awesomeAdapter;
	private BlueMeshServiceStub bms;
	private Boolean stop = false;
	private ArrayList <String> slides;

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
	}

	public void onStart(){
		super.onStart();


		if( !EMULATOR ){
			try{
				bms = new BlueMeshServiceStub();
			}
			catch(NullPointerException e){
				Toast.makeText(cxt, "Bluetooth Not Enabeled", Toast.LENGTH_LONG).show();
				Log.e(TAG, "BlueMeshService Constructor failed");
				return;
			}
		}
		else{
			bms = new BlueMeshServiceStub();
		}
		
		slides.add("<html><body><marquee>WELCOME</marquee></body></html>");
		NUM_AWESOME_VIEWS++;
		awesomeAdapter.notifyDataSetChanged();
		

		while (true){
			if(stop){
				Log.d(TAG, "readThread interrupted");
				return;
			}
			byte bytes[] = null;
			bytes = bms.pull();
			if( bytes == null){
				//We got nothing
			}
			else{
				if( EMULATOR && NUM_AWESOME_VIEWS > 10 ){ stop = true; }
				slides.add(new String(bytes) + NUM_AWESOME_VIEWS);
				awesomeAdapter.notifyDataSetChanged();
				NUM_AWESOME_VIEWS++;
				awesomePager.setCurrentItem(NUM_AWESOME_VIEWS - 1);
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
