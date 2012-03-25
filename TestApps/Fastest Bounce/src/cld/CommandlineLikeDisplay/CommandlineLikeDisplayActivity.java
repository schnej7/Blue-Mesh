package cld.CommandlineLikeDisplay;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;


public class CommandlineLikeDisplayActivity extends Activity {
	
		private ArrayAdapter <String> mMessageArray;
		private ArrayList<String> consoleText = new ArrayList<String>();
		private ListView mMessageView;
        private EditText txtInput;
        private CLDMessage myCLDMessage;
        private backendThread myBackendThread;
        
        
        //MENU
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
                	myCLDMessage.print_normal("quit");
                    this.myBackendThread.stopService();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        
        
       @Override 
        public void onSaveInstanceState(Bundle savedInstanceState){
        	savedInstanceState.putStringArrayList("ConsoleText", consoleText);
        	savedInstanceState.putString("EditBox",txtInput.getText().toString());
        } 
        
       @Override
       public void onRestoreInstanceState(Bundle savedInstanceState){
    	   //restore saved settings (console text and input box text)
    	   txtInput.setText(savedInstanceState.getString("EditBox"));
    	   ArrayList <String> newConsole = savedInstanceState.getStringArrayList("ConsoleText");
    	   //restore messageArray's contents
    	   for(int i=0; i<newConsole.size(); i++){
    		   mMessageArray.add(newConsole.get(i));
    	   }
    	   //restore consoleText's contents
    	   consoleText=newConsole;
       }
		
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        setup();
	    }
	    
	    //Used to setup the display and the service
	    private void setup(){
	    	//Sets up the UI
	    	mMessageArray = new ArrayAdapter<String>(this, R.layout.message);
	    	//grabs the old console text from the savedInstanceState of consoleText
	    	for(int i=0; i<consoleText.size(); ++i){
	    		mMessageArray.add(consoleText.get(i));
	    	}
	    	mMessageView = (ListView) findViewById(R.id.ListMessages);
	    	mMessageView.setAdapter(mMessageArray);
	    	txtInput = (EditText)findViewById(R.id.txtInput);
	    	myCLDMessage = new CLDMessage(mHandler);
	    	
	        //button listener
	        final Button buttonCls = (Button) findViewById(R.id.btnCls);
	        buttonCls.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                clearDisplay();
	            }
	        });
	        
	        //button listener
	        final Button buttonStart = (Button) findViewById(R.id.btnStart);
	        buttonStart.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	myCLDMessage.clearQueue();
	    	    	myBackendThread = new backendThread();
	            	myBackendThread.start();
	            }
	        });
	        
	        final Button buttonGo = (Button) findViewById(R.id.btnGo);
	        buttonGo.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					getText();
				}
	        });
	        
	        txtInput.setOnKeyListener(new View.OnKeyListener() {
				
				public boolean onKey(View v, int keyCode, KeyEvent event) {	
					if( keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
						getText();
						return true;
					}
					else{
						return false;
					}
				}
			});
	        
	        final Button buttonClr = (Button) findViewById(R.id.btnClr);
	        buttonClr.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					txtInput.setText("");
				}
	        });
	    }
	    
	    private void getText(){
	    	myCLDMessage.getLineFromInput(txtInput.getText().toString());
	    }
	   
	    //Used to clear the display
	    private void clearDisplay(){
	    	mMessageArray.clear();
	    	consoleText.clear();
	    }
	    
	    //Handler used for receiving messages from the service
	    private final Handler mHandler = new Handler(){
	    	@Override
	    	public void handleMessage( Message msg ){
	    		byte[] writeBuf;
	    		String messageString;
		    	switch(msg.what){
		    	case Constants.MSG_DEBUG:
		    		if(Constants.DEBUG){
			    		writeBuf = (byte[]) msg.obj;
			    		messageString = new String(writeBuf);
			    		consoleText.add(messageString);
			    		mMessageArray.add(messageString);
			    		mMessageView.setSelection(mMessageView.getCount() - 1);
		    		}
		    		break;
		    	case Constants.MSG_NORMAL:
		    		writeBuf = (byte[]) msg.obj;
		    		messageString = new String(writeBuf);
		    		consoleText.add(messageString);
		    		mMessageArray.add(messageString);
		    		mMessageView.setSelection(mMessageView.getCount() - 1);
		    		break;
		    	}
	    	}
	    };
	    
	    private class backendThread extends Thread{
	    
	    	private FastestBounce myService;
	    	
	    	public void run(){
	    		///////////////////////////////////////////////
	    		//TODO
	    		//
	    		//Here you call the constructor or a setup function for
		    	//your class, it is important to pass in mHandler so
		    	//that in your class you can create a CLDMessage object
		    	//used for sending data to the UI
	    		///////////////////////////////////////////////
	    		myService = new FastestBounce(myCLDMessage);
        		///////////////////////////////////////////////
        		//TODO
        		//
        		//Here you can set the start button to 
            	//call a function in your class 	
        		///////////////////////////////////////////////
                myService.start();
	    	}
	    	
	    	public void stopService(){
	    		myService.stop();
	    	}
	    	
	    }
	}